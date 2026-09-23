/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.lyrics

import android.content.Context
import android.util.LruCache
import com.convx.music.constants.LyricsProviderOrderKey
import com.convx.music.constants.PreferredLyricsProvider
import com.convx.music.constants.PreferredLyricsProviderKey
import com.convx.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.convx.music.extensions.toEnum
import com.convx.music.models.MediaMetadata
import com.convx.music.utils.NetworkConnectivityObserver
import com.convx.music.utils.dataStore
import com.convx.music.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class LyricsHelper
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val networkConnectivity: NetworkConnectivityObserver,
) {
    /**
     * Resolves the ordered list of lyrics providers from the user's saved priority order.
     * Falls back to migrating the legacy [PreferredLyricsProvider] enum if the new order
     * preference has not been written yet, ensuring a smooth upgrade for existing users.
     */
    private suspend fun resolveLyricsProviders(): List<LyricsProvider> {
        val preferences = context.dataStore.data.first()
        val orderString = preferences[LyricsProviderOrderKey].orEmpty()

        if (orderString.isNotBlank()) {
            return LyricsProviderRegistry.getOrderedProviders(orderString)
        }

        // Migration path: place the old preferred provider first in the default order
        val preferredEnum = preferences[PreferredLyricsProviderKey]
            .toEnum(PreferredLyricsProvider.LRCLIB)
        val preferredName = LyricsProviderRegistry.getProviderNameForEnum(preferredEnum)
        val defaultOrder = LyricsProviderRegistry.getDefaultProviderOrder()
        val migratedOrder = listOf(preferredName) + defaultOrder.filter { it != preferredName }
        return migratedOrder.mapNotNull { LyricsProviderRegistry.getProviderByName(it) }
    }



    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)
    private val inFlightRequests = ConcurrentHashMap<String, Deferred<LyricsWithProvider>>()
    private var currentLyricsJob: Job? = null

    suspend fun getLyrics(mediaMetadata: MediaMetadata): LyricsWithProvider {
        val artistTitleKey = "${mediaMetadata.artists.joinToString { it.name }}-${mediaMetadata.title}".replace(" ", "")
        val cached = cache.get(mediaMetadata.id)?.firstOrNull() ?: cache.get(artistTitleKey)?.firstOrNull()
        if (cached != null) {
            return LyricsWithProvider(cached.lyrics, cached.providerName)
        }

        // Single-flight deduplication: if this song is already being fetched, await the existing request
        val existingDeferred = inFlightRequests[mediaMetadata.id]
        if (existingDeferred != null && existingDeferred.isActive) {
            return existingDeferred.await()
        }

        // Check network connectivity before making network requests
        val isNetworkAvailable = try {
            networkConnectivity.isCurrentlyConnected()
        } catch (e: Exception) {
            true
        }
        
        if (!isNetworkAvailable) {
            return LyricsWithProvider(LYRICS_NOT_FOUND, "Unknown")
        }

        val providers = resolveLyricsProviders()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val deferred = scope.async {
            try {
                for (provider in providers) {
                    if (!provider.isEnabled(context)) continue
                    try {
                        // Max 3s per provider: enough for YouTube Music's two sequential
                        // API calls (next + lyrics browse) while still failing fast on dead providers.
                        val result = withTimeoutOrNull(3000L) {
                            provider.getLyrics(
                                mediaMetadata.id,
                                mediaMetadata.title,
                                mediaMetadata.artists.joinToString { it.name },
                                mediaMetadata.duration,
                                mediaMetadata.album?.title,
                            )
                        } ?: continue

                        result.onSuccess { lyrics ->
                            if (lyrics.isNotBlank() && lyrics != LYRICS_NOT_FOUND) {
                                val found = LyricsWithProvider(lyrics, provider.name)
                                val resultList = listOf(LyricsResult(provider.name, lyrics))
                                cache.put(mediaMetadata.id, resultList)
                                cache.put(artistTitleKey, resultList)
                                return@async found
                            }
                        }.onFailure {
                            reportException(it)
                        }
                    } catch (e: Exception) {
                        reportException(e)
                    }
                }
                LyricsWithProvider(LYRICS_NOT_FOUND, "Unknown")
            } finally {
                inFlightRequests.remove(mediaMetadata.id)
            }
        }

        inFlightRequests[mediaMetadata.id] = deferred
        val result = try {
            deferred.await()
        } finally {
            scope.cancel()
        }
        return result
    }

    suspend fun getAllLyrics(
        mediaId: String,
        songTitle: String,
        songArtists: String,
        duration: Int,
        album: String? = null,
        callback: (LyricsResult) -> Unit,
    ) {
        currentLyricsJob?.cancel()

        val cacheKey = "$songArtists-$songTitle".replace(" ", "")
        val cached = cache.get(mediaId) ?: cache.get(cacheKey)
        if (cached != null) {
            cached.forEach { callback(it) }
            return
        }

        // Check network connectivity before making network requests
        val isNetworkAvailable = try {
            networkConnectivity.isCurrentlyConnected()
        } catch (e: Exception) {
            true
        }
        
        if (!isNetworkAvailable) {
            return
        }

        val allResult = mutableListOf<LyricsResult>()
        val providers = resolveLyricsProviders()
        currentLyricsJob = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            providers.forEach { provider ->
                if (provider.isEnabled(context)) {
                    try {
                        provider.getAllLyrics(mediaId, songTitle, songArtists, duration, album) { lyrics ->
                            val result = LyricsResult(provider.name, lyrics)
                            allResult += result
                            callback(result)
                        }
                    } catch (e: Exception) {
                        reportException(e)
                    }
                }
            }
            cache.put(cacheKey, allResult)
            cache.put(mediaId, allResult)
        }

        currentLyricsJob?.join()
    }

    fun cancelCurrentLyricsJob() {
        currentLyricsJob?.cancel()
        currentLyricsJob = null
    }

    companion object {
        private const val MAX_CACHE_SIZE = 10
    }
}

data class LyricsResult(
    val providerName: String,
    val lyrics: String,
)

data class LyricsWithProvider(
    val lyrics: String,
    val provider: String,
)