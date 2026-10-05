/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.viewmodels

import kotlinx.coroutines.flow.combine
import androidx.compose.runtime.Immutable
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.pages.HistoryPage
import com.convx.music.constants.HideVideoSongsKey
import com.convx.music.constants.DataSaverEnabledKey
import com.convx.music.constants.HistorySource
import com.convx.music.constants.InnerTubeCookieKey
import com.music.innertube.utils.parseCookieString
import com.convx.music.db.MusicDatabase
import com.convx.music.utils.dataStore
import com.convx.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

import kotlinx.coroutines.flow.first
import com.convx.music.models.toMediaMetadata
import com.convx.music.db.entities.Event
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
) : ViewModel() {
    var historySource = MutableStateFlow(HistorySource.LOCAL)
    val isSyncing = MutableStateFlow(false)

    private val today = LocalDate.now()
    private val thisMonday = today.with(DayOfWeek.MONDAY)
    private val lastMonday = thisMonday.minusDays(7)

    val historyPage = MutableStateFlow<HistoryPage?>(null)

    val events =
        context.dataStore.data
            .map { (it[HideVideoSongsKey] ?: false) || (it[DataSaverEnabledKey] ?: false) }
            .distinctUntilChanged()
            .flatMapLatest { hideVideoSongs ->
                database
                    .events()
                    .map { events ->
                        events
                            .filter { !hideVideoSongs || !it.song.song.isVideo }
                            .groupBy {
                                val date = it.event.timestamp.toLocalDate()
                                val daysAgo = ChronoUnit.DAYS.between(date, today).toInt()
                                when {
                                    daysAgo == 0 -> DateAgo.Today
                                    daysAgo == 1 -> DateAgo.Yesterday
                                    date >= thisMonday -> DateAgo.ThisWeek
                                    date >= lastMonday -> DateAgo.LastWeek
                                    else -> DateAgo.Other(date.withDayOfMonth(1))
                                }
                            }.toSortedMap(
                                compareBy { dateAgo ->
                                    when (dateAgo) {
                                        DateAgo.Today -> 0L
                                        DateAgo.Yesterday -> 1L
                                        DateAgo.ThisWeek -> 2L
                                        DateAgo.LastWeek -> 3L
                                        is DateAgo.Other -> ChronoUnit.DAYS.between(dateAgo.date, today)
                                    }
                                },
                            ).mapValues { entry ->
                                entry.value.distinctBy { it.song.id }
                            }
                    }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val flatEvents = events
        .map { map -> map.values.flatten() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val searchQuery = MutableStateFlow("")

    val filteredEvents = combine(events, searchQuery) { eventsMap, queryText ->
        if (queryText.isEmpty()) {
            eventsMap
        } else {
            eventsMap.mapValues { (_, songs) ->
                songs.filter { event ->
                    event.song.song.title.contains(queryText, ignoreCase = true) ||
                            event.song.artists.any {
                                it.name.contains(queryText, ignoreCase = true)
                            }
                }
            }.filterValues { it.isNotEmpty() }
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val filteredFlatEvents = combine(flatEvents, searchQuery) { flatList, queryText ->
        if (queryText.isEmpty()) {
            flatList
        } else {
            flatList.filter { event ->
                event.song.song.title.contains(queryText, ignoreCase = true) ||
                        event.song.artists.any {
                            it.name.contains(queryText, ignoreCase = true)
                        }
            }
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredRemoteContent = combine(historyPage, searchQuery) { page, queryText ->
        if (queryText.isEmpty()) {
            page?.sections
        } else {
            page?.sections?.map { section ->
                section.copy(
                    songs = section.songs.filter { song ->
                        song.title.contains(queryText, ignoreCase = true) ||
                                song.artists.any { it.name.contains(queryText, ignoreCase = true) }
                    }
                )
            }?.filter { it.songs.isNotEmpty() }
        }
    }.flowOn(Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    init {
        // Observe login state: auto-switch to REMOTE and auto-sync when logged in
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[InnerTubeCookieKey] ?: "" }
                .distinctUntilChanged()
                .collect { cookie ->
                    val loggedIn = "SAPISID" in parseCookieString(cookie)
                    if (!loggedIn) {
                        historyPage.value = null
                        historySource.value = HistorySource.LOCAL
                    } else {
                        historySource.value = HistorySource.REMOTE
                        fetchRemoteHistory(syncToDatabase = true)
                    }
                }
        }
    }

    fun fetchRemoteHistory(syncToDatabase: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncing.value = true
            try {
                YouTube.musicHistory().onSuccess { page ->
                    historyPage.value = page
                    if (syncToDatabase) {
                        syncRemoteHistoryToDatabase(page)
                    }
                }.onFailure {
                    reportException(it)
                }
            } finally {
                isSyncing.value = false
            }
        }
    }

    private suspend fun syncRemoteHistoryToDatabase(page: HistoryPage) {
        val sections = page.sections ?: return
        val now = LocalDateTime.now()
        val existingEvents = try {
            database.events().first()
        } catch (_: Exception) {
            emptyList()
        }
        val existingKeys = existingEvents.map { "${it.event.songId}_${it.event.timestamp.toLocalDate()}" }.toMutableSet()

        database.transaction {
            sections.forEachIndexed { sectionIndex, section ->
                val baseTime = when {
                    section.title.contains("today", ignoreCase = true) || section.title.contains("hari ini", ignoreCase = true) -> now
                    section.title.contains("yesterday", ignoreCase = true) || section.title.contains("kemarin", ignoreCase = true) -> now.minusDays(1)
                    section.title.contains("week", ignoreCase = true) || section.title.contains("minggu", ignoreCase = true) -> now.minusDays(3 + sectionIndex.toLong())
                    else -> now.minusDays(7 + sectionIndex.toLong() * 2)
                }

                section.songs.forEachIndexed { songIndex, song ->
                    val timestamp = baseTime.minusMinutes(songIndex.toLong() * 5)
                    val key = "${song.id}_${timestamp.toLocalDate()}"
                    if (key !in existingKeys) {
                        try {
                            insert(song.toMediaMetadata())
                            insert(
                                Event(
                                    songId = song.id,
                                    timestamp = timestamp,
                                    playTime = (song.duration ?: 180) * 1000L
                                )
                            )
                            existingKeys.add(key)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }
}

@Immutable
sealed class DateAgo {
    @Immutable
    data object Today : DateAgo()

    @Immutable
    data object Yesterday : DateAgo()

    @Immutable
    data object ThisWeek : DateAgo()

    @Immutable
    data object LastWeek : DateAgo()

    @Immutable
    class Other(
        val date: LocalDate,
    ) : DateAgo() {
        override fun equals(other: Any?): Boolean {
            if (other is Other) return date == other.date
            return super.equals(other)
        }

        override fun hashCode(): Int = date.hashCode()
    }
}
