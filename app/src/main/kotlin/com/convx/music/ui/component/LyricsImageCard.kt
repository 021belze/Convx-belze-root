/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import com.convx.music.R
import com.convx.music.models.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class LyricsCardDimensions(
    val coverArtSize: Dp,
    val coverCornerRadius: Dp,
    val padding: Dp,
    val initialFontSize: TextUnit,
    val minFontSize: TextUnit,
    val lineHeightMultiplier: Float
)

@Composable
fun rememberAdjustedFontSize(
    text: String,
    maxWidth: Dp,
    maxHeight: Dp,
    density: Density,
    initialFontSize: TextUnit = 20.sp,
    minFontSize: TextUnit = 14.sp,
    style: TextStyle = TextStyle.Default,
    textMeasurer: androidx.compose.ui.text.TextMeasurer? = null
): TextUnit {
    val measurer = textMeasurer ?: rememberTextMeasurer()

    var calculatedFontSize by remember(text, maxWidth, maxHeight, style, density) {
        val initialSize = when {
            text.length < 50 -> initialFontSize
            text.length < 100 -> (initialFontSize.value * 0.82f).sp
            text.length < 200 -> (initialFontSize.value * 0.65f).sp
            else -> (initialFontSize.value * 0.52f).sp
        }
        mutableStateOf(initialSize)
    }

    LaunchedEffect(key1 = text, key2 = maxWidth, key3 = maxHeight) {
        val targetWidthPx = with(density) { maxWidth.toPx() * 0.94f }
        val targetHeightPx = with(density) { maxHeight.toPx() * 0.94f }
        if (text.isBlank()) {
            calculatedFontSize = minFontSize
            return@LaunchedEffect
        }

        if (text.length < 20) {
            val largerSize = (initialFontSize.value * 1.1f).sp
            val result = measurer.measure(
                text = AnnotatedString(text),
                style = style.copy(fontSize = largerSize)
            )
            if (result.size.width <= targetWidthPx && result.size.height <= targetHeightPx) {
                calculatedFontSize = largerSize
                return@LaunchedEffect
            }
        } else if (text.length < 35) {
            val largerSize = (initialFontSize.value * 0.95f).sp
            val result = measurer.measure(
                text = AnnotatedString(text),
                style = style.copy(fontSize = largerSize)
            )
            if (result.size.width <= targetWidthPx && result.size.height <= targetHeightPx) {
                calculatedFontSize = largerSize
                return@LaunchedEffect
            }
        }

        var minSize = minFontSize.value
        var maxSize = initialFontSize.value
        var bestFit = minSize
        var iterations = 0

        while (minSize <= maxSize && iterations < 20) {
            iterations++
            val midSize = (minSize + maxSize) / 2
            val midSizeSp = midSize.sp

            val result = measurer.measure(
                text = AnnotatedString(text),
                style = style.copy(fontSize = midSizeSp)
            )

            if (result.size.width <= targetWidthPx && result.size.height <= targetHeightPx) {
                bestFit = midSize
                minSize = midSize + 0.5f
            } else {
                maxSize = midSize - 0.5f
            }
        }

        calculatedFontSize = if (bestFit < minFontSize.value) minFontSize else bestFit.sp
    }

    return calculatedFontSize
}

enum class LyricsBackgroundStyle {
    SOLID,
    BLUR,
    GRADIENT
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LyricsImageCard(
    lyricText: String,
    mediaMetadata: MediaMetadata,
    darkBackground: Boolean = true,
    backgroundColor: Color? = null,
    backgroundStyle: LyricsBackgroundStyle = LyricsBackgroundStyle.SOLID,
    textColor: Color? = null,
    secondaryTextColor: Color? = null,
    textAlign: TextAlign = TextAlign.Center
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val lineCount = remember(lyricText) {
        lyricText.lines().filter { it.isNotBlank() }.size.coerceAtLeast(1)
    }

    val dims = remember(lineCount) {
        when {
            lineCount == 1 -> LyricsCardDimensions(
                coverArtSize = 76.dp,
                coverCornerRadius = 16.dp,
                padding = 28.dp,
                initialFontSize = 32.sp,
                minFontSize = 24.sp,
                lineHeightMultiplier = 1.3f
            )
            lineCount <= 3 -> LyricsCardDimensions(
                coverArtSize = 62.dp,
                coverCornerRadius = 14.dp,
                padding = 24.dp,
                initialFontSize = 23.sp,
                minFontSize = 18.sp,
                lineHeightMultiplier = 1.28f
            )
            lineCount <= 6 -> LyricsCardDimensions(
                coverArtSize = 48.dp,
                coverCornerRadius = 12.dp,
                padding = 20.dp,
                initialFontSize = 18.sp,
                minFontSize = 13.sp,
                lineHeightMultiplier = 1.22f
            )
            else -> LyricsCardDimensions(
                coverArtSize = 38.dp,
                coverCornerRadius = 10.dp,
                padding = 16.dp,
                initialFontSize = 15.sp,
                minFontSize = 10.sp,
                lineHeightMultiplier = 1.18f
            )
        }
    }

    val cardCornerRadius = 24.dp
    val defaultBgColor = if (darkBackground) Color(0xFF101014) else Color(0xFFF6F6F8)
    val backgroundSolidColor = backgroundColor ?: defaultBgColor
    
    val mainTextColor = textColor ?: if (darkBackground) Color.White else Color.Black
    val secondaryColor = secondaryTextColor ?: if (darkBackground) Color.White.copy(alpha = 0.72f) else Color.Black.copy(alpha = 0.68f)

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(mediaMetadata.thumbnailUrl)
            .crossfade(false)
            .build()
    )
    
    // Calculate gradient colors if needed
    var gradientBrush by remember { mutableStateOf<Brush?>(null) }
    
    if (backgroundStyle == LyricsBackgroundStyle.GRADIENT) {
        LaunchedEffect(mediaMetadata.thumbnailUrl) {
            withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val req = ImageRequest.Builder(context).data(mediaMetadata.thumbnailUrl).allowHardware(false).build()
                    val result = loader.execute(req)
                    val bmp = result.image?.toBitmap()
                    if (bmp != null) {
                        val palette = Palette.from(bmp).generate()
                        val vibrant = palette.getVibrantColor(defaultBgColor.toArgb())
                        val darkVibrant = palette.getDarkVibrantColor(defaultBgColor.toArgb())
                        
                        val color1 = Color(vibrant)
                        val color2 = Color(darkVibrant)
                        
                        gradientBrush = Brush.linearGradient(
                            colors = listOf(color1, color2),
                            tileMode = TileMode.Clamp
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Main Card Container with rounded corners
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cardCornerRadius))
    ) {
            // Background Layer
            when (backgroundStyle) {
                LyricsBackgroundStyle.SOLID -> {
                    Box(modifier = Modifier.fillMaxSize().background(backgroundSolidColor))
                }
                LyricsBackgroundStyle.BLUR -> {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(70.dp)
                    )
                    // Ambient radial overlay for enhanced contrast and depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                }
                LyricsBackgroundStyle.GRADIENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush ?: Brush.linearGradient(listOf(backgroundSolidColor, backgroundSolidColor)))
                    )
                }
            }

            // Bottom gradient scrim to guarantee footer readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
            

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dims.padding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Cover Art + Title & Artist
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(dims.coverArtSize)
                            .shadow(8.dp, RoundedCornerShape(dims.coverCornerRadius))
                            .clip(RoundedCornerShape(dims.coverCornerRadius))
                            .border(1.dp, mainTextColor.copy(alpha = 0.2f), RoundedCornerShape(dims.coverCornerRadius))
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = mediaMetadata.title,
                            color = mainTextColor,
                            fontSize = if (lineCount <= 3) 19.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = mediaMetadata.artists.joinToString { it.name },
                            color = secondaryColor,
                            fontSize = if (lineCount <= 3) 15.sp else 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Lyrics text container with decorative quote watermark
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = when (textAlign) {
                        TextAlign.Left, TextAlign.Start -> Alignment.CenterStart
                        TextAlign.Right, TextAlign.End -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    val availableWidth = maxWidth
                    val availableHeight = maxHeight
                    val textStyle = TextStyle(
                        color = mainTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign,
                        letterSpacing = 0.005.em,
                    )


                    val textMeasurer = rememberTextMeasurer()
                    val dynamicFontSize = rememberAdjustedFontSize(
                        text = lyricText,
                        maxWidth = (availableWidth - 8.dp).coerceAtLeast(1.dp),
                        maxHeight = (availableHeight - 8.dp).coerceAtLeast(1.dp),
                        density = density,
                        initialFontSize = dims.initialFontSize,
                        minFontSize = dims.minFontSize,
                        style = textStyle,
                        textMeasurer = textMeasurer
                    )


                    Text(
                        text = lyricText,
                        style = textStyle.copy(
                            fontSize = dynamicFontSize,
                            lineHeight = dynamicFontSize.value.sp * dims.lineHeightMultiplier
                        ),
                        overflow = TextOverflow.Ellipsis,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Footer Pill with Frosted Glass Look
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(mainTextColor.copy(alpha = 0.09f))
                            .border(0.5.dp, mainTextColor.copy(alpha = 0.18f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.convx_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = context.getString(R.string.app_name),
                            color = mainTextColor.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
    }
}
