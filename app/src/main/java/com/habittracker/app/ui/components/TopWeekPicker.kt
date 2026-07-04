package com.habittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.window.PopupPositionProvider
import com.habittracker.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun TopWeekPicker(
    selectedMonth: YearMonth,
    currentWeekStart: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWeekSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    
    // Label e.g., "July 2026"
    val monthLabel = remember(selectedMonth) {
        val monthStr = selectedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$monthStr ${selectedMonth.year}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── Row 1: Month Name & Arrows ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.ChevronLeft, 
                        "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.ChevronRight, 
                        "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // ── Row 2: Week Chips for Selected Month ─────────────────────────
            WeekStrip(
                selectedMonth    = selectedMonth,
                currentWeekStart = currentWeekStart,
                today            = today,
                onWeekSelected   = onWeekSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollState = rememberScrollState()

    // Auto scroll to selected week
    val selectedIndex = weeks.indexOf(currentWeekStart)
    LaunchedEffect(currentWeekStart, selectedMonth) {
        if (selectedIndex >= 0) {
            scrollState.animateScrollTo((selectedIndex * 120).coerceAtLeast(0))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weeks.forEachIndexed { index, weekStart ->
            val weekEnd    = weekStart.plusDays(6)
            val isSelected = (weekStart == currentWeekStart)
            val containsToday = !today.isBefore(weekStart) && !today.isAfter(weekEnd)

            val chipBg by animateColorAsState(
                targetValue = when {
                    isSelected    -> AccentCyan.copy(alpha = 0.20f)
                    containsToday -> AccentPurple.copy(alpha = 0.10f)
                    else          -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                label = "week_chip_bg"
            )
            val chipBorder by animateColorAsState(
                targetValue = when {
                    isSelected    -> AccentCyan
                    containsToday -> AccentPurple.copy(alpha = 0.5f)
                    else          -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                label = "week_chip_border"
            )
            val textColor = when {
                isSelected    -> AccentCyan
                containsToday -> AccentPurple
                else          -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val startStr = weekStart.format(fmt)
            val endStr   = weekEnd.format(fmt)
            val yearTag  = if (weekStart.year != weekEnd.year) " '${weekEnd.year.toString().takeLast(2)}" else ""
            val tooltipText = "$startStr – $endStr$yearTag"
            
            val tooltipState = rememberTooltipState()

            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(tooltipText, fontSize = 12.sp)
                    }
                },
                state = tooltipState
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                        .clickable { onWeekSelected(weekStart) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text       = "Week ${index + 1}",
                        fontSize   = 12.sp,
                        color      = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
