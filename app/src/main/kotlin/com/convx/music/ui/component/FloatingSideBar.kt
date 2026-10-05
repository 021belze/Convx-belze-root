/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import com.convx.music.ui.utils.Motion
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.zIndex
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.convx.music.R
import com.convx.music.ui.component.backdrop.catalog.utils.InteractiveHighlight
import com.convx.music.ui.component.backdrop.drawBackdrop
import com.convx.music.ui.component.backdrop.effects.blur
import com.convx.music.ui.component.backdrop.effects.lens
import com.convx.music.ui.component.backdrop.highlight.Highlight
import com.convx.music.ui.component.backdrop.shadow.Shadow
import com.convx.music.ui.component.shapes.ContinuousRoundedRectangle
import com.convx.music.ui.player.FloatingMiniPlayer
import com.convx.music.ui.screens.Screens
import com.convx.music.ui.theme.BrandName
import com.convx.music.ui.theme.LocalAccentTextColor
import com.convx.music.ui.theme.LocalDynamicPlayerThemeColor
import com.convx.music.ui.theme.rememberBrandFontFamily
import com.convx.music.ui.utils.fadingEdge

/** Panel corner. A tall panel, so a rounded rect rather than the phone bar's capsule. */
private val SideBarShape = ContinuousRoundedRectangle(28.dp)

/** Row/puck corner inside the panel: a full capsule. */
private val SideRowShape = ContinuousRoundedRectangle(percent = 50)

/** Width of the whole floating panel. */
val SideBarWidth: Dp = 260.dp

/** Width when collapsed to an icon-only rail — just enough for the icon + padding. */
val SideBarCollapsedWidth: Dp = 72.dp

/**
 * Container width at or above which the app lays itself out for tab view on its
 * own. Below it the side bar only appears if the Appearance toggle forces it.
 */
val TabletWidthThreshold: Dp = 840.dp

/** Panel inset from the screen edge, and the gap it leaves to the content. */
val SideBarMargin: Dp = 12.dp

/**
 * Horizontal space content should reserve on the start edge.
 *
 * Applied as *content padding*, never as a layout inset: rows begin clear of the
 * panel at rest but the list still spans the full width, so scrolling carries
 * them under the glass rather than clipping them short of it.
 */
val SideBarContentInset: Dp = SideBarWidth + SideBarMargin * 2

/**
 * Rim and shadow the puck keeps when it is NOT pressed. Driving both straight
 * off pressProgress leaves the puck shapeless at rest, which is exactly how it
 * vanishes into the panel over dark content.
 */
private const val PuckRestHighlightAlpha = 0.5f
private const val PuckRestShadowAlpha = 0.35f

private val SideTabHeight = 48.dp
private val SideBarContentPadding = PaddingValues(6.dp)

/**
 * Fixed leading slot every row's icon/artwork/avatar is centred in, so the labels
 * all start at the same x. Without it a 22dp icon row and a 24dp artwork row put
 * their text 2dp apart, which is exactly what reads as "the icons don't line up".
 */
private val SideRowLeadingSlot = 24.dp
private val SideRowIconSize = 22.dp

/**
 * The free-floating now playing pill in tab view: 80% of the width left over
 * after the side bar, centred in it, with the height following from that width
 * so it keeps its proportions on any screen.
 */
const val FloatingMiniPlayerWidthFraction = 0.8f

/** Height as a share of the pill's own width, clamped to stay a bar. */
private const val FloatingMiniPlayerAspect = 0.11f
private val FloatingMiniPlayerMinHeight = 64.dp
private val FloatingMiniPlayerMaxHeight = 64.dp

/** A non-tab destination in the side bar: history, stats, a playlist, and so on. */
@Immutable
data class SideBarLink(
    val label: String,
    val iconRes: Int? = null,
    /** Playlist/album art, shown instead of [iconRes] when set. */
    val thumbnailUrl: String? = null,
    val isSelected: Boolean = false,
    val onClick: () -> Unit,
)

@Immutable
data class SideBarSection(
    /** null renders the rows with no heading. */
    val title: String? = null,
    val links: List<SideBarLink>,
    val isCollapsible: Boolean = false,
    val defaultCollapsed: Boolean = false,
)

@Immutable
private data class SideTab(
    val screen: Screens,
    val selected: Boolean,
    val onClick: () -> Unit,
)

/**
 * The tab-view side bar: a full-height floating glass panel.
 *
 * Three zones, as in the reference. The wordmark is pinned at the top and the
 * account/settings row at the bottom; everything between them scrolls, and fades
 * out under both so rows slide beneath rather than being cut off.
 *
 * The primary tab group inside it is the phone's floating nav bar rebuilt on the
 * vertical axis — same [GlassEffectConfig] glass, the same glass selection puck
 * and the same [InteractiveHighlight] finger glow.
 *
 * The panel floats OVER the content: nothing reserves layout width for it, so
 * screens run full width and scroll underneath its glass.
 */
@Composable
fun AppFloatingSideBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    sections: List<SideBarSection>,
    footer: @Composable (collapsed: Boolean) -> Unit,
    collapsed: Boolean,
    onToggleCollapsed: () -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    val glassConfig = LocalGlassEffectConfig.current
    val useGlass = glassConfig.isEnabledFor(GlassComponent.SIDE_PANEL) && isGlassAllowed()
    val animationScope = rememberCoroutineScope()
    val targetPanelWidth = if (collapsed) SideBarCollapsedWidth else SideBarWidth
    val panelWidth by animateDpAsState(
        targetValue = targetPanelWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sideBarWidth",
    )

    // Same finger-tracking glow the mini player carries: it follows the touch
    // across the whole panel, so every row lights up under the finger, not just
    // the tab group.
    // radiusScale is much smaller than the default 1.5f: minDimension here is the
    // panel's full width, not a row's — at the default scale the glow bloom
    // covered several rows and buried the icon/label under the finger.
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope, radiusScale = 0.3f)
    }

    val backgroundColor = when {
        useGlass -> Color.Transparent
        pureBlack -> Color.Black
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    // Collapsed rail is narrow and tall — a full capsule (same shape as the
    // tab rows/puck) reads as a pill, where the expanded panel's fixed 28dp
    // corner radius would look like a barely-rounded rectangle instead.
    val panelShape = if (collapsed) SideRowShape else SideBarShape
    // The fold is a width animation on the app's largest glass surface:
    // re-capturing and re-blurring the whole screen on every frame of it is what
    // makes the collapse stutter. Hold the last capture until the width settles —
    // the panel is moving, so a frame-old backdrop behind it is not perceptible.
    // Shared with the tab group below, which draws two more backdrops of its own
    // and was re-capturing both through the whole fold.
    val backdropFrozen: () -> Boolean = { panelWidth != targetPanelWidth }
    val panelSurface = if (useGlass) {
        Modifier.liquidGlass(
            config = glassConfig.forSidePanel(),
            shape = panelShape,
            highlightAlpha = 0.3f,
            frozen = backdropFrozen,
        )
    } else {
        Modifier
    }

    val playerThemeColor = LocalDynamicPlayerThemeColor.current

    Column(
        modifier
            .width(panelWidth)
            .shadow(shape = panelShape, elevation = 10.dp)
            .background(backgroundColor, panelShape)
            .clip(panelShape)
            .then(panelSurface)
            .then(
                if (playerThemeColor != null && playerThemeColor.isSpecified) {
                    Modifier.background(playerThemeColor.copy(alpha = 0.08f), panelShape)
                } else {
                    Modifier
                }
            )
            .then(interactiveHighlight.modifier)
            .padding(vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (collapsed) 0.dp else 22.dp, bottom = 10.dp),
            horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.SpaceBetween,
        ) {
            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInHorizontally(tween(180)) { -it / 3 },
                exit = fadeOut(tween(80)),
            ) {
                Text(
                    text = BrandName,
                    fontFamily = rememberBrandFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp,
                    color = glassConfig.textColor,
                )
            }
            Icon(
                painter = painterResource(if (collapsed) R.drawable.chevron_right_px else R.drawable.chevron_leftpx),
                contentDescription = stringResource(
                    if (collapsed) R.string.expand_side_panel else R.string.collapse_side_panel
                ),
                tint = glassConfig.textColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(end = if (collapsed) 0.dp else 16.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleCollapsed),
            )
        }

        // Pinned above the scroll area: the primary tabs stay reachable however
        // far the library list below them is scrolled.
        SideNavTabs(
            navigationItems = navigationItems,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
            useGlass = useGlass,
            backgroundColor = backgroundColor,
            collapsed = collapsed,
            frozen = backdropFrozen,
            modifier = Modifier.padding(horizontal = 6.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                // Rows dissolve into the pinned wordmark and footer instead of
                // being clipped by a hard edge.
                .fadingEdge(vertical = 18.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 6.dp),
        ) {
            // The library sections (playlists, history, ...) need a text label to
            // mean anything — an icon-only rail collapses to just the primary tabs,
            // same as NavigationRail never showing arbitrary link lists either.
            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn(tween(200, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(80)),
            ) {
                Column {
                    sections.forEach { section ->
                        SideBarSectionView(section = section, contentColor = glassConfig.textColor)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        footer(collapsed)
    }
}

/**
 * The phone's floating nav bar, stood on its end — plain glass puck, no gestures.
 *
 * The puck springs to whichever row is selected and nothing else drives it. It
 * also draws UNDER the icons instead of sampling a hidden tinted copy of them, so
 * every row has exactly one icon on screen: sampling icon art through a lens at
 * this row width read as a smeared ghost, not as material.
 */
@Composable
private fun SideNavTabs(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    useGlass: Boolean,
    backgroundColor: Color,
    collapsed: Boolean,
    frozen: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    // Deliberately the global config, not forSidePanel(): the tab group is the
    // phone nav bar stood on its end, so its puck answers to the same "normal
    // liquid glass" settings the phone bar's does. Only the PANEL behind it uses
    // the side-panel tuning.
    val glassConfig = LocalGlassEffectConfig.current
    val appleMusicUi = LocalAppleMusicUi.current

    val selectedContentColor = glassConfig.textColor
    val unselectedContentColor = glassConfig.textColor.copy(alpha = 0.75f)

    // Same wash the phone puck uses: the configured colour, or one that follows
    // the theme rather than assuming a dark bar.
    val puckWash = if (glassConfig.puckColor.isSpecified) {
        glassConfig.puckColor
    } else if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFF2F2F2)
    } else {
        Color(28, 27, 28)
    }
    val puckRestAlpha = glassConfig.puckOpacity.coerceIn(0f, 1f)

    // Puck + tint follow the sticky selection so a non-tab destination holds the
    // last tab instead of snapping to Home; the click passes the REAL route state
    // so tapping a held tab from a sub-screen still navigates to it.
    val selectedRoute = rememberStickySelectedRoute(currentRoute, navigationItems)
    val tabs = navigationItems.map { screen ->
        SideTab(
            screen = screen,
            selected = screen.route == selectedRoute,
            onClick = { onItemClick(screen, isRouteSelected(currentRoute, screen.route, navigationItems)) },
        )
    }
    val tabsCount = tabs.size
    if (tabsCount == 0) return

    val density = LocalDensity.current
    val backdrop = if (useGlass) LocalAppBackdrop.current else null
    val tabHeightPx = with(density) { SideTabHeight.toPx() }

    val hasSelection = tabs.any { it.selected }
    val rawIndex = tabs.indexOfFirst { it.selected }
    val selectedIndex = if (rawIndex >= 0) rawIndex else 0
    val puckAlpha by animateFloatAsState(
        targetValue = if (hasSelection) 1f else 0f,
        animationSpec = tween(150),
        label = "puckAlpha",
    )

    // The puck's position, in tab indices. Nothing drives it but the selection.
    val puckPosition = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(selectedIndex, hasSelection) {
        if (hasSelection) {
            puckPosition.animateTo(selectedIndex.toFloat(), Motion.select())
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(SideTabHeight * tabsCount + SideBarContentPadding.calculateTopPadding() * 2)
    ) {
        // Drawn FIRST at zIndex(-1) so it sits visually and spatially BELOW the
        // clickable Column. graphicsLayer { translationY } only moves the rendering
        // layer — the hit-test rect stays at the layout position. Without zIndex(-1)
        // the puck Box's hit-test area (always at row 0) silently intercepted touches
        // intended for the Column's rows, making every tab except the selected one
        // unresponsive when the puck was on them. zIndex(-1) pushes the puck below
        // the Column in hit-testing too, so all rows receive clicks correctly.
        Box(
            Modifier
                .zIndex(-1f)
                .padding(SideBarContentPadding)
                .graphicsLayer {
                    translationY = puckPosition.value * tabHeightPx
                    alpha = puckAlpha
                }
                .fillMaxWidth()
                .height(SideTabHeight)
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { SideRowShape },
                            effects = {
                                // The panel's own blur, not the global one: the puck
                                // is cut from the panel it sits in, and reading the
                                // shared radius made it a window onto sharper content
                                // than the frost around it.
                                blur(glassConfig.sidePanelBlurRadius.dp.toPx())
                                lens(
                                    32f.dp.toPx(),
                                    28f.dp.toPx(),
                                    chromaticAberration = true,
                                )
                            },
                            highlight = {
                                Highlight.Default.copy(alpha = PuckRestHighlightAlpha)
                            },
                            shadow = { Shadow(alpha = PuckRestShadowAlpha) },
                            onDrawSurface = { drawRect(puckWash.copy(alpha = puckRestAlpha)) },
                            frozen = frozen,
                        )
                    } else {
                        Modifier
                            .shadow(shape = SideRowShape, elevation = 3.dp)
                            .background(backgroundColor.copy(alpha = 0.5f), SideRowShape)
                            .clip(SideRowShape)
                    }
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(SideBarContentPadding),
        ) {
            tabs.forEach { tab ->
                SideTabRow(
                    tab = tab,
                    appleMusicUi = appleMusicUi,
                    contentColor = if (tab.selected) selectedContentColor else unselectedContentColor,
                    alpha = if (tab.selected) 1f else 0.6f,
                    collapsed = collapsed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SideTabHeight)
                        .clip(SideRowShape)
                        .tapClickable(onClick = tab.onClick),
                )
            }
        }
    }
}

/**
 * The now playing pill for tab view: the same [FloatingMiniPlayer] the phone bar
 * docks as its accessory, on the same glass and the same capsule, floating free
 * in the content rather than attached to anything.
 *
 * Place it inside a container that has already padded out [SideBarContentInset]
 * — it fills that container's width by [FloatingMiniPlayerWidthFraction] and
 * centres itself in it, so "centred" means the content area, not the screen.
 */
@Composable
fun BoxWithConstraintsScope.AppFloatingNowPlayingPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    onLyricsClick: (() -> Unit)? = null,
    onQueueClick: (() -> Unit)? = null,
) {
    val glassConfig = LocalGlassEffectConfig.current
    val useGlass = glassConfig.isEnabledFor(GlassComponent.SIDE_PANEL) && isGlassAllowed()
    val pillShape = ContinuousRoundedRectangle(percent = 50)

    val width = maxWidth * FloatingMiniPlayerWidthFraction
    val height = (width * FloatingMiniPlayerAspect)
        .coerceIn(FloatingMiniPlayerMinHeight, FloatingMiniPlayerMaxHeight)

    val background = when {
        useGlass -> Color.Transparent
        pureBlack -> Color.Black
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val surface = if (useGlass) {
        // This is the mini player pill, not the side panel — it uses the
        // global glass settings directly, same as the phone's docked
        // accessory. (forSidePanel() here was a bug: dialing the side panel
        // rail's glass was leaking into this pill too.)
        Modifier.liquidGlass(config = glassConfig, shape = pillShape, highlightAlpha = 0.3f)
    } else {
        Modifier.background(background, pillShape)
    }

    FloatingMiniPlayer(
        isInline = false,
        // See AppNavigation: hardcoded white disappeared on light themes.
        contentColor = if (useGlass) glassConfig.textColor else MaterialTheme.colorScheme.onSurface,
        onClick = onClick,
        onLyricsClick = onLyricsClick,
        onQueueClick = onQueueClick,
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(shape = pillShape, elevation = 10.dp)
            .clip(pillShape)
            .then(surface),
    )
}

/**
 * The pinned bottom row: the account avatar when signed in, otherwise a settings
 * cog. Same destination as the phone top bar's trailing button.
 */
@Composable
fun SideBarAccountRow(
    accountImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapsed: Boolean = false,
) {
    val contentColor = LocalGlassEffectConfig.current.textColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .height(SideTabHeight)
            .clip(SideRowShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier.size(SideRowLeadingSlot),
            contentAlignment = Alignment.Center,
        ) {
            if (accountImageUrl != null) {
                AsyncImage(
                    model = accountImageUrl,
                    contentDescription = stringResource(R.string.account),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.settings),
                    contentDescription = stringResource(R.string.settings),
                    tint = contentColor,
                    modifier = Modifier.size(SideRowIconSize),
                )
            }
        }
        AnimatedVisibility(
            visible = !collapsed,
            enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInHorizontally(tween(180)) { -it / 3 },
            exit = fadeOut(tween(80)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(
                        if (accountImageUrl != null) R.string.account else R.string.settings
                    ),
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SideTabRow(
    tab: SideTab,
    appleMusicUi: Boolean,
    contentColor: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
    collapsed: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.Start,
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .padding(horizontal = if (collapsed) 0.dp else 16.dp),
    ) {
        Box(
            modifier = Modifier.size(SideRowLeadingSlot),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    tab.screen.icon(appleMusicUi)
                ),
                contentDescription = stringResource(tab.screen.titleId),
                tint = contentColor,
                modifier = Modifier.size(SideRowIconSize),
            )
        }
        AnimatedVisibility(
            visible = !collapsed,
            enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInHorizontally(tween(180)) { -it / 3 },
            exit = fadeOut(tween(80)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(tab.screen.titleId),
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SideBarSectionView(
    section: SideBarSection,
    contentColor: Color,
) {
    if (section.links.isEmpty()) return

    if (section.isCollapsible && section.title != null) {
        var isExpanded by rememberSaveable(section.title) {
            mutableStateOf(!section.defaultCollapsed)
        }
        val chevronRotation by animateFloatAsState(
            targetValue = if (isExpanded) 90f else 0f,
            animationSpec = Motion.select(),
            label = "sectionChevronRotation",
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(SideRowShape)
                .clickable { isExpanded = !isExpanded }
                .padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.6f),
            )
            Icon(
                painter = painterResource(R.drawable.chevron_right_px),
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = chevronRotation },
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(180)) + expandVertically(spring(0.85f, 400f)),
            exit = fadeOut(tween(140)) + shrinkVertically(spring(0.85f, 400f)),
        ) {
            Column {
                section.links.forEach { link ->
                    SideBarLinkRow(link = link, contentColor = contentColor)
                }
            }
        }
    } else {
        section.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 4.dp),
            )
        }
        section.links.forEach { link ->
            SideBarLinkRow(link = link, contentColor = contentColor)
        }
    }
}

@Composable
private fun SideBarLinkRow(link: SideBarLink, contentColor: Color) {
    val isSelected = link.isSelected
    val activeColor = LocalAccentTextColor.current ?: MaterialTheme.colorScheme.primary
    val effectiveColor = if (isSelected) activeColor else contentColor
    val rowBg = if (isSelected) effectiveColor.copy(alpha = 0.12f) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(SideTabHeight)
            .clip(SideRowShape)
            .background(rowBg, SideRowShape)
            .clickable(onClick = link.onClick)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier.size(SideRowLeadingSlot),
            contentAlignment = Alignment.Center,
        ) {
            when {
                link.thumbnailUrl != null -> AsyncImage(
                    model = link.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(ContinuousRoundedRectangle(6.dp)),
                )

                link.iconRes != null -> Icon(
                    painter = painterResource(link.iconRes),
                    contentDescription = null,
                    tint = effectiveColor,
                    modifier = Modifier.size(SideRowIconSize),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = link.label,
            color = effectiveColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Release-triggered [clickable], same as the phone bar's: firing on finger-DOWN
 * navigates even when the touch turns into a puck drag or a scroll.
 */
@Composable
private fun Modifier.tapClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        onClick = onClick,
        indication = null,
        interactionSource = interactionSource,
    )
}
