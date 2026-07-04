package com.habittracker.app.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.components.*
import com.habittracker.app.ui.theme.*
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: HabitTrackerViewModel) {
    // ── State ───────────────────────────────────────────────────────────────
    val state       by viewModel.uiState.collectAsStateWithLifecycle()
    val scope       = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context     = LocalContext.current

    var currentScreen    by remember { mutableStateOf("dashboard") }
    var showAddDialog    by remember { mutableStateOf(false) }
    var habitToEdit      by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete    by remember { mutableStateOf<Habit?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText  by remember { mutableStateOf("") }
    var isEditingName    by remember { mutableStateOf(false) }
    var nameInput        by remember { mutableStateOf("") }
    var showConfetti     by remember { mutableStateOf(false) }

    LaunchedEffect(state.userProfile) {
        nameInput = state.userProfile?.name ?: "Your Name"
    }

    // Navigate to analytics or settings screen
    if (currentScreen == "analytics") {
        AnalyticsScreen(viewModel = viewModel, onBack = { currentScreen = "dashboard" })
        return
    }
    if (currentScreen == "settings") {
        SettingsScreen(viewModel = viewModel, onBack = { currentScreen = "dashboard" })
        return
    }

    // ── Week dates derived from selectedWeekStart ────────────────────────────
    val weekDates = remember(state.selectedWeekStart) {
        (0..6).map { state.selectedWeekStart.plusDays(it.toLong()) }
    }

    // ── Progress bars for chart ──────────────────────────────────────────────
    val barData = remember(weekDates, state.dailyCounts, state.habits, state.today) {
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        weekDates.mapIndexed { i, date ->
            BarData(
                label    = labels[i],
                progress = viewModel.getDayProgress(state, date.toEpochDay()),
                isToday  = date == state.today
            )
        }
    }

    // ── Background gradient ──────────────────────────────────────────────────
    val isDark  = state.userProfile?.isDarkMode ?: false
    // Must NOT be wrapped in remember(isDark) so recomposition happens when isDark flips
    val bgBrush = if (isDark)
        Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientMidDark, BgGradientEndDark))
    else
        Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientMidLight, BgGradientEndLight))

    // ── Confetti Trigger ─────────────────────────────────────────────────────
    val todayScheduled = remember(state.habits, state.today) {
        state.habits.count { viewModel.isScheduledForDate(it, state.today) }
    }
    val todayDone = state.dailyCounts.find { it.dateEpochDay == state.today.toEpochDay() }?.count ?: 0
    var hasCelebratedToday by remember { mutableStateOf(false) }
    
    LaunchedEffect(todayDone, todayScheduled) {
        if (todayScheduled > 0 && todayDone == todayScheduled && !hasCelebratedToday) {
            showConfetti = true
            hasCelebratedToday = true
        } else if (todayDone < todayScheduled) {
            hasCelebratedToday = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                // Use fully opaque surface so dark mode text is readable
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // ── App name ──────────────────────────────────────────────
                    Text(
                        text = "Aham",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentPurple
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your personal wellness companion",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))
                    DrawerSectionLabel("PROFILE")
                    Spacer(Modifier.height(8.dp))

                    // ── Profile card ─────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        if (isEditingName) {
                            Column {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Your Name", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentPurple,
                                        focusedLabelColor  = AccentPurple
                                    )
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { isEditingName = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = {
                                            if (nameInput.isNotBlank()) {
                                                viewModel.updateProfileName(nameInput)
                                                isEditingName = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                    ) { Text("Save", color = Color.White) }
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
                                        text = state.userProfile?.name ?: "Your Name",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = { isEditingName = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, "Edit Name", tint = AccentPurple, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    DrawerSectionLabel("NAVIGATE")
                    Spacer(Modifier.height(8.dp))

                    DrawerMenuItem(Icons.Default.BarChart, "Analytics & Insights") {
                        scope.launch { drawerState.close(); currentScreen = "analytics" }
                    }

                    DrawerMenuItem(Icons.Default.Settings, "Settings") {
                        scope.launch { drawerState.close(); currentScreen = "settings" }
                    }

                    // Removed Backup & Restore section since Android Auto Backup handles it seamlessly in the background

                    Spacer(modifier = Modifier.weight(1f))

                    // ── Sticky footer ────────────────────────────────────────
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "made by raghav",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "aham brahmasmi",
                            fontSize = 13.sp,
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
            modifier = Modifier.background(bgBrush),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "HabitTracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    actions = {
                        // Streak badge
                        val streak = state.userProfile?.currentStreak ?: 0
                        if (streak > 0) {
                            Box(
                                modifier = Modifier
                                    .background(AccentAmber.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                    .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text("🔥 $streak days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        // Active habits badge
                        val count = state.habits.size
                        val badgeLabel = if (count == 1) "1 Habit" else "$count Habits"
                        Box(
                            modifier = Modifier
                                .background(AccentPurple.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("💪 $badgeLabel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentPurpleLight)
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = AccentPurple,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(10.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Habit")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                // ── Top Navigation (Month / Week) ───────────────────────────
                item {
                    TopWeekPicker(
                        selectedMonth    = state.selectedMonth,
                        currentWeekStart = state.selectedWeekStart,
                        onPrevMonth      = { viewModel.selectMonth(state.selectedMonth.minusMonths(1)) },
                        onNextMonth      = { viewModel.selectMonth(state.selectedMonth.plusMonths(1)) },
                        onWeekSelected   = { monday -> viewModel.selectWeekStart(monday) }
                    )
                }

                // ── Stats cards ─────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatsCard("Weekly Avg", "${state.weeklyAverage.toInt()}%", AccentPurple, Modifier.weight(1f))
                        StatsCard("Monthly Avg", "${state.monthlyAverage.toInt()}%", AccentCyan, Modifier.weight(1f))
                    }
                }

                // ── Progress bar chart ──────────────────────────────────────
                item { DailyProgressChart(bars = barData) }

                // ── Section header ──────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                "Habit Grid",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val weekRange = remember(state.selectedWeekStart) {
                                val end = state.selectedWeekStart.plusDays(6)
                                "${state.selectedWeekStart.format(DateTimeFormatter.ofPattern("d MMM"))} – ${end.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
                            }
                            Text(weekRange, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Grid header ─────────────────────────────────────────────
                if (state.habits.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        ) {
                            HabitGridHeader(weekDates = weekDates, today = state.today)
                        }
                    }
                }

                // ── Habit rows ──────────────────────────────────────────────
                items(items = state.habits, key = { it.id }) { habit ->
                    HabitRow(
                        habit = habit,
                        weekDates = weekDates,
                        isCompleted = { date -> viewModel.isCompleted(state, habit.id, date.toEpochDay()) },
                        onToggle = { date -> viewModel.toggleHabitCompletion(habit.id, date.toEpochDay()) },
                        onEditClick = { habitToEdit = habit },
                        isScheduled = { date -> viewModel.isScheduledForDate(habit, date) },
                        today = state.today,
                        hapticsEnabled = state.userProfile?.hapticsEnabled ?: true,
                        compactMode = state.userProfile?.compactHabitGrid ?: false
                    )
                }

                // Grid bottom cap
                if (state.habits.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                        )
                    }
                }

                // ── Empty state ─────────────────────────────────────────────
                if (state.habits.isEmpty()) {
                    item { EmptyHabitState(onAdd = { showAddDialog = true }) }
                }

                // ── Wellness section ────────────────────────────────────────
                item {
                    WellnessSection(
                        wellness     = state.wellnessEntry,
                        selectedDate = state.selectedDate,
                        onMoodChange = viewModel::updateMood,
                        onSleepChange = viewModel::updateSleep
                    )
                }
            }
            
            // ── Confetti Overlay ────────────────────────────────────────
            ConfettiOverlay(
                isVisible = showConfetti,
                onAnimationEnd = { showConfetti = false }
            )
        }
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

    if (showAddDialog) {
        HabitDialog(
            habitToEdit = null,
            onDismiss   = { showAddDialog = false },
            onConfirm   = { name, color, freqType, days ->
                viewModel.addHabit(name, color, freqType, days)
                showAddDialog = false
            }
        )
    }

    habitToEdit?.let { habit ->
        HabitDialog(
            habitToEdit = habit,
            onDismiss   = { habitToEdit = null },
            onConfirm   = { name, color, freqType, days ->
                viewModel.updateHabit(habit.copy(name = name, colorHex = color, frequencyType = freqType, customDays = days))
                habitToEdit = null
            },
            onDelete    = { habitToDelete = habit; habitToEdit = null }
        )
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor   = MaterialTheme.colorScheme.surfaceVariant,
            icon             = { Icon(Icons.Default.Delete, null, tint = AccentRed) },
            title            = { Text("Delete Habit?", color = MaterialTheme.colorScheme.onSurface) },
            text             = {
                Text(
                    "\"${habit.name}\" and all its history will be deleted.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteHabit(habit); habitToDelete = null }) {
                    Text("Delete", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor   = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Import Backup", color = MaterialTheme.colorScheme.onSurface) },
            text  = {
                Column {
                    Text(
                        "Paste exported JSON backup data below. This overwrites all current data.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value          = importInputText,
                        onValueChange  = { importInputText = it },
                        placeholder    = { Text("Paste JSON here…", fontSize = 11.sp) },
                        modifier       = Modifier.fillMaxWidth().height(120.dp),
                        textStyle      = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick  = {
                        viewModel.importBackup(importInputText) { success ->
                            Toast.makeText(
                                context,
                                if (success) "Backup restored!" else "Invalid backup data.",
                                Toast.LENGTH_SHORT
                            ).show()
                            showImportDialog = false
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

// ── Helper Composables ───────────────────────────────────────────────────────

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp,
        color = AccentPurple
    )
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StatsCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun EmptyHabitState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✨", fontSize = 44.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No habits yet",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap + to create your first habit\nand start tracking your progress.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onAdd,
            colors  = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Create First Habit", color = Color.White)
        }
    }
}
