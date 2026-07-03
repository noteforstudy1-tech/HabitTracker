package com.habittracker.app.ui.screen

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Screen navigation toggle: "dashboard" or "analytics"
    var currentScreen by remember { mutableStateOf("dashboard") }

    // Dialog flags
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }

    // Drawer profile edit state
    var isEditingName by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    // Sync input name
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
            val progress = viewModel.getDayProgress(state, date.toEpochDay())
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

    if (currentScreen == "analytics") {
        AnalyticsScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "dashboard" }
        )
        return
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
                    Spacer(Modifier.height(24.dp))

                    // Profile Section
                    Text(
                        text = "PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))

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

                    Spacer(Modifier.height(24.dp))

                    // Menu Actions
                    Text(
                        text = "NAVIGATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    NavigationMenuItem(
                        icon = Icons.Default.BarChart,
                        label = "Analytics & Insights",
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                currentScreen = "analytics"
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "BACKUP & RESTORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    // Export / Backup button (Share Intent)
                    NavigationMenuItem(
                        icon = Icons.Default.Share,
                        label = "Export Data (Share)",
                        onClick = {
                            scope.launch {
                                val json = viewModel.getExportJsonString()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Habit Tracker Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                                drawerState.close()
                            }
                        }
                    )

                    Spacer(Modifier.height(6.dp))

                    // Import Backup button (Dialog Paste)
                    NavigationMenuItem(
                        icon = Icons.Default.Upload,
                        label = "Import Backup",
                        onClick = {
                            scope.launch {
                                importInputText = ""
                                showImportDialog = true
                                drawerState.close()
                            }
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
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
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
                        isScheduled = { date ->
                            viewModel.isScheduledForDate(habit, date)
                        },
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
            onConfirm = { name, color, freqType, customDays ->
                viewModel.addHabit(name, color, freqType, customDays)
                showAddDialog = false
            }
        )
    }

    habitToEdit?.let { habit ->
        HabitDialog(
            habitToEdit = habit,
            onDismiss = { habitToEdit = null },
            onConfirm = { name, color, freqType, customDays ->
                viewModel.updateHabit(habit.copy(name = name, colorHex = color, frequencyType = freqType, customDays = customDays))
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

    // Backup restore text input dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Import Backup", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text(
                        text = "Paste the exported backup JSON data below. This will overwrite all existing habits, completions, and wellness logs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("Paste JSON here...", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importInputText.isNotBlank()) {
                            viewModel.importBackup(importInputText) { success ->
                                if (success) {
                                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to parse backup. Check formatting.", Toast.LENGTH_SHORT).show()
                                }
                                showImportDialog = false
                            }
                        }
                    },
                    enabled = importInputText.isNotBlank()
                ) {
                    Text("Restore", color = AccentPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
