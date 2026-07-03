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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HabitGridHeader(
    weekDates: List<LocalDate>,
    today: LocalDate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pinned title taking weight 0.32f
        Box(
            modifier = Modifier
                .weight(0.32f)
                .padding(start = 12.dp, end = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Habit",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // 7 day headers taking weight 0.68f combined
        Row(
            modifier = Modifier.weight(0.68f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDates.forEach { date ->
                val isToday = date == today
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DayHeaderCell(date = date, isToday = isToday)
                }
            }
        }
    }
}

@Composable
fun DayHeaderCell(date: LocalDate, isToday: Boolean) {
    val dayLetter = date.format(DateTimeFormatter.ofPattern("EEE")).take(1)
    val dayNum = date.dayOfMonth.toString()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayLetter,
            fontSize = 9.sp,
            color = if (isToday) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .then(
                    if (isToday) Modifier.background(AccentPurple.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNum,
                fontSize = 10.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HabitRow(
    habit: Habit,
    weekDates: List<LocalDate>,
    isCompleted: (LocalDate) -> Boolean,
    onToggle: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    isScheduled: (LocalDate) -> Boolean,
    today: LocalDate
) {
    val haptic = LocalHapticFeedback.current
    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        AccentPurple
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pinned habit name column: weight 0.32f
        Row(
            modifier = Modifier
                .weight(0.32f)
                .clickable { onEditClick() }
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, RoundedCornerShape(3.dp))
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

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // Checkbox cells: weight 0.68f combined
        Row(
            modifier = Modifier.weight(0.68f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDates.forEach { date ->
                val done = isCompleted(date)
                val isFuture = date.isAfter(today)
                val scheduled = isScheduled(date)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CheckboxCell(
                        checked = done,
                        isFuture = isFuture,
                        isScheduled = scheduled,
                        accentColor = accentColor,
                        onClick = {
                            if (!isFuture && scheduled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggle(date)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CheckboxCell(
    checked: Boolean,
    isFuture: Boolean,
    isScheduled: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            !isScheduled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
            checked -> accentColor.copy(alpha = 0.3f)
            isFuture -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cell_bg"
    )
    val borderCol by animateColorAsState(
        targetValue = when {
            !isScheduled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            checked -> accentColor
            isFuture -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.outline
        },
        label = "cell_border"
    )

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(enabled = !isFuture && isScheduled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!isScheduled) {
            Text(
                text = "–",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
