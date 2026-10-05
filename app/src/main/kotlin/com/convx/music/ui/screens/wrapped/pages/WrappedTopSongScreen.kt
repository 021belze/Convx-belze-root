/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.wrapped.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.convx.music.R
import com.convx.music.db.entities.SongWithStats
import com.convx.music.ui.screens.wrapped.components.AnimatedDecorativeElement
import kotlin.random.Random

@Composable
fun WrappedTopSongScreen(topSong: SongWithStats?, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            visible = true
        }
    }

    val topStartElements = remember {
        List(3) { Pair(Random.nextInt(0, 100).dp to Random.nextInt(0, 100).dp, Random.nextInt(20, 80).dp) }
    }
    val bottomEndElements = remember {
        List(4) { Pair(Random.nextInt(0, 120).dp to Random.nextInt(0, 120).dp, Random.nextInt(20, 90).dp) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            topStartElements.forEach { (padding, size) ->
                AnimatedDecorativeElement(
                    Modifier.padding(start = padding.first, top = padding.second).size(size),
                    isVisible
                )
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            bottomEndElements.forEach { (padding, size) ->
                AnimatedDecorativeElement(
                    Modifier.padding(end = padding.first, bottom = padding.second).size(size),
                    isVisible
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Category Tag
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(900, delayMillis = 150)) + slideInVertically(animationSpec = tween(900, delayMillis = 150))
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6).copy(alpha = 0.5f), Color(0xFFEC4899).copy(alpha = 0.5f))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.wrapped_top_song_title).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cinematic Album Art with glow & rounded corners
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 350)) + slideInVertically(animationSpec = tween(1000, delayMillis = 350))
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF7C3AED))
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(topSong?.thumbnailUrl)
                            .build(),
                        contentDescription = stringResource(id = R.string.wrapped_top_song_album_art_content_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Song Title
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(900, delayMillis = 550)) + slideInVertically(animationSpec = tween(900, delayMillis = 550))
            ) {
                Text(
                    text = topSong?.title ?: stringResource(id = R.string.wrapped_no_data),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 30.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Artist Name
            if (!topSong?.artistName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(900, delayMillis = 700)) + slideInVertically(animationSpec = tween(900, delayMillis = 700))
                ) {
                    Text(
                        text = topSong?.artistName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Chip
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(900, delayMillis = 850)) + slideInVertically(animationSpec = tween(900, delayMillis = 850))
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val minutes = topSong?.timeListened?.div(60000) ?: 0
                    val count = topSong?.songCountListened ?: 0

                    Text(
                        text = "$minutes min",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (count > 0) {
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "$count plays",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
