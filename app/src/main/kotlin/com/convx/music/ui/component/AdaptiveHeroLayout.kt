/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.convx.music.LocalTabView

/**
 * Adaptive hero container for detail screens (Album, Artist, Playlist).
 *
 * In phone view: artwork, title/info, and controls are stacked vertically.
 * In tablet/wide view: artwork sits on the start side with info and primary
 * controls beside it, matching Apple Music iPad hero presentation.
 */
@Composable
fun AdaptiveHeroLayout(
    artwork: @Composable () -> Unit,
    info: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    artworkWidth: Dp = 280.dp,
    gap: Dp = 24.dp,
    content: (@Composable () -> Unit)? = null,
) {
    if (LocalTabView.current) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(artworkWidth)) {
                    artwork()
                }
                Spacer(modifier = Modifier.width(gap))
                Column(modifier = Modifier.weight(1f)) {
                    info()
                }
            }
            content?.invoke()
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            artwork()
            info()
            content?.invoke()
        }
    }
}
