/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.convx.music.LocalPlayerAwareWindowInsets
import com.convx.music.R
import com.convx.music.ui.component.backdrop.backdrops.rememberLayerBackdrop
import com.convx.music.ui.utils.isScrollingUp

private val FabEnterTransition =
    slideInVertically(spring(dampingRatio = 0.8f, stiffness = 380f)) { it / 2 } +
        fadeIn(tween(180)) +
        scaleIn(spring(dampingRatio = 0.8f, stiffness = 380f), initialScale = 0.8f)

private val FabExitTransition =
    slideOutVertically(spring(dampingRatio = 1.0f, stiffness = 400f)) { it / 2 } +
        fadeOut(tween(150)) +
        scaleOut(spring(dampingRatio = 1.0f, stiffness = 400f), targetScale = 0.8f)

@Composable
fun BoxScope.HideOnScrollFAB(
    visible: Boolean = true,
    lazyListState: LazyListState,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    onRecognitionClick: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible && lazyListState.isScrollingUp(),
        enter = FabEnterTransition,
        exit = FabExitTransition,
        modifier =
        Modifier
            .align(Alignment.BottomEnd)
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
    ) {
        // Unattached backdrop, scoped to just these two buttons: never
        // .layerBackdrop'd, so liquidGlass early-returns to its translucent
        // frosted fallback instead of sampling the live NavHost-wide
        // appBackdrop, which — since this composable is used on ordinary
        // NavHost screens with no hero backdrop of their own to borrow —
        // would be a RenderNode self-reference cycle. Same style as the nav
        // bar's glass (liquidGlass + GlassCircleButton), just without real
        // blur-through here.
        val fabBackdrop = rememberLayerBackdrop()
        CompositionLocalProvider(LocalAppBackdrop provides fabBackdrop) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (onRecognitionClick != null) {
                GlassCircleButton(
                    onClick = onRecognitionClick,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic),
                        contentDescription = stringResource(R.string.recognize_music),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            GlassCircleButton(
                onClick = onClick,
                size = 56.dp,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                )
            }
        }
        }
    }
}

@Composable
fun BoxScope.HideOnScrollFAB(
    visible: Boolean = true,
    lazyListState: LazyGridState,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    onRecognitionClick: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible && lazyListState.isScrollingUp(),
        enter = FabEnterTransition,
        exit = FabExitTransition,
        modifier =
        Modifier
            .align(Alignment.BottomEnd)
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
    ) {
        // Unattached backdrop, scoped to just these two buttons: never
        // .layerBackdrop'd, so liquidGlass early-returns to its translucent
        // frosted fallback instead of sampling the live NavHost-wide
        // appBackdrop, which — since this composable is used on ordinary
        // NavHost screens with no hero backdrop of their own to borrow —
        // would be a RenderNode self-reference cycle. Same style as the nav
        // bar's glass (liquidGlass + GlassCircleButton), just without real
        // blur-through here.
        val fabBackdrop = rememberLayerBackdrop()
        CompositionLocalProvider(LocalAppBackdrop provides fabBackdrop) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (onRecognitionClick != null) {
                GlassCircleButton(
                    onClick = onRecognitionClick,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic),
                        contentDescription = stringResource(R.string.recognize_music),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            GlassCircleButton(
                onClick = onClick,
                size = 56.dp,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                )
            }
        }
        }
    }
}

@Composable
fun BoxScope.HideOnScrollFAB(
    visible: Boolean = true,
    scrollState: ScrollState,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    onRecognitionClick: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible && scrollState.isScrollingUp(),
        enter = FabEnterTransition,
        exit = FabExitTransition,
        modifier =
        Modifier
            .align(Alignment.BottomEnd)
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            ),
    ) {
        // Unattached backdrop, scoped to just these two buttons: never
        // .layerBackdrop'd, so liquidGlass early-returns to its translucent
        // frosted fallback instead of sampling the live NavHost-wide
        // appBackdrop, which — since this composable is used on ordinary
        // NavHost screens with no hero backdrop of their own to borrow —
        // would be a RenderNode self-reference cycle. Same style as the nav
        // bar's glass (liquidGlass + GlassCircleButton), just without real
        // blur-through here.
        val fabBackdrop = rememberLayerBackdrop()
        CompositionLocalProvider(LocalAppBackdrop provides fabBackdrop) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (onRecognitionClick != null) {
                GlassCircleButton(
                    onClick = onRecognitionClick,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic),
                        contentDescription = stringResource(R.string.recognize_music),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            GlassCircleButton(
                onClick = onClick,
                size = 56.dp,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                )
            }
        }
        }
    }
}
