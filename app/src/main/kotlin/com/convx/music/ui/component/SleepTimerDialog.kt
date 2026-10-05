/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.convx.music.LocalPlayerConnection
import com.convx.music.R
import com.convx.music.utils.makeTimeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val sleepTimer = playerConnection.service.sleepTimer
    val haptic = LocalHapticFeedback.current

    val isTimerActive = remember(sleepTimer.triggerTime, sleepTimer.pauseWhenSongEnd) {
        sleepTimer.isActive
    }

    var timeLeftMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (isActive) {
                timeLeftMillis = if (sleepTimer.pauseWhenSongEnd) {
                    (playerConnection.player.duration - playerConnection.player.currentPosition).coerceAtLeast(0L)
                } else {
                    (sleepTimer.triggerTime - System.currentTimeMillis()).coerceAtLeast(0L)
                }
                delay(1000L)
            }
        }
    }

    var selectedMinutes by rememberSaveable { mutableFloatStateOf(30f) }
    var isEndOfSongMode by rememberSaveable { mutableStateOf(false) }

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = Modifier.animateContentSize(),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTimerActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.bedtime),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isTimerActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.sleep_timer),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        buttons = {
            if (isTimerActive) {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        sleepTimer.clear()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.sleep_timer_cancel))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            } else {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(stringResource(android.R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isEndOfSongMode) {
                            sleepTimer.start(-1)
                        } else {
                            sleepTimer.start(selectedMinutes.roundToInt())
                        }
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.sleep_timer_start))
                }
            }
        }
    ) {
        if (isTimerActive) {
            // ── Active Timer View ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (sleepTimer.pauseWhenSongEnd) {
                            Text(
                                text = stringResource(R.string.sleep_timer_end_of_track),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.sleep_timer_stop_in, makeTimeString(timeLeftMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = makeTimeString(timeLeftMillis),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            val stopTimeStr = remember(sleepTimer.triggerTime) {
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                timeFormat.format(Date(sleepTimer.triggerTime))
                            }
                            Text(
                                text = stringResource(R.string.sleep_timer_stop_in, stopTimeStr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Quick extend buttons when timed timer is running
                if (!sleepTimer.pauseWhenSongEnd) {
                    Text(
                        text = stringResource(R.string.sleep_timer_active),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        val addMinutesList = listOf(5, 15, 30)
                        addMinutesList.forEach { extraMins ->
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val currentRemMinutes = ((sleepTimer.triggerTime - System.currentTimeMillis()) / 60000L)
                                        .toInt()
                                        .coerceAtLeast(0)
                                    sleepTimer.start(currentRemMinutes + extraMins)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text(
                                    text = stringResource(R.string.sleep_timer_extend, extraMins),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ── Inactive / Setup Timer View ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sleep_timer_select_duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick preset chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isEndOfSongMode,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isEndOfSongMode = true
                        },
                        label = { Text(stringResource(R.string.end_of_song)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    val presets = listOf(15, 30, 45, 60, 90)
                    presets.forEach { preset ->
                        val isSelected = !isEndOfSongMode && selectedMinutes.roundToInt() == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isEndOfSongMode = false
                                selectedMinutes = preset.toFloat()
                            },
                            label = { Text("$preset m") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Duration indicator
                AnimatedContent(
                    targetState = isEndOfSongMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TimerDurationLabel"
                ) { endOfSong ->
                    if (endOfSong) {
                        Text(
                            text = stringResource(R.string.sleep_timer_end_of_track),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = pluralStringResource(
                                R.plurals.minute,
                                selectedMinutes.roundToInt(),
                                selectedMinutes.roundToInt()
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom slider
                Slider(
                    value = selectedMinutes,
                    onValueChange = {
                        isEndOfSongMode = false
                        selectedMinutes = it
                    },
                    valueRange = 5f..120f,
                    steps = (120 - 5) / 5 - 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
