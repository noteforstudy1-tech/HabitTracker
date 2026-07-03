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
        for (i in -3..3) add(now.plusMonths(i.toLong()))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark)
    ) {
        // Month scroll strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = TextSecondary)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                months.forEach { month ->
                    val isSelected = month == selectedMonth
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) AccentPurple else SurfaceVariantDark,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "month_bg"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AccentPurple else BorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onWeekSelected(month.atDay(1)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            fontSize = 12.sp,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = TextSecondary)
            }
        }

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
    // Generate all week starts in the selected month
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weeks.forEach { weekStart ->
            val weekEnd = weekStart.plusDays(6)
            val isSelected = weekStart == currentWeekStart
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) AccentCyan.copy(alpha = 0.15f) else SurfaceDark,
                label = "week_bg"
            )
            val borderCol by animateColorAsState(
                targetValue = if (isSelected) AccentCyan else BorderColor,
                label = "week_border"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
                    .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                    .clickable { onWeekSelected(weekStart) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${weekStart.dayOfMonth}–${weekEnd.dayOfMonth}",
                    fontSize = 11.sp,
                    color = if (isSelected) AccentCyan else TextSecondary
                )
            }
        }
    }
}
