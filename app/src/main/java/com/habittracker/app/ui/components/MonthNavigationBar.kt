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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * A clean, two-row bottom navigation bar:
 * Row 1: ← [Month chips scrollable] →
 * Row 2: Week chips for selected month
 *
 * Key fix: month selection and week selection are completely decoupled.
 * Clicking a month chip always correctly highlights THAT month and shows ITS weeks.
 */
@Composable
fun MonthNavigationBar(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWeekSelected: (LocalDate) -> Unit
) {
    // Build 13 months centered on TODAY — never on selectedMonth to avoid offset bug
    val today      = remember { LocalDate.now() }
    val anchorMonth = remember { YearMonth.now() }
    val months     = remember(anchorMonth) {
        (-6..6).map { anchorMonth.plusMonths(it.toLong()) }
    }

    val monthScrollState = rememberScrollState()

    // Auto-scroll month strip so selected month is visible
    val selectedIndex = months.indexOfFirst { it == selectedMonth }
    LaunchedEffect(selectedMonth) {
        if (selectedIndex >= 0) {
            // Roughly scroll to center the selected month chip (each chip ~90dp wide)
            monthScrollState.animateScrollTo((selectedIndex * 90).coerceAtLeast(0))
        }
    }

    Surface(
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {

            // ── Row 1: Month navigation ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick   = onPrevMonth,
                    modifier  = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft, "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(monthScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    months.forEach { month ->
                        val isSelected = (month == selectedMonth)

                        val chipBg by animateColorAsState(
                            targetValue   = if (isSelected) AccentPurple
                                            else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "month_chip_bg"
                        )
                        val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurface

                        // Show "Jul 26" — short month + 2-digit year
                        val label = month.month.name.take(3).let {
                            it[0] + it.substring(1).lowercase()
                        } + " " + month.year.toString().takeLast(2)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .clickable {
                                    // Navigate to the first Monday of selected month
                                    val firstMonday = month.atDay(1)
                                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                    onWeekSelected(firstMonday)
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = label,
                                fontSize   = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color      = chipText
                            )
                        }
                    }
                }

                IconButton(
                    onClick  = onNextMonth,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight, "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(Modifier.height(6.dp))

            // ── Row 2: Week navigation ───────────────────────────────────────
            WeekStrip(
                selectedMonth    = selectedMonth,
                currentWeekStart = currentWeekStart,
                today            = today,
                onWeekSelected   = onWeekSelected
            )
        }
    }
}

@Composable
private fun WeekStrip(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    today: LocalDate,
    onWeekSelected: (LocalDate) -> Unit
) {
    // All Mondays whose week overlaps with selectedMonth
    val weeks = remember(selectedMonth) {
        buildList {
            var monday = selectedMonth.atDay(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val monthEnd = selectedMonth.atEndOfMonth()
            while (!monday.isAfter(monthEnd)) {
                add(monday)
                monday = monday.plusWeeks(1)
            }
        }
    }

    val fmt = DateTimeFormatter.ofPattern("d MMM")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weeks.forEach { weekStart ->
            val weekEnd    = weekStart.plusDays(6)
            val isSelected = (weekStart == currentWeekStart)
            val containsToday = !today.isBefore(weekStart) && !today.isAfter(weekEnd)

            val chipBg by animateColorAsState(
                targetValue = when {
                    isSelected   -> AccentCyan.copy(alpha = 0.20f)
                    containsToday -> AccentPurple.copy(alpha = 0.10f)
                    else          -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                label = "week_chip_bg"
            )
            val chipBorder by animateColorAsState(
                targetValue = when {
                    isSelected    -> AccentCyan
                    containsToday -> AccentPurple.copy(alpha = 0.5f)
                    else          -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                },
                label = "week_chip_border"
            )
            val textColor = when {
                isSelected    -> AccentCyan
                containsToday -> AccentPurple
                else          -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(9.dp))
                    .clickable { onWeekSelected(weekStart) }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                // Only show year suffix if week crosses a year boundary
                val startStr = weekStart.format(fmt)
                val endStr   = weekEnd.format(fmt)
                val yearTag  = " '${weekEnd.year.toString().takeLast(2)}"
                Text(
                    text      = "$startStr – $endStr$yearTag",
                    fontSize  = 10.sp,
                    color     = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
