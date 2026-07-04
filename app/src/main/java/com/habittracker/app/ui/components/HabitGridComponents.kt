package com.habittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Grid Header ─────────────────────────────────────────────────────────────

@Composable
fun HabitGridHeader(
    weekDates: List<LocalDate>,
    today: LocalDate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit name column header
        Box(
            modifier = Modifier
                .weight(0.30f)
                .padding(start = 12.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Habit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 7 day columns
        Row(modifier = Modifier.weight(0.70f)) {
            weekDates.forEach { date ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DayHeaderCell(date = date, isToday = date == today)
                }
            }
        }
    }
}

@Composable
private fun DayHeaderCell(date: LocalDate, isToday: Boolean) {
    val dayLetter = date.format(DateTimeFormatter.ofPattern("EEE")).take(1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = dayLetter,
            fontSize = 11.sp,
            color = if (isToday) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ── Habit Row ────────────────────────────────────────────────────────────────

@Composable
fun HabitRow(
    habit: Habit,
    weekDates: List<LocalDate>,
    isCompleted: (LocalDate) -> Boolean,
    onToggle: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    isScheduled: (LocalDate) -> Boolean,
    today: LocalDate,
    hapticsEnabled: Boolean,
    compactMode: Boolean
) {
    val accentColor = remember(habit.colorHex) {
        try { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        catch (e: Exception) { AccentPurple }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit name — tappable to open edit dialog
        Row(
            modifier = Modifier
                .weight(0.30f)
                .clickable(onClick = onEditClick)
                .padding(horizontal = 10.dp, vertical = if (compactMode) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(accentColor, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = habit.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(48.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // 7 day checkbox cells
        Row(modifier = Modifier.weight(0.70f)) {
            weekDates.forEach { date ->
                val done      = isCompleted(date)
                val isToday   = date == today
                val future    = date.isAfter(today)
                val scheduled = isScheduled(date)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CheckboxCell(
                        checked     = done,
                        isToday     = isToday,
                        isFuture    = future,
                        isScheduled = scheduled,
                        accentColor = accentColor,
                        hapticsEnabled = hapticsEnabled,
                        compactMode    = compactMode,
                        onClick     = {
                            if (isToday && scheduled) onToggle(date)
                        }
                    )
                }
            }
        }
    }
}

// ── Checkbox Cell ────────────────────────────────────────────────────────────

@Composable
fun CheckboxCell(
    checked: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    isScheduled: Boolean,
    accentColor: Color,
    hapticsEnabled: Boolean,
    compactMode: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = when {
            !isScheduled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.04f)
            checked      -> accentColor.copy(alpha = 0.25f)
            isFuture     -> Color.Transparent
            else         -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cell_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !isScheduled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            checked      -> accentColor
            isFuture     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            else         -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cell_border"
    )
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.0f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cell_scale"
    )

    Box(
        modifier = Modifier
            .size(if (compactMode) 38.dp else 44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(bounded = true, color = accentColor), // ripple for tactile feedback
                enabled           = isToday && isScheduled,
                onClick           = {
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compactMode) 28.dp else 34.dp)
                .scale(scale)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                !isScheduled -> Text(
                    text  = "–",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    fontSize = 12.sp
                )
                checked -> Icon(
                    imageVector      = Icons.Default.Check,
                    contentDescription = "Done",
                    tint             = accentColor,
                    modifier         = Modifier.size(16.dp)
                )
            }
        }
    }
}
