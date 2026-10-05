/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.cache.SimpleCache
import com.convx.music.constants.HideExplicitKey
import com.convx.music.constants.HideVideoSongsKey
import com.convx.music.constants.DataSaverEnabledKey
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.Song
import com.convx.music.di.DownloadCache
import com.convx.music.di.PlayerCache
import com.convx.music.extensions.filterExplicit
import com.convx.music.extensions.filterVideoSongs
import com.convx.music.utils.dataStore
import com.convx.music.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CachePlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    @PlayerCache private val playerCache: SimpleCache,
    @DownloadCache private val downloadCache: SimpleCache
) : ViewModel() {

    private val _cachedSongs = MutableStateFlow<List<Song>>(emptyList())
    val cachedSongs: StateFlow<List<Song>> = _cachedSongs

    init {
        viewModelScope.launch {
            while (true) {
                val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false) || context.dataStore.get(DataSaverEnabledKey, false)
                val cachedIds = playerCache.keys.map { it.substringBefore('#') }.filter { it.isNotBlank() }.toSet()
                val downloadedIds = downloadCache.keys.map { it.substringBefore('#') }.filter { it.isNotBlank() }.toSet()
                val pureCacheIds = cachedIds.subtract(downloadedIds)

                val songs = if (pureCacheIds.isNotEmpty()) {
                    database.getSongsByIds(pureCacheIds.toList())
                } else {
                    emptyList()
                }

                val completeSongs = songs.filter { song ->
                    val id = song.song.id
                    val cachedBytes = maxOf(
                        playerCache.getCachedBytes(id, 0, Long.MAX_VALUE),
                        playerCache.getCachedBytes("$id#flac", 0, Long.MAX_VALUE),
                        playerCache.getCachedBytes("$id#saavn", 0, Long.MAX_VALUE)
                    )
                    val contentLength = song.format?.contentLength
                    (contentLength != null && cachedBytes >= contentLength * 0.75f) || cachedBytes > 300_000L
                }

                if (completeSongs.isNotEmpty()) {
                    database.query {
                        completeSongs.forEach {
                            if (it.song.dateDownload == null) {
                                update(it.song.copy(dateDownload = LocalDateTime.now()))
                            }
                        }
                    }
                }

                _cachedSongs.value = completeSongs
                    .sortedByDescending { it.song.dateDownload ?: LocalDateTime.MIN }
                    .filterExplicit(hideExplicit)
                    .filterVideoSongs(hideVideoSongs)

                delay(2000)
            }
        }
    }

    fun removeSongFromCache(songId: String) {
        playerCache.removeResource(songId)
    }
}
