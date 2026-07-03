package com.habittracker.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.components.*
import com.habittracker.app.ui.theme.*
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: HabitTrackerViewModel) {
    val state by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // Build 7 dates for the current week
    val weekDates = remember(state.selectedWeekStart) {
        (0..6).map { state.selectedWeekStart.plusDays(it.toLong()) }
    }

    // Build bar chart data
    val barData = remember(weekDates, state.dailyCounts, state.totalHabits, state.today) {
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        weekDates.mapIndexed { index, date ->
            val epochDay = date.toEpochDay()
            val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
            val progress = if (state.totalHabits > 0) count.toFloat() / state.totalHabits else 0f
            BarData(
                label = dayLabels[index],
                progress = progress.coerceIn(0f, 1f),
                isToday = date == state.today
            )
        }
    }

    // Month display string
    val monthDisplay = remember(state.selectedMonth) {
        state.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Habit Tracker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = monthDisplay,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    // Streak counter chip
                    Box(
                        modifier = Modifier
                            .background(AccentPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔥 ${state.habits.size} habits",
                            fontSize = 12.sp,
                            color = AccentPurpleLight
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentPurple,
                contentColor = TextPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        bottomBar = {
            MonthNavigationBar(
                selectedMonth = state.selectedMonth,
                currentWeekStart = state.selectedWeekStart,
                onPrevMonth = {
                    viewModel.selectMonth(state.selectedMonth.minusMonths(1))
                },
                onNextMonth = {
                    viewModel.selectMonth(state.selectedMonth.plusMonths(1))
                },
                onWeekSelected = { firstDay ->
                    // If a month chip is tapped, firstDay is atDay(1) of that month
                    val month = YearMonth.from(firstDay)
                    viewModel.selectMonth(month)
                    val monday = firstDay.with(
                        java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
                    )
                    viewModel.selectWeekStart(monday)
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {

            // ── TOP: Daily Progress Chart ─────────────────────────────────────
            item {
                DailyProgressChart(bars = barData)
            }

            // ── MIDDLE: Habit Grid Section Header ─────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habit Grid",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        val weekRangeLabel = remember(state.selectedWeekStart) {
                            val start = state.selectedWeekStart
                            val end = start.plusDays(6)
                            val fmt = DateTimeFormatter.ofPattern("d MMM")
                            "${start.format(fmt)} – ${end.format(fmt)}"
                        }
                        Text(weekRangeLabel, fontSize = 11.sp, color = TextSecondary)
                    }
                    if (state.habits.isEmpty()) {
                        Text(
                            "Tap + to add habits",
                            fontSize = 11.sp,
                            color = AccentPurpleLight
                        )
                    } else {
                        // Weekly completion percentage
                        val totalPossible = state.habits.size * 7
                        val totalDone = state.dailyCounts.sumOf { it.count }
                        val pct = if (totalPossible > 0) (totalDone * 100 / totalPossible) else 0
                        Text("$pct% this week", fontSize = 11.sp, color = AccentGreen)
                    }
                }
            }

            // ── Grid header (sticky day labels) ───────────────────────────────
            if (state.habits.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    ) {
                        HabitGridHeader(weekDates = weekDates, today = state.today)
                    }
                }
            }

            // ── Habit Rows ────────────────────────────────────────────────────
            items(items = state.habits, key = { it.id }) { habit ->
                HabitRow(
                    habit = habit,
                    weekDates = weekDates,
                    isCompleted = { date ->
                        viewModel.isCompleted(state, habit.id, date.toEpochDay())
                    },
                    onToggle = { date ->
                        viewModel.toggleHabitCompletion(habit.id, date.toEpochDay())
                    },
                    onLongPress = { habitToDelete = habit },
                    today = state.today
                )
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (state.habits.isEmpty()) {
                item { EmptyHabitState(onAdd = { showAddDialog = true }) }
            }

            // ── BOTTOM: Overall Wellness ──────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(AccentPurple.copy(0.08f), AccentCyan.copy(0.05f))),
                            RoundedCornerShape(2.dp)
                        )
                        .padding(vertical = 2.dp)
                )
            }

            item {
                WellnessSection(
                    wellness = state.wellnessEntry,
                    onMoodChange = viewModel::updateMood,
                    onSleepChange = viewModel::updateSleep
                )
            }

            // Bottom padding for FAB
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addHabit(name, color)
                showAddDialog = false
            }
        )
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor = SurfaceDark,
            title = { Text("Delete Habit?", color = TextPrimary) },
            text = {
                Text(
                    "\"${habit.name}\" and all its completion history will be removed.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habit)
                    habitToDelete = null
                }) {
                    Text("Delete", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
            }
        )
    }
}

@Composable
private fun EmptyHabitState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No habits yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap the + button to start building\nyour ideal daily routine.",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add First Habit")
        }
    }
}
