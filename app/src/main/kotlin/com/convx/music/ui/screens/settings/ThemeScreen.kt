package com.convx.music.ui.screens.settings

import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.utils.appTopBarWindowInsets
import android.content.res.Configuration
import com.convx.music.ui.utils.appTopBarWindowInsets
import android.os.Build
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.AnimatedVisibility
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.core.Spring
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.core.animateDpAsState
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.core.animateFloatAsState
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.core.spring
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.core.tween
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.fadeIn
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.fadeOut
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.scaleIn
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.animation.scaleOut
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.BorderStroke
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.Canvas
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.background
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.utils.bounceClick
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.utils.combinedBounceClick
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.isSystemInDarkTheme
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.Arrangement
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.Box
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.Column
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.Row
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.Spacer
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.aspectRatio
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.height
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.heightIn
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.padding
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.size
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.layout.width
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.lazy.LazyRow
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.lazy.items
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.rememberScrollState
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.shape.CircleShape
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.foundation.verticalScroll
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.Card
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.CardDefaults
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.Icon
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.IconButton
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.MaterialTheme
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.Text
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.TextButton
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.TopAppBar
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.dynamicDarkColorScheme
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.dynamicLightColorScheme
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.material3.ripple
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.runtime.Composable
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.runtime.getValue
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.runtime.mutableStateOf
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.runtime.remember
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.runtime.setValue
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.component.ColorPickerDialog
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.Alignment
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.Modifier
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.draw.clip
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.geometry.Offset
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.geometry.Size
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.graphics.Color
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.graphics.graphicsLayer
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.graphics.toArgb
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.platform.LocalConfiguration
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.platform.LocalContext
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.res.painterResource
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.res.stringResource
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.semantics.contentDescription
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.semantics.semantics
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.compose.ui.unit.dp
import com.convx.music.ui.utils.appTopBarWindowInsets
import androidx.navigation.NavController
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.materialkolor.PaletteStyle
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.materialkolor.rememberDynamicColorScheme
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.R
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.constants.DarkModeKey
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.constants.DynamicThemeKey
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.constants.PureBlackKey
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.constants.PureBlackMiniPlayerKey
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.constants.SelectedThemeColorKey
import com.convx.music.constants.AppBackgroundColorKey
import com.convx.music.constants.AppTextColorKey
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.theme.AppleTokens
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.theme.DefaultThemeColor
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.theme.vivimusicTheme
import com.convx.music.ui.theme.ConvxThemePresets
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.text.font.FontWeight
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.utils.rememberEnumPreference
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.utils.rememberPreference
import androidx.compose.foundation.layout.windowInsetsPadding
import com.convx.music.LocalPlayerAwareWindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    navController: NavController,
) {
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, DarkMode.AUTO)
    val (pureBlack, onPureBlackChangeRaw) = rememberPreference(PureBlackKey, defaultValue = false)
    val (_, onPureBlackMiniPlayerChange) = rememberPreference(
        PureBlackMiniPlayerKey,
        defaultValue = false
    )

    val onPureBlackChange: (Boolean) -> Unit = { enabled ->
        onPureBlackChangeRaw(enabled)
        onPureBlackMiniPlayerChange(enabled)
    }
    val (selectedThemeColorInt, onSelectedThemeColorChange) = rememberPreference(
        SelectedThemeColorKey,
        DefaultThemeColor.toArgb()
    )
    val (_, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)

    val (backgroundColorInt, onBackgroundColorChange) = rememberPreference(AppBackgroundColorKey, defaultValue = 0)
    val (textColorInt, onTextColorChange) = rememberPreference(AppTextColorKey, defaultValue = 0)

    val selectedThemeColor = Color(selectedThemeColorInt)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Helper function to handle color selection with dynamic theme toggle
    val handleColorSelection: (Color) -> Unit = { color ->
        onSelectedThemeColorChange(color.toArgb())
        val isDynamicColor = color == DefaultThemeColor
        onDynamicThemeChange(isDynamicColor)
    }

    val onThemeModeSelect: (DarkMode, Boolean) -> Unit = { newMode, newPureBlack ->
        onDarkModeChange(newMode)
        onPureBlackChange(newPureBlack)
        when {
            newMode == DarkMode.AUTO -> {
                onBackgroundColorChange(0)
                onTextColorChange(0)
            }
            newMode == DarkMode.OFF -> {
                onBackgroundColorChange(ConvxThemePresets.LightBackground.toArgb())
                onTextColorChange(ConvxThemePresets.LightText.toArgb())
            }
            newMode == DarkMode.ON && !newPureBlack -> {
                onBackgroundColorChange(ConvxThemePresets.DarkBackground.toArgb())
                onTextColorChange(ConvxThemePresets.DarkText.toArgb())
            }
            newMode == DarkMode.ON && newPureBlack -> {
                onBackgroundColorChange(ConvxThemePresets.PureBlackBackground.toArgb())
                onTextColorChange(ConvxThemePresets.PureBlackText.toArgb())
            }
        }
    }

    val onResetToConvxDefault: () -> Unit = {
        when {
            darkMode == DarkMode.AUTO -> {
                onBackgroundColorChange(0)
                onTextColorChange(0)
            }
            darkMode == DarkMode.OFF -> {
                onBackgroundColorChange(ConvxThemePresets.LightBackground.toArgb())
                onTextColorChange(ConvxThemePresets.LightText.toArgb())
            }
            darkMode == DarkMode.ON && !pureBlack -> {
                onBackgroundColorChange(ConvxThemePresets.DarkBackground.toArgb())
                onTextColorChange(ConvxThemePresets.DarkText.toArgb())
            }
            darkMode == DarkMode.ON && pureBlack -> {
                onBackgroundColorChange(ConvxThemePresets.PureBlackBackground.toArgb())
                onTextColorChange(ConvxThemePresets.PureBlackText.toArgb())
            }
        }
    }

    val onReset: () -> Unit = {
        onDarkModeChange(DarkMode.AUTO)
        onPureBlackChange(false)
        onSelectedThemeColorChange(DefaultThemeColor.toArgb())
        onDynamicThemeChange(true)
        onBackgroundColorChange(0)
        onTextColorChange(0)
    }

    if (isLandscape) {
        LandscapeThemeLayout(
            darkMode = darkMode,
            pureBlack = pureBlack,
            onThemeModeSelect = onThemeModeSelect,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = handleColorSelection,
            backgroundColorInt = backgroundColorInt,
            onBackgroundColorChange = onBackgroundColorChange,
            textColorInt = textColorInt,
            onTextColorChange = onTextColorChange,
            onResetToConvxDefault = onResetToConvxDefault,
            onReset = onReset,
        )
    } else {
        PortraitThemeLayout(
            darkMode = darkMode,
            pureBlack = pureBlack,
            onThemeModeSelect = onThemeModeSelect,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = handleColorSelection,
            backgroundColorInt = backgroundColorInt,
            onBackgroundColorChange = onBackgroundColorChange,
            textColorInt = textColorInt,
            onTextColorChange = onTextColorChange,
            onResetToConvxDefault = onResetToConvxDefault,
            onReset = onReset,
        )
    }

    TopAppBar(
            windowInsets = appTopBarWindowInsets(),
        title = { Text(stringResource(R.string.theme_colors)) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        }
    )
}

@Composable
fun PortraitThemeLayout(
    darkMode: DarkMode,
    pureBlack: Boolean,
    onThemeModeSelect: (DarkMode, Boolean) -> Unit,
    selectedThemeColor: Color,
    onSelectedThemeColorChange: (Color) -> Unit,
    backgroundColorInt: Int,
    onBackgroundColorChange: (Int) -> Unit,
    textColorInt: Int,
    onTextColorChange: (Int) -> Unit,
    onResetToConvxDefault: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeMockupPortrait(
                darkMode = darkMode,
                pureBlack = pureBlack,
                themeColor = selectedThemeColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ThemeControls(
            darkMode = darkMode,
            pureBlack = pureBlack,
            onThemeModeSelect = onThemeModeSelect,
            backgroundColorInt = backgroundColorInt,
            onBackgroundColorChange = onBackgroundColorChange,
            textColorInt = textColorInt,
            onTextColorChange = onTextColorChange,
            onResetToConvxDefault = onResetToConvxDefault,
            onReset = onReset,
        )

        Spacer(modifier = Modifier.height(16.dp))

        HomeBackgroundControls()

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun LandscapeThemeLayout(
    darkMode: DarkMode,
    pureBlack: Boolean,
    onThemeModeSelect: (DarkMode, Boolean) -> Unit,
    selectedThemeColor: Color,
    onSelectedThemeColorChange: (Color) -> Unit,
    backgroundColorInt: Int,
    onBackgroundColorChange: (Int) -> Unit,
    textColorInt: Int,
    onTextColorChange: (Int) -> Unit,
    onResetToConvxDefault: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(max = 300.dp),
                contentAlignment = Alignment.Center
            ) {
                ThemeMockup(
                    darkMode = darkMode,
                    pureBlack = pureBlack,
                    themeColor = selectedThemeColor
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            ThemeControls(
                darkMode = darkMode,
                pureBlack = pureBlack,
                onThemeModeSelect = onThemeModeSelect,
                backgroundColorInt = backgroundColorInt,
                onBackgroundColorChange = onBackgroundColorChange,
                textColorInt = textColorInt,
                onTextColorChange = onTextColorChange,
                onResetToConvxDefault = onResetToConvxDefault,
                onReset = onReset,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeBackgroundControls()

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ThemeControls(
    darkMode: DarkMode,
    pureBlack: Boolean,
    onThemeModeSelect: (DarkMode, Boolean) -> Unit,
    backgroundColorInt: Int,
    onBackgroundColorChange: (Int) -> Unit,
    textColorInt: Int,
    onTextColorChange: (Int) -> Unit,
    onResetToConvxDefault: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // System mode (AUTO)
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.AUTO,
                        targetPureBlack = pureBlack,
                        onClick = {
                            onThemeModeSelect(DarkMode.AUTO, false)
                        },
                        showIcon = true
                    )
                    
                    // Vertical divider to separate System from manual modes
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    
                    // Manual modes (Light, Dark, Pure Black)
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.OFF,
                        targetPureBlack = false,
                        onClick = {
                            onThemeModeSelect(DarkMode.OFF, false)
                        },
                        showIcon = false
                    )
                    
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.ON,
                        targetPureBlack = false,
                        onClick = {
                            onThemeModeSelect(DarkMode.ON, false)
                        },
                        showIcon = false
                    )
                    
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.ON,
                        targetPureBlack = true,
                        onClick = {
                            onThemeModeSelect(DarkMode.ON, true)
                        },
                        showIcon = false
                    )
                }
            }

            AppBackgroundTextColorSection(
                darkMode = darkMode,
                pureBlack = pureBlack,
                backgroundColorInt = backgroundColorInt,
                onBackgroundColorChange = onBackgroundColorChange,
                textColorInt = textColorInt,
                onTextColorChange = onTextColorChange,
                onResetToConvxDefault = onResetToConvxDefault,
            )

            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.reset))
            }
        }
    }
}

/**
 * App-wide background and text color controls.
 * Automatically configured with Convx's signature aesthetic palettes when switching modes,
 * while allowing full user customization and curated presets.
 */
@Composable
private fun AppBackgroundTextColorSection(
    darkMode: DarkMode,
    pureBlack: Boolean,
    backgroundColorInt: Int,
    onBackgroundColorChange: (Int) -> Unit,
    textColorInt: Int,
    onTextColorChange: (Int) -> Unit,
    onResetToConvxDefault: () -> Unit,
) {
    val convxPreset = when {
        darkMode == DarkMode.OFF -> ConvxThemePresets.LightBackground to ConvxThemePresets.LightText
        darkMode == DarkMode.ON && pureBlack -> ConvxThemePresets.PureBlackBackground to ConvxThemePresets.PureBlackText
        darkMode == DarkMode.ON -> ConvxThemePresets.DarkBackground to ConvxThemePresets.DarkText
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor = if (backgroundColorInt == 0) convxPreset.first else Color(backgroundColorInt)
    val textColor = if (textColorInt == 0) convxPreset.second else Color(textColorInt)

    val isCurrentConvxDefault = (backgroundColorInt == 0 || backgroundColorInt == convxPreset.first.toArgb()) &&
            (textColorInt == 0 || textColorInt == convxPreset.second.toArgb())

    var showBackgroundPicker by remember { mutableStateOf(false) }
    var showTextPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_background_text_color),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.app_background_text_color_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isCurrentConvxDefault) {
                FilledTonalButton(
                    onClick = onResetToConvxDefault,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.refresh),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.convx_default),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppColorSwatchCard(
                label = stringResource(R.string.background_color),
                color = backgroundColor,
                isCustom = backgroundColorInt != 0 && backgroundColorInt != convxPreset.first.toArgb(),
                onClick = { showBackgroundPicker = true },
                modifier = Modifier.weight(1f),
            )
            AppColorSwatchCard(
                label = stringResource(R.string.text_color),
                color = textColor,
                isCustom = textColorInt != 0 && textColorInt != convxPreset.second.toArgb(),
                onClick = { showTextPicker = true },
                modifier = Modifier.weight(1f),
            )
        }

        // Quick Presets Row
        Text(
            text = stringResource(R.string.presets),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SuggestionChip(
                onClick = onResetToConvxDefault,
                label = { Text(stringResource(R.string.convx_default)) },
                icon = {
                    if (isCurrentConvxDefault) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            if (darkMode == DarkMode.OFF) {
                ThemePresetChip(
                    name = "Clean Frost",
                    bg = Color(0xFFF6F7F9),
                    text = Color(0xFF1C1C1E),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
                ThemePresetChip(
                    name = "Warm Ivory",
                    bg = Color(0xFFFBF9F5),
                    text = Color(0xFF2B2625),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
                ThemePresetChip(
                    name = "Pure White",
                    bg = Color(0xFFFFFFFF),
                    text = Color(0xFF111111),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
            } else {
                ThemePresetChip(
                    name = "Midnight Slate",
                    bg = Color(0xFF121214),
                    text = Color(0xFFFFFFFF),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
                ThemePresetChip(
                    name = "OLED Pitch",
                    bg = Color(0xFF000000),
                    text = Color(0xFFFFFFFF),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
                ThemePresetChip(
                    name = "Deep Navy",
                    bg = Color(0xFF0D1117),
                    text = Color(0xFFE6EDF3),
                    currentBg = backgroundColor,
                    currentText = textColor,
                    onSelect = { bg, tx ->
                        onBackgroundColorChange(bg.toArgb())
                        onTextColorChange(tx.toArgb())
                    }
                )
            }
        }
    }

    if (showBackgroundPicker) {
        ColorPickerDialog(
            initialColor = backgroundColor,
            title = stringResource(R.string.background_color),
            defaultColor = convxPreset.first,
            onDismiss = { showBackgroundPicker = false },
            onConfirm = {
                onBackgroundColorChange(it.toArgb())
                showBackgroundPicker = false
            },
            onReset = {
                onBackgroundColorChange(convxPreset.first.toArgb())
                showBackgroundPicker = false
            }
        )
    }

    if (showTextPicker) {
        ColorPickerDialog(
            initialColor = textColor,
            title = stringResource(R.string.text_color),
            defaultColor = convxPreset.second,
            onDismiss = { showTextPicker = false },
            onConfirm = {
                onTextColorChange(it.toArgb())
                showTextPicker = false
            },
            onReset = {
                onTextColorChange(convxPreset.second.toArgb())
                showTextPicker = false
            }
        )
    }
}

@Composable
private fun AppColorSwatchCard(
    label: String,
    color: Color,
    isCustom: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (isCustom) "Custom" else "Convx Preset",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThemePresetChip(
    name: String,
    bg: Color,
    text: Color,
    currentBg: Color,
    currentText: Color,
    onSelect: (Color, Color) -> Unit,
) {
    val isSelected = currentBg == bg && currentText == text
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(bg, text) },
        label = { Text(name) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, text.copy(alpha = 0.5f), CircleShape)
            )
        }
    )
}

@Composable
fun ModeCircle(
    darkMode: DarkMode,
    pureBlack: Boolean,
    targetMode: DarkMode,
    targetPureBlack: Boolean,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val isSelected = darkMode == targetMode && pureBlack == targetPureBlack
    
    val effectiveDark = when (targetMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }
    
    // Use actual system colors for AUTO mode on Android 12+
    val modeColorScheme = if (targetMode == DarkMode.AUTO && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = DefaultThemeColor,
            isDark = effectiveDark,
            style = PaletteStyle.TonalSpot
        )
    }
    
    val fillColor = when {
        targetPureBlack -> Color.Black
        effectiveDark -> modeColorScheme.surface
        else -> modeColorScheme.surface
    }
    
    // Animated border width
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "borderWidth"
    )
    
    // Animated scale for the entire circle
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    val contentDesc = when {
        targetPureBlack -> stringResource(R.string.cd_pure_black_mode)
        targetMode == DarkMode.OFF -> stringResource(R.string.cd_light_mode)
        targetMode == DarkMode.ON -> stringResource(R.string.cd_dark_mode)
        else -> stringResource(R.string.cd_system_mode)
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fillColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .bounceClick(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .semantics {
                contentDescription = contentDesc
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            showIcon -> {
                Icon(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = null,
                    tint = modeColorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            isSelected -> {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                        initialScale = 0.3f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(
                        targetScale = 0.3f,
                        animationSpec = tween(150)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeMockup(
    darkMode: DarkMode,
    pureBlack: Boolean,
    themeColor: Color
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDark = when (darkMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }

    vivimusicTheme(
        darkTheme = useDark,
        pureBlack = pureBlack,
        themeColor = themeColor
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(9f / 18f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(6.dp))
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeMockupPortrait(
    darkMode: DarkMode,
    pureBlack: Boolean,
    themeColor: Color
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDark = when (darkMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }

    vivimusicTheme(
        darkTheme = useDark,
        pureBlack = pureBlack,
        themeColor = themeColor
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (20% of height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                    }
                }

                // Main Content (60% of height)
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                        )
                    }
                }

                // FAB Area (20% of height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .padding(6.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    )
                }
            }
        }
    }
}
