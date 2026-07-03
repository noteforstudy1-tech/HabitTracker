package com.habittracker.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.*
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HabitTrackerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val bgGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientMidDark, BgGradientEndDark))
        } else {
            Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientMidLight, BgGradientEndLight))
        }
    }

    val today = LocalDate.now()
    val startRange = today.minusDays(112) // Grouped cleanly into 16 weeks (112 days)

    // ── Heatmap Data Mapping ──
    val historicalCounts = remember(state.historicalCompletions) {
        val countsMap = mutableMapOf<Long, Int>()
        state.historicalCompletions.forEach { completion ->
            if (completion.isCompleted) {
                countsMap[completion.dateEpochDay] = (countsMap[completion.dateEpochDay] ?: 0) + 1
            }
        }
        countsMap
    }

    // ── Mood vs Completion Rate dynamic correlation ──
    val moodAverages = remember(state.historicalCompletions, state.historicalWellness, state.habits) {
        val ratesByMood = MutableList(5) { mutableListOf<Float>() }
        val completionsByDay = state.historicalCompletions
            .filter { it.isCompleted }
            .groupBy { it.dateEpochDay }

        state.historicalWellness.forEach { wellness ->
            val date = LocalDate.ofEpochDay(wellness.dateEpochDay)
            val scheduledCount = state.habits.count { viewModel.isScheduledForDate(it, date) }
            if (scheduledCount > 0) {
                val completedCount = completionsByDay[wellness.dateEpochDay]?.size ?: 0
                val completionRate = (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f) * 100f
                if (wellness.moodIndex in 0..4) {
                    ratesByMood[wellness.moodIndex].add(completionRate)
                }
            }
        }
        ratesByMood.map { rates -> if (rates.isEmpty()) 0f else rates.average().toFloat() }
    }

    // ── Sleep vs Completion Rate dynamic correlation ──
    val sleepLabels = listOf("4-5h", "6h", "7h", "8h", "9h+")
    val sleepAverages = remember(state.historicalCompletions, state.historicalWellness, state.habits) {
        val ratesBySleepBin = MutableList(5) { mutableListOf<Float>() }
        val completionsByDay = state.historicalCompletions
            .filter { it.isCompleted }
            .groupBy { it.dateEpochDay }

        state.historicalWellness.forEach { wellness ->
            val date = LocalDate.ofEpochDay(wellness.dateEpochDay)
            val scheduledCount = state.habits.count { viewModel.isScheduledForDate(it, date) }
            if (scheduledCount > 0) {
                val completedCount = completionsByDay[wellness.dateEpochDay]?.size ?: 0
                val completionRate = (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f) * 100f
                val sleepHours = wellness.sleepHours

                val binIdx = when {
                    sleepHours < 5.5f -> 0 // 4-5h
                    sleepHours < 6.5f -> 1 // 6h
                    sleepHours < 7.5f -> 2 // 7h
                    sleepHours < 8.5f -> 3 // 8h
                    else -> 4              // 9h+
                }
                ratesBySleepBin[binIdx].add(completionRate)
            }
        }
        ratesBySleepBin.map { rates -> if (rates.isEmpty()) 0f else rates.average().toFloat() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(bgGradient),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics & Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // ── Section 1: GitHub-Style Heatmap Grid ──────────────────────────
            item {
                Column {
                    Text(
                        text = "Consistency Heatmap",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Habit completion intensity over the last 16 weeks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    RealtimeHeatmapGrid(
                        startDate = startRange,
                        historicalCounts = historicalCounts,
                        totalHabits = state.totalHabits
                    )
                }
            }

            // ── Section 2: Sleep vs. Habit Completion ──────────────────────────
            item {
                Column {
                    Text(
                        text = "Sleep vs. Habit Completion",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time average completions grouped by hours slept",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    RealtimeBarChart(
                        labels = sleepLabels,
                        values = sleepAverages,
                        accentColor = AccentCyan
                    )
                }
            }

            // ── Section 3: Mood vs. Habit Completion ───────────────────────────
            item {
                Column {
                    Text(
                        text = "Mood vs. Habit Completion",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time average completions grouped by checked mood",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    RealtimeBarChart(
                        labels = listOf("😞", "😕", "😐", "🙂", "😄"),
                        values = moodAverages,
                        accentColor = AccentPurpleLight
                    )
                }
            }
        }
    }
}

@Composable
fun RealtimeHeatmapGrid(
    startDate: LocalDate,
    historicalCounts: Map<Long, Int>,
    totalHabits: Int
) {
    val columns = 16
    val rows = 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (col in 0 until columns) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (row in 0 until rows) {
                        val cellIdx = col * rows + row
                        val cellDate = startDate.plusDays(cellIdx.toLong())
                        val count = historicalCounts[cellDate.toEpochDay()] ?: 0
                        val ratio = if (totalHabits > 0) count.toFloat() / totalHabits else 0f

                        val cellColor = when {
                            ratio == 0f -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ratio <= 0.3f -> AccentPurple.copy(alpha = 0.3f)
                            ratio <= 0.6f -> AccentPurple.copy(alpha = 0.6f)
                            ratio <= 0.9f -> AccentPurple
                            else -> AccentCyan
                        }

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeBarChart(
    labels: List<String>,
    values: List<Float>,
    accentColor: Color
) {
    val hasData = values.any { it > 0f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        if (!hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logging habits and wellness to generate correlation insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                values.forEachIndexed { idx, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "${value.toInt()}%",
                            fontSize = 9.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight((value / 100f).coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(accentColor, AccentPurple)
                                    )
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = labels[idx],
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
