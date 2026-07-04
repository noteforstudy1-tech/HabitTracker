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
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun MonthNavigationBar(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWeekSelected: (LocalDate) -> Unit
) {
    // Show 12 months centred around today — 6 past + current + 5 future (no year clamping)
    val months = buildList {
        val anchor = YearMonth.now()
        for (i in -6..6) add(anchor.plusMonths(i.toLong()))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        // ── Month strip ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(36.dp)) {
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                months.forEach { month ->
                    val isSelected = month == selectedMonth
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) AccentPurple else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "month_bg"
                    )
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(
                                1.dp,
                                if (isSelected) AccentPurple else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                // Navigate to first week of that month
                                val firstMonday = month.atDay(1)
                                    .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                                onWeekSelected(firstMonday)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Full month name + full 4-digit year so years are never ambiguous
                        val label = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                                    " " + month.year
                        Text(
                            text  = label,
                            fontSize = 11.sp,
                            color = textColor,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // ── Week strip ─────────────────────────────────────────────────────
        WeekNavigationRow(
            selectedMonth   = selectedMonth,
            currentWeekStart = currentWeekStart,
            onWeekSelected  = onWeekSelected
        )
    }
}

@Composable
private fun WeekNavigationRow(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onWeekSelected: (LocalDate) -> Unit
) {
    // All Mondays that have at least one day in the selected month
    val weeks = buildList {
        var monday = selectedMonth.atDay(1)
            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val monthEnd = selectedMonth.atEndOfMonth()
        while (!monday.isAfter(monthEnd)) {
            add(monday)
            monday = monday.plusWeeks(1)
        }
    }

    val fmt = DateTimeFormatter.ofPattern("d MMM")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weeks.forEach { weekStart ->
            val weekEnd    = weekStart.plusDays(6)
            val isSelected = weekStart == currentWeekStart

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) AccentCyan.copy(alpha = 0.20f)
                              else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
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
                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                    .clickable { onWeekSelected(weekStart) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                // Show full dates with year so "Jan 2026" vs "Jan 2027" are never confused
                val startStr = weekStart.format(fmt) + if (weekStart.year != weekEnd.year)
                    " '${weekStart.year.toString().takeLast(2)}" else ""
                val endStr   = weekEnd.format(fmt) + " '${weekEnd.year.toString().takeLast(2)}"
                Text(
                    text  = "$startStr – $endStr",
                    fontSize = 10.sp,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
