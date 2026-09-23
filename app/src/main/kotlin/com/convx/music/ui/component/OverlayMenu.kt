/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */
package com.convx.music.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.convx.music.LocalPlayerAwareWindowInsets
import com.convx.music.ui.component.shapes.ContinuousRoundedRectangle

/**
 * The long-press actions, as an overlay rather than a bottom sheet.
 *
 * Same [MenuState] and therefore the same call sites: every `menuState.show { … }` in the
 * app goes through here or through [BottomSheetMenu] depending on one preference, and
 * neither the menus themselves nor the ~30 places that open them know which is in use.
 *
 * A dim rather than a blur behind it. Blurring the whole window every frame a menu is
 * open is real GPU cost for a surface that is about to be covered anyway, and sampling
 * the app backdrop from a root-level overlay is exactly the RenderNode cycle the glass
 * chrome has to avoid elsewhere. The dim reads the same and costs nothing.
 */
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

@Composable
fun OverlayMenu(
    state: MenuState,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val maxHeight = (configuration.screenHeightDp - if (isLandscape) 16 else 32).dp
    val maxWidth = if (isLandscape) 740.dp else MenuMaxWidth

    fun dismiss() {
        focusManager.clearFocus()
        state.isVisible = false
    }

    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                // No ripple and no indication: the scrim is a dismiss target, not a
                // button, and a ripple spreading across the whole window on every
                // outside tap looks like a bug.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::dismiss,
                ),
            contentAlignment = if (isLandscape) Alignment.Center else Alignment.BottomCenter,
        ) {
            BackHandler(onBack = ::dismiss)

            // The scrim fades (above); the menu itself rises in portrait or fades in landscape.
            AnimatedVisibility(
                visible = state.isVisible,
                enter = if (isLandscape) {
                    fadeIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                } else {
                    slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        initialOffsetY = { it },
                    ) + fadeIn()
                },
                exit = if (isLandscape) {
                    fadeOut(spring(stiffness = Spring.StiffnessMedium))
                } else {
                    slideOutVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        targetOffsetY = { it },
                    ) + fadeOut()
                },
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(
                            if (isLandscape) WindowInsets.systemBars
                            else WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        )
                        .imePadding()
                        .padding(horizontal = if (isLandscape) 16.dp else 12.dp, vertical = if (isLandscape) 6.dp else 12.dp)
                        .widthIn(max = maxWidth)
                        .heightIn(max = maxHeight)
                        .fillMaxWidth()
                        .background(background, ContinuousRoundedRectangle(if (isLandscape) 24.dp else 28.dp))
                        // Swallows taps so a press on the menu itself does not reach the
                        // scrim's dismiss handler underneath.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        // Deliberately NOT verticalScroll: the menus put a LazyColumn inside
                        // this Column, and a lazy list measured inside a scrolling parent gets
                        // an infinite height constraint and throws. The menus scroll
                        // themselves; this Column only has to stay bounded, which the
                        // fillMaxSize parent and heightIn guarantee.
                        .padding(horizontal = if (isLandscape) 16.dp else 20.dp, vertical = if (isLandscape) 8.dp else 12.dp),
                    content = { state.content(this) },
                )
            }
        }
    }
}

/** Heavy enough that the list behind reads as dismissed, light enough to keep context. */
private val ScrimColor = Color.Black.copy(alpha = 0.55f)

/** Beyond this the action rows stretch into a very wide, hard-to-scan line on a tablet. */
private val MenuMaxWidth = 560.dp
