package com.habittracker.app.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.ui.theme.*
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HabitTrackerViewModel,
    onBack: () -> Unit
) {
    // ✅ Use lifecycle-aware collection, not plain collectAsState
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    // ✅ Read dark mode from Room preference, not from system
    val isDark = state.userProfile?.isDarkMode ?: false

    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientMidDark, BgGradientEndDark))
    } else {
        Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientMidLight, BgGradientEndLight))
    }

    val today      = LocalDate.now()
    val startRange = today.minusDays(112) // 16 weeks

    // ── Heatmap Data ──────────────────────────────────────────────────────────
    val historicalCounts = remember(state.historicalCompletions) {
        buildMap<Long, Int> {
            state.historicalCompletions.forEach { c ->
                if (c.isCompleted) put(c.dateEpochDay, (get(c.dateEpochDay) ?: 0) + 1)
            }
        }
    }

    // ── Mood correlation ──────────────────────────────────────────────────────
    val moodAverages = remember(state.historicalCompletions, state.historicalWellness, state.habits) {
        val buckets = MutableList(5) { mutableListOf<Float>() }
        val doneByDay = state.historicalCompletions.filter { it.isCompleted }.groupBy { it.dateEpochDay }
        state.historicalWellness.forEach { w ->
            val date = LocalDate.ofEpochDay(w.dateEpochDay)
            val scheduled = state.habits.count { viewModel.isScheduledForDate(it, date) }
            if (scheduled > 0 && w.moodIndex in 0..4) {
                val done = doneByDay[w.dateEpochDay]?.size ?: 0
                buckets[w.moodIndex].add((done.toFloat() / scheduled * 100f).coerceIn(0f, 100f))
            }
        }
        buckets.map { if (it.isEmpty()) -1f else it.average().toFloat() } // -1f = no data
    }

    // ── Sleep correlation ─────────────────────────────────────────────────────
    val sleepLabels  = listOf("4-5h", "6h", "7h", "8h", "9h+")
    val sleepAverages = remember(state.historicalCompletions, state.historicalWellness, state.habits) {
        val buckets = MutableList(5) { mutableListOf<Float>() }
        val doneByDay = state.historicalCompletions.filter { it.isCompleted }.groupBy { it.dateEpochDay }
        state.historicalWellness.forEach { w ->
            val date = LocalDate.ofEpochDay(w.dateEpochDay)
            val scheduled = state.habits.count { viewModel.isScheduledForDate(it, date) }
            if (scheduled > 0) {
                val done = doneByDay[w.dateEpochDay]?.size ?: 0
                val rate = (done.toFloat() / scheduled * 100f).coerceIn(0f, 100f)
                val bin  = when {
                    w.sleepHours < 5.5f -> 0
                    w.sleepHours < 6.5f -> 1
                    w.sleepHours < 7.5f -> 2
                    w.sleepHours < 8.5f -> 3
                    else                -> 4
                }
                buckets[bin].add(rate)
            }
        }
        buckets.map { if (it.isEmpty()) -1f else it.average().toFloat() } // -1f = no data
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(bgGradient),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics & Insights",
                        style     = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color     = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding      = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Section 1 — Heatmap
            item {
                AnalyticsSectionHeader(
                    title    = "Consistency Heatmap",
                    subtitle = "Habit completion intensity — last 16 weeks"
                )
                Spacer(Modifier.height(10.dp))
                HeatmapGrid(
                    startDate        = startRange,
                    historicalCounts = historicalCounts,
                    totalHabits      = state.totalHabits
                )
            }

            // Section 2 — Sleep vs Completion
            item {
                AnalyticsSectionHeader(
                    title    = "Sleep vs. Habit Completion",
                    subtitle = "Average completion rate grouped by hours of sleep"
                )
                Spacer(Modifier.height(10.dp))
                CorrelationBarChart(
                    labels      = sleepLabels,
                    values      = sleepAverages,
                    accentColor = AccentCyan
                )
            }

            // Section 3 — Mood vs Completion
            item {
                AnalyticsSectionHeader(
                    title    = "Mood vs. Habit Completion",
                    subtitle = "Average completion rate grouped by daily mood"
                )
                Spacer(Modifier.height(10.dp))
                CorrelationBarChart(
                    labels      = listOf("😞 Awful", "😕 Bad", "😐 Okay", "🙂 Good", "😄 Great"),
                    values      = moodAverages,
                    accentColor = AccentPurpleLight
                )
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsSectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Heatmap Grid ──────────────────────────────────────────────────────────────

@Composable
fun HeatmapGrid(
    startDate: LocalDate,
    historicalCounts: Map<Long, Int>,
    totalHabits: Int
) {
    val columns = 16
    val rows    = 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until columns) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (row in 0 until rows) {
                                val cellDate = startDate.plusDays((col * rows + row).toLong())
                                val count    = historicalCounts[cellDate.toEpochDay()] ?: 0
                                val ratio    = if (totalHabits > 0) count.toFloat() / totalHabits else 0f

                                val cellColor = when {
                                    ratio == 0f  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ratio <= 0.3f -> AccentPurple.copy(alpha = 0.30f)
                                    ratio <= 0.6f -> AccentPurple.copy(alpha = 0.65f)
                                    ratio <= 0.9f -> AccentPurple
                                    else          -> AccentCyan
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

            Spacer(Modifier.height(12.dp))
            
            // Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    AccentPurple.copy(alpha = 0.30f),
                    AccentPurple.copy(alpha = 0.65f),
                    AccentPurple,
                    AccentCyan
                ).forEach { color ->
                    Box(modifier = Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Correlation Bar Chart ─────────────────────────────────────────────────────

@Composable
fun CorrelationBarChart(
    labels: List<String>,
    values: List<Float>,   // -1f means no data for that bucket
    accentColor: Color
) {
    val hasAnyData = values.any { it >= 0f }
    val maxValue   = values.filter { it >= 0f }.maxOrNull()?.coerceAtLeast(1f) ?: 100f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (!hasAnyData) {
            // Empty state
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📊", fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = "Log habits & wellness for a few days to see correlations here.",
                    fontSize  = 12.sp,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Chart
            val chartHeight = 130.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEachIndexed { idx, rawValue ->
                    val hasData   = rawValue >= 0f
                    val pct       = if (hasData) rawValue else 0f
                    // Animate each bar height
                    val animated by animateFloatAsState(
                        targetValue   = if (hasData) pct / maxValue else 0f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label         = "bar_$idx"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Percentage label above bar (or "–" for no data)
                        Text(
                            text      = if (hasData) "${pct.toInt()}%" else "–",
                            fontSize  = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color     = if (hasData) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))

                        // Bar itself — min 4dp height so 0% isn't invisible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(chartHeight * animated.coerceAtLeast(if (hasData) 0.03f else 0f))
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    if (hasData)
                                        Brush.verticalGradient(listOf(accentColor, AccentPurple))
                                    else
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                )
                        )
                        Spacer(Modifier.height(6.dp))

                        // Label beneath bar — word-wrapped, centered
                        Text(
                            text      = labels[idx],
                            fontSize  = 9.sp,
                            color     = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines  = 2,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Keep old names as aliases so any other file referencing them still compiles
@Composable
fun RealtimeHeatmapGrid(
    startDate: LocalDate,
    historicalCounts: Map<Long, Int>,
    totalHabits: Int
) = HeatmapGrid(startDate, historicalCounts, totalHabits)

@Composable
fun RealtimeBarChart(
    labels: List<String>,
    values: List<Float>,
    accentColor: Color
) = CorrelationBarChart(labels, values, accentColor)
