package com.habittracker.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.app.HabitTrackerApplication
import com.habittracker.app.data.dao.DailyCount
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.data.model.UserProfile
import com.habittracker.app.data.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class HabitTrackerUiState(
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val dailyCounts: List<DailyCount> = emptyList(),
    val wellnessEntry: WellnessEntry? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedWeekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val totalHabits: Int = 0,
    val userProfile: UserProfile? = null,
    val weeklyAverage: Float = 0f,
    val monthlyAverage: Float = 0f,
    val historicalCompletions: List<HabitCompletion> = emptyList(),
    val historicalWellness: List<WellnessEntry> = emptyList()
)

// Intermediate flow containers to avoid >5-arg combine
private data class WeekData(
    val habits: List<Habit>,
    val completions: List<HabitCompletion>,
    val dailyCounts: List<DailyCount>
)

private data class ContextData(
    val profile: UserProfile?,
    val month: YearMonth,
    val weekStart: LocalDate,
    val selectedDate: LocalDate,
    val wellness: WellnessEntry?
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    private val _selectedMonth    = MutableStateFlow(YearMonth.now())
    private val _selectedWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HabitTrackerUiState>

    init {
        val db = (application as HabitTrackerApplication).database
        repository = HabitRepository(
            db.habitDao(),
            db.habitCompletionDao(),
            db.wellnessDao(),
            db.userProfileDao()
        )

        viewModelScope.launch {
            repository.checkAndInitProfile()
            updateStreakInDatabase()
        }

        // Flow 1: habits + week completions + week daily counts (3 sub-flows → 1)
        val weekDataFlow = _selectedWeekStart.flatMapLatest { weekStart ->
            val s = weekStart.toEpochDay()
            val e = weekStart.plusDays(6).toEpochDay()
            combine(
                repository.allHabits,
                repository.getCompletionsInRange(s, e),
                repository.getDailyCompletionCounts(s, e)
            ) { habits, comps, counts -> WeekData(habits, comps, counts) }
        }

        // Flow 2: monthly completion counts
        val monthlyCountsFlow = _selectedMonth.flatMapLatest { month ->
            repository.getDailyCompletionCounts(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
        }

        // Flow 3: context (profile + navigation state + wellness) 5 sub-flows → 1
        val contextFlow = combine(
            repository.userProfile,
            _selectedMonth,
            _selectedWeekStart,
            _selectedDate,
            _selectedDate.flatMapLatest { repository.getWellnessEntry(it.toEpochDay()) }
        ) { profile, month, weekStart, date, wellness ->
            ContextData(profile, month, weekStart, date, wellness)
        }

        // Flow 4: 120-day historical completions
        val histStart = LocalDate.now().minusDays(120).toEpochDay()
        val histEnd   = LocalDate.now().toEpochDay()
        val histCompletionsFlow = repository.getCompletionsInRange(histStart, histEnd)

        // Flow 5: 120-day historical wellness
        val histWellnessFlow = repository.getWellnessInRange(histStart, histEnd)

        // Top-level combine: exactly 5 flows (safe)
        uiState = combine(
            weekDataFlow,
            monthlyCountsFlow,
            contextFlow,
            histCompletionsFlow,
            histWellnessFlow
        ) { weekData, monthlyCounts, ctx, histComps, histWellness ->

            val habits      = weekData.habits
            val completions = weekData.completions
            val weeklyCounts = weekData.dailyCounts

            // Weekly average (respects custom frequency schedules)
            var scheduledWeek = 0
            for (i in 0..6) scheduledWeek += habits.count { isScheduledForDate(it, ctx.weekStart.plusDays(i.toLong())) }
            val weeklyAvg = if (scheduledWeek > 0)
                (weeklyCounts.sumOf { it.count }.toFloat() / scheduledWeek) * 100f else 0f

            // Monthly average
            var scheduledMonth = 0
            for (i in 1..ctx.month.lengthOfMonth()) scheduledMonth += habits.count { isScheduledForDate(it, ctx.month.atDay(i)) }
            val monthlyAvg = if (scheduledMonth > 0)
                (monthlyCounts.sumOf { it.count }.toFloat() / scheduledMonth) * 100f else 0f

            HabitTrackerUiState(
                habits               = habits,
                completions          = completions,
                dailyCounts          = weeklyCounts,
                wellnessEntry        = ctx.wellness,
                selectedMonth        = ctx.month,
                selectedWeekStart    = ctx.weekStart,
                selectedDate         = ctx.selectedDate,
                today                = LocalDate.now(),
                totalHabits          = habits.size,
                userProfile          = ctx.profile,
                weeklyAverage        = weeklyAvg,
                monthlyAverage       = monthlyAvg,
                historicalCompletions = histComps,
                historicalWellness   = histWellness
            )
        }.stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = HabitTrackerUiState()
        )
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        // Jump to first Monday on or before the 1st of that month
        val monday = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _selectedWeekStart.value = monday
        _selectedDate.value = yearMonth.atDay(1)
    }

    fun selectWeekStart(weekMonday: LocalDate) {
        // Always snap to Monday regardless of what day was passed
        val monday = weekMonday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _selectedWeekStart.value = monday
        _selectedDate.value = monday
        // Sync month display if user navigated to a different month's week
        _selectedMonth.value = YearMonth.of(monday.year, monday.month)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ── Habit CRUD ──────────────────────────────────────────────────────────

    fun addHabit(name: String, colorHex: String = "#8B5CF6", frequencyType: String = "DAILY", customDays: String = "1,2,3,4,5,6,7") {
        viewModelScope.launch {
            repository.addHabit(name.trim(), colorHex, frequencyType, customDays)
            updateStreakInDatabase()
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            updateStreakInDatabase()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            updateStreakInDatabase()
        }
    }

    /**
     * Core checkbox toggle — calls repository which does a delete-or-insert,
     * guaranteed to trigger the Room Flow to re-emit.
     */
    fun toggleHabitCompletion(habitId: Long, dateEpochDay: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, dateEpochDay)
            updateStreakInDatabase()
        }
    }

    // ── Wellness ────────────────────────────────────────────────────────────

    fun updateMood(moodIndex: Int) {
        viewModelScope.launch {
            val date    = _selectedDate.value
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(date.toEpochDay(), moodIndex, current?.sleepHours ?: 7f)
        }
    }

    fun updateSleep(hours: Float) {
        viewModelScope.launch {
            val date    = _selectedDate.value
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(date.toEpochDay(), current?.moodIndex ?: 2, hours)
        }
    }

    // ── Profile ─────────────────────────────────────────────────────────────

    fun updateProfileName(name: String) {
        viewModelScope.launch { repository.updateUserProfile(name.trim()) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = uiState.value.userProfile?.isDarkMode ?: false
            repository.updateDarkMode(!current)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val current = uiState.value.userProfile?.hapticsEnabled ?: true
            repository.updateHaptics(!current)
        }
    }

    fun toggleCompactMode() {
        viewModelScope.launch {
            val current = uiState.value.userProfile?.compactHabitGrid ?: false
            repository.updateCompactMode(!current)
        }
    }

    // ── Public Helpers ──────────────────────────────────────────────────────

    fun isCompleted(state: HabitTrackerUiState, habitId: Long, epochDay: Long): Boolean =
        state.completions.any { it.habitId == habitId && it.dateEpochDay == epochDay && it.isCompleted }

    fun isScheduledForDate(habit: Habit, date: LocalDate): Boolean {
        if (habit.frequencyType == "DAILY") return true
        val dayNum = date.dayOfWeek.value
        return habit.customDays.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayNum)
    }

    fun getDayProgress(state: HabitTrackerUiState, epochDay: Long): Float {
        val date = LocalDate.ofEpochDay(epochDay)
        val scheduled = state.habits.count { isScheduledForDate(it, date) }
        if (scheduled == 0) return 0f
        val done = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
        return (done.toFloat() / scheduled).coerceIn(0f, 1f)
    }

    // ── Streak calculation ──────────────────────────────────────────────────

    private suspend fun calculateCurrentStreak(): Int {
        val habitsList = repository.allHabits.first()
        if (habitsList.isEmpty()) return 0
        val today = LocalDate.now()
        val startRange = today.minusDays(180).toEpochDay()
        val counts = repository.getDailyCompletionCounts(startRange, today.toEpochDay()).first()

        var streak = 0
        var check  = today

        // If today isn't fully done yet, start counting from yesterday
        val todayScheduled = habitsList.count { isScheduledForDate(it, today) }
        val todayDone = counts.find { it.dateEpochDay == today.toEpochDay() }?.count ?: 0
        if (todayScheduled > 0 && todayDone < todayScheduled) check = today.minusDays(1)

        while (true) {
            val scheduled = habitsList.count { isScheduledForDate(it, check) }
            if (scheduled == 0) { check = check.minusDays(1); continue }
            val done = counts.find { it.dateEpochDay == check.toEpochDay() }?.count ?: 0
            if (done >= scheduled) { streak++; check = check.minusDays(1) } else break
        }
        return streak
    }

    private suspend fun updateStreakInDatabase() {
        repository.updateStreak(calculateCurrentStreak())
    }

    // ── Backup ──────────────────────────────────────────────────────────────

    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val data = com.habittracker.app.data.utils.BackupHelper.parseImportedData(jsonString)
            if (data != null) {
                repository.importBackup(data)
                updateStreakInDatabase()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    suspend fun getExportJsonString(): String {
        val habits      = repository.allHabits.first()
        val completions = repository.getAllCompletions().first()
        val wellness    = repository.getAllWellness().first()
        return com.habittracker.app.data.utils.BackupHelper.exportDataToJson(habits, completions, wellness)
    }
}
