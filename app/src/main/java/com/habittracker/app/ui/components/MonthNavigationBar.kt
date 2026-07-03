package com.habittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthNavigationBar(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWeekSelected: (LocalDate) -> Unit
) {
    val months = buildList {
        val now = YearMonth.now()
        for (i in -3..3) {
            add(now.plusMonths(i.toLong()))
        }
    }

    // Glass container at the bottom
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 8.dp)
    ) {
        // Month scroll strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                months.forEach { month ->
                    val isSelected = month == selectedMonth
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "month_bg"
                    )
                    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(10.dp))
                            .clickable { onWeekSelected(month.atDay(1)) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + month.year.toString().takeLast(2),
                            fontSize = 12.sp,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Week navigation within selected month
        WeekNavigationRow(
            selectedMonth = selectedMonth,
            currentWeekStart = currentWeekStart,
            onWeekSelected = onWeekSelected
        )
    }
}

@Composable
private fun WeekNavigationRow(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onWeekSelected: (LocalDate) -> Unit
) {
    // Generate all week starts in the selected month in chronological order
    val weeks = buildList {
        var day = selectedMonth.atDay(1).with(
            java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
        )
        val monthEnd = selectedMonth.atEndOfMonth()
        while (!day.isAfter(monthEnd)) {
            add(day)
            day = day.plusWeeks(1)
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weeks.forEach { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            val isSelected = weekStart == currentWeekStart
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) AccentCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                label = "week_bg"
            )
            val borderCol by animateColorAsState(
                targetValue = if (isSelected) AccentCyan else MaterialTheme.colorScheme.outline,
                label = "week_border"
            )
            val textColor = if (isSelected) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(8.dp))
                    .clickable { onWeekSelected(weekStart) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${weekStart.format(dateFormatter)} – ${weekEnd.format(dateFormatter)}",
                    fontSize = 11.sp,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
