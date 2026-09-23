/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.convx.music.R
import com.convx.music.constants.ChipSortTypeKey
import com.convx.music.constants.LibraryFilter
import com.convx.music.ui.component.ChipsRow
import com.convx.music.ui.component.HomeImageBackground
import com.convx.music.utils.rememberEnumPreference

@Composable
fun LibraryScreen(navController: NavController) {
    var filterType by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)

    val filterContent: @Composable () -> Unit = {
        Row {
            ChipsRow(
                chips = listOf(
                    LibraryFilter.PLAYLISTS to stringResource(R.string.filter_playlists),
                    LibraryFilter.SONGS to stringResource(R.string.filter_songs),
                    LibraryFilter.ALBUMS to stringResource(R.string.filter_albums),
                ),
                currentValue = filterType,
                onValueUpdate = {
                    filterType = if (filterType == it) {
                        LibraryFilter.LIBRARY
                    } else {
                        it
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Hoisted here (not per-tab) so switching Songs/Albums/Artists/Playlists
        // doesn't lose the custom background — previously only LibraryMixScreen drew it.
        HomeImageBackground(withGradient = true)

        AnimatedContent(
            targetState = filterType,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                (fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width ->
                            if (forward) width / 8 else -width / 8
                        }) togetherWith
                        (fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                slideOutHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { width ->
                                    if (forward) -width / 8 else width / 8
                                })
            },
            label = "library_tab_transition",
            modifier = Modifier.fillMaxSize(),
        ) { targetFilter ->
            when (targetFilter) {
                LibraryFilter.LIBRARY -> LibraryMixScreen(navController, filterContent)
                LibraryFilter.PLAYLISTS -> LibraryPlaylistsScreen(navController, filterContent)
                LibraryFilter.SONGS -> LibrarySongsScreen(navController, filterContent)
                LibraryFilter.ALBUMS -> LibraryAlbumsScreen(navController, filterContent)
                LibraryFilter.ARTISTS -> LibraryMixScreen(navController, filterContent)
            }
        }
    }
}
