package com.habittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val moodLabels  = listOf("Awful", "Bad", "Okay", "Good", "Great")
private val moodEmojis  = listOf("😞", "😕", "😐", "🙂", "😄")

@Composable
fun WellnessSection(
    wellness: WellnessEntry?,
    selectedDate: LocalDate,
    onMoodChange: (Int) -> Unit,
    onSleepChange: (Float) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dateLabel = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overall Wellness",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Check-in for $dateLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            // ── Mood Row ──────────────────────────────────────────────────────────
            Text(
                text = "Mood",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moodEmojis.forEachIndexed { index, emoji ->
                    MoodChip(
                        emoji = emoji,
                        label = moodLabels[index],
                        color = MoodColors[index],
                        selected = wellness?.moodIndex == index,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMoodChange(index)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Sleep Row ─────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sleep: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${wellness?.sleepHours ?: 7.0f} hrs",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Sleep Selector Grid (4.0h to 12.0h in 0.5h steps)
            val sleepOptions = (8..24).map { it * 0.5f }
            SleepSelector(
                options = sleepOptions,
                selected = wellness?.sleepHours ?: 7.0f,
                onSelect = { hour ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSleepChange(hour)
                }
            )
        }
    }
}

@Composable
private fun MoodChip(
    emoji: String,
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "mood_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.outline,
        label = "mood_border"
    )
    val scale = if (selected) 1.05f else 1.0f

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SleepSelector(
    options: List<Float>,
    selected: Float,
    onSelect: (Float) -> Unit
) {
    val rows = options.chunked(6)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowOptions.forEach { hour ->
                    val isSelected = selected == hour
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) AccentCyan.copy(0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        label = "sleep_bg"
                    )
                    val borderCol by animateColorAsState(
                        targetValue = if (isSelected) AccentCyan else MaterialTheme.colorScheme.outline,
                        label = "sleep_border"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(8.dp))
                            .clickable { onSelect(hour) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (hour == hour.toInt().toFloat()) "${hour.toInt()}h" else "${hour}h"
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = if (isSelected) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
