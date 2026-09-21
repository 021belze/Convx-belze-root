/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        var lastScrollingUp = true
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                lastScrollingUp = previousIndex > firstVisibleItemIndex
            } else if (previousScrollOffset != firstVisibleItemScrollOffset) {
                lastScrollingUp = previousScrollOffset > firstVisibleItemScrollOffset
            }
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
            lastScrollingUp
        }
    }.value
}

@Composable
fun LazyGridState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        var lastScrollingUp = true
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                lastScrollingUp = previousIndex > firstVisibleItemIndex
            } else if (previousScrollOffset != firstVisibleItemScrollOffset) {
                lastScrollingUp = previousScrollOffset > firstVisibleItemScrollOffset
            }
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
            lastScrollingUp
        }
    }.value
}

@Composable
fun ScrollState.isScrollingUp(): Boolean {
    return remember(this) {
        var previousScrollOffset = value
        var lastScrollingUp = true
        derivedStateOf {
            if (previousScrollOffset != value) {
                lastScrollingUp = previousScrollOffset > value
            }
            previousScrollOffset = value
            lastScrollingUp
        }
    }.value
}
