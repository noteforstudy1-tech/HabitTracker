package com.habittracker.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.components.*
import com.habittracker.app.ui.theme.*
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: HabitTrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // Drawer profile edit state
    var isEditingName by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    // Synchronize initial text field input with current profile name
    LaunchedEffect(state.userProfile) {
        state.userProfile?.name?.let { newNameInput = it }
    }

    // Build 7 dates for the selected week
    val weekDates = remember(state.selectedWeekStart) {
        (0..6).map { state.selectedWeekStart.plusDays(it.toLong()) }
    }

    // Chart progress bar data
    val barData = remember(weekDates, state.dailyCounts, state.totalHabits, state.today) {
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        weekDates.mapIndexed { idx, date ->
            val epochDay = date.toEpochDay()
            val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
            val progress = if (state.totalHabits > 0) count.toFloat() / state.totalHabits else 0f
            BarData(
                label = dayLabels[idx],
                progress = progress.coerceIn(0f, 1f),
                isToday = date == state.today
            )
        }
    }

    val isDark = isSystemInDarkTheme()
    val mainGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientMidDark, BgGradientEndDark))
        } else {
            Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientMidLight, BgGradientEndLight))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Drawer Header
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(28.dp))

                    // Profile Section
                    Text(
                        text = "PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = newNameInput,
                                    onValueChange = { newNameInput = it },
                                    label = { Text("Your Name", fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentPurple,
                                        focusedLabelColor = AccentPurple
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { isEditingName = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = {
                                            if (newNameInput.isNotBlank()) {
                                                viewModel.updateProfileName(newNameInput)
                                                isEditingName = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                    ) {
                                        Text("Save", color = Color.White)
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = state.userProfile?.name ?: "Raghav Parashar",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Active Tracker",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            newNameInput = state.userProfile?.name ?: "Raghav Parashar"
                                            isEditingName = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile Name",
                                            tint = AccentPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Menu Body
                    Text(
                        text = "NAVIGATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    NavigationMenuItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Drawer Footer
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "made by raghav",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "aham brahmasmi",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.background(mainGradient),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "Habit Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val monthLabel = remember(state.selectedMonth) {
                                state.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                            }
                            Text(
                                text = monthLabel,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        // Current Streak Counter
                        val streak = state.userProfile?.currentStreak ?: 0
                        if (streak > 0) {
                            Box(
                                modifier = Modifier
                                    .background(AccentAmber.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🔥 $streak days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentAmber
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))

                        // Active Habits count badge
                        Box(
                            modifier = Modifier
                                .background(AccentPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💪 ${state.habits.size} habits",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurpleLight
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = AccentPurple,
                    contentColor = Color.White,
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
                    onPrevMonth = { viewModel.selectMonth(state.selectedMonth.minusMonths(1)) },
                    onNextMonth = { viewModel.selectMonth(state.selectedMonth.plusMonths(1)) },
                    onWeekSelected = { monday ->
                        viewModel.selectWeekStart(monday)
                        viewModel.selectDate(monday)
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
            ) {
                // ── analytics row: side-by-side stats ─────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticsCard(
                            title = "Weekly Average",
                            value = "${state.weeklyAverage.toInt()}%",
                            color = AccentPurple,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Monthly Average",
                            value = "${state.monthlyAverage.toInt()}%",
                            color = AccentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── chart ────────────────────────────────────────────────────────
                item {
                    DailyProgressChart(bars = barData)
                }

                // ── section header ────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Habit Grid",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val weekRange = remember(state.selectedWeekStart) {
                                val end = state.selectedWeekStart.plusDays(6)
                                val fmt = DateTimeFormatter.ofPattern("d MMM")
                                "${state.selectedWeekStart.format(fmt)} – ${end.format(fmt)}"
                            }
                            Text(
                                text = weekRange,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── sticky table headers ──────────────────────────────────────────
                if (state.habits.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            HabitGridHeader(
                                weekDates = weekDates,
                                today = state.today
                            )
                        }
                    }
                }

                // ── habit rows ───────────────────────────────────────────────────
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
                        onEditClick = { habitToEdit = habit },
                        today = state.today
                    )
                }

                // ── rounded bottom border for habit grid ──────────────────────────
                if (state.habits.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        )
                    }
                }

                // ── empty state ──────────────────────────────────────────────────
                if (state.habits.isEmpty()) {
                    item {
                        EmptyHabitState(onAdd = { showAddDialog = true })
                    }
                }

                // ── wellness check-in ────────────────────────────────────────────
                item {
                    WellnessSection(
                        wellness = state.wellnessEntry,
                        selectedDate = state.selectedDate,
                        onMoodChange = viewModel::updateMood,
                        onSleepChange = viewModel::updateSleep
                    )
                }
            }
        }
    }

    // ── CRUD dialogs ─────────────────────────────────────────────────────────

    if (showAddDialog) {
        HabitDialog(
            habitToEdit = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addHabit(name, color)
                showAddDialog = false
            }
        )
    }

    habitToEdit?.let { habit ->
        HabitDialog(
            habitToEdit = habit,
            onDismiss = { habitToEdit = null },
            onConfirm = { name, color ->
                viewModel.updateHabit(habit.copy(name = name, colorHex = color))
                habitToEdit = null
            },
            onDelete = {
                habitToDelete = habit
                habitToEdit = null
            }
        )
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Delete Habit?", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "\"${habit.name}\" and all its completion history will be removed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
            }
        )
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun NavigationMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyHabitState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(20.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No habits configured",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Configure habits to begin tracking\nyour goals and metrics.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Create First Habit", color = Color.White)
        }
    }
}
