/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.utils.YouTubeUrlParser
import com.convx.music.constants.HideExplicitKey
import com.convx.music.constants.HideVideoSongsKey
import com.convx.music.constants.DataSaverEnabledKey
import com.convx.music.db.MusicDatabase
import com.convx.music.db.entities.SearchHistory
import com.convx.music.utils.dataStore
import com.convx.music.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            query
                // Debounce here, at the ViewModel layer, so the operator chain
                // correctly feeds into flatMapLatest below. The 300 ms window
                // collapses rapid keystrokes into a single emission before any
                // network or DB work starts.
                .debounce { q -> if (q.isEmpty()) 0L else 300L }
                // Skip re-executing the pipeline when the settled value hasn't
                // actually changed (e.g. cursor moved but text is identical).
                .distinctUntilChanged()
                // flatMapLatest cancels the previous coroutine whenever a new
                // query comes in. Combined with debounce this guarantees that
                // a slow in-flight request (e.g. 2 s network) is abandoned the
                // moment the user types again — results never arrive out of order.
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        database.searchHistory().map { history ->
                            SearchSuggestionViewState(
                                history = history,
                            )
                        }
                    } else {
                        val parsedUrl = YouTubeUrlParser.parse(query)
                        val parsedItem = if (parsedUrl != null) fetchParsedUrlItem(parsedUrl) else null

                        val result = if (parsedUrl != null) null else YouTube.searchSuggestions(query).getOrNull()
                        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false) || context.dataStore.get(DataSaverEnabledKey, false)

                        database
                            .searchHistory(query)
                            .map { it.take(3) }
                            .map { history ->
                                SearchSuggestionViewState(
                                    history = history,
                                    suggestions =
                                        result
                                            ?.queries
                                            ?.filter { suggestionQuery ->
                                                history.none { it.query == suggestionQuery }
                                            }.orEmpty(),
                                    items = listOfNotNull(parsedItem) +
                                        result
                                            ?.recommendedItems
                                            ?.distinctBy { it.id }
                                            ?.filter { it.id != parsedItem?.id }
                                            ?.filterExplicit(hideExplicit)
                                            ?.filterVideoSongs(hideVideoSongs)
                                            .orEmpty(),
                                    isFromLink = parsedUrl != null
                                )
                            }
                    }
                }.collect {
                    _viewState.value = it
                }
        }
    }

    private suspend fun fetchParsedUrlItem(parsedUrl: YouTubeUrlParser.ParsedUrl): YTItem? {
        Timber.d("Fetching metadata for parsed URL: $parsedUrl")
        return try {
            val item = when (parsedUrl) {
                is YouTubeUrlParser.ParsedUrl.Video -> {
                    YouTube.queue(listOf(parsedUrl.id)).getOrNull()?.firstOrNull()
                }

                is YouTubeUrlParser.ParsedUrl.Artist -> {
                    YouTube.artist(parsedUrl.id).getOrNull()?.artist
                }
            }
            Timber.d("Fetch successful: ${item?.id} (${item?.javaClass?.simpleName})")
            item
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch metadata for parsed URL")
            null
        }
    }
}

data class SearchSuggestionViewState(
    val history: List<SearchHistory> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
    val isFromLink: Boolean = false,
)
