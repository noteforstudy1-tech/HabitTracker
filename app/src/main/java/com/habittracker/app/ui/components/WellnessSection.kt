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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.ui.theme.*

private val moodLabels  = listOf("Awful", "Bad", "Okay", "Good", "Great")
private val moodEmojis  = listOf("😞", "😕", "😐", "🙂", "😄")

@Composable
fun WellnessSection(
    wellness: WellnessEntry?,
    onMoodChange: (Int) -> Unit,
    onSleepChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Overall Wellness",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = "Today's check-in",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        // ── Mood Row ──────────────────────────────────────────────────────────
        Text("Mood", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            moodEmojis.forEachIndexed { index, emoji ->
                MoodChip(
                    emoji = emoji,
                    label = moodLabels[index],
                    color = MoodColors[index],
                    selected = wellness?.moodIndex == index,
                    onClick = { onMoodChange(index) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Sleep Row ─────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Sleep", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${wellness?.sleepHours ?: 7f}h",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
        }

        Spacer(Modifier.height(10.dp))

        // Sleep selector: 4h to 12h in 0.5h steps shown as chips
        val sleepOptions = (8..24).map { it * 0.5f } // 4.0 to 12.0
        SleepSelector(
            options = sleepOptions,
            selected = wellness?.sleepHours ?: 7f,
            onSelect = onSleepChange
        )
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
        targetValue = if (selected) color.copy(alpha = 0.25f) else SurfaceVariantDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "mood_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) color else BorderColor,
        label = "mood_border"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (selected) color else TextHint
        )
    }
}

@Composable
private fun SleepSelector(
    options: List<Float>,
    selected: Float,
    onSelect: (Float) -> Unit
) {
    // Show nicely: 5.0, 5.5, 6.0 ... 12.0
    val displayOptions = (8..24).map { it * 0.5f }
    val rows = displayOptions.chunked(6)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowOptions.forEach { hour ->
                    val isSelected = selected == hour
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) AccentCyan.copy(0.25f) else SurfaceVariantDark,
                        label = "sleep_bg"
                    )
                    val borderCol by animateColorAsState(
                        targetValue = if (isSelected) AccentCyan else BorderColor,
                        label = "sleep_border"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                            .clickable { onSelect(hour) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (hour == hour.toLong().toFloat()) "${hour.toInt()}h" else "${hour}h"
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
