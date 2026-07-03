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
    val monthlyAverage: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
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

        // Initialize profile and calculate initial streak
        viewModelScope.launch {
            repository.checkAndInitProfile()
            updateStreakInDatabase()
        }

        // ── Flow: habits + week completions + week daily counts ────────────
        val habitDataFlow = _selectedWeekStart.flatMapLatest { weekStart ->
            val startDay = weekStart.toEpochDay()
            val endDay = weekStart.plusDays(6).toEpochDay()
            combine(
                repository.allHabits,
                repository.getCompletionsInRange(startDay, endDay),
                repository.getDailyCompletionCounts(startDay, endDay)
            ) { habits, completions, counts ->
                Triple(habits, completions, counts)
            }
        }

        // ── Flow: monthly counts for monthly average ────────────
        val monthlyCountsFlow = _selectedMonth.flatMapLatest { month ->
            val startDay = month.atDay(1).toEpochDay()
            val endDay = month.atEndOfMonth().toEpochDay()
            repository.getDailyCompletionCounts(startDay, endDay)
        }

        // ── Flow: wellness entry for the selected date ────────────
        val wellnessFlow = _selectedDate.flatMapLatest { date ->
            repository.getWellnessEntry(date.toEpochDay())
        }

        // ── Combine everything ────────────
        uiState = combine(
            habitDataFlow,
            monthlyCountsFlow,
            wellnessFlow,
            repository.userProfile,
            _selectedMonth,
            _selectedWeekStart,
            _selectedDate
        ) { habitTriple, monthlyCounts, wellness, profile, month, weekStart, selectedDate ->
            val habits = habitTriple.first
            val completions = habitTriple.second
            val weeklyCounts = habitTriple.third
            val totalHabits = habits.size

            // Calculate weekly average based on scheduled frequencies
            var totalScheduledWeek = 0
            for (i in 0..6) {
                val d = weekStart.plusDays(i.toLong())
                totalScheduledWeek += habits.count { isScheduledForDate(it, d) }
            }
            val weeklyAvg = if (totalScheduledWeek > 0) {
                val totalCompletions = weeklyCounts.sumOf { it.count }
                (totalCompletions.toFloat() / totalScheduledWeek) * 100f
            } else 0f

            // Calculate monthly average based on scheduled frequencies
            var totalScheduledMonth = 0
            val totalDays = month.lengthOfMonth()
            for (i in 1..totalDays) {
                val d = month.atDay(i)
                totalScheduledMonth += habits.count { isScheduledForDate(it, d) }
            }
            val monthlyAvg = if (totalScheduledMonth > 0) {
                val totalCompletions = monthlyCounts.sumOf { it.count }
                (totalCompletions.toFloat() / totalScheduledMonth) * 100f
            } else 0f

            HabitTrackerUiState(
                habits = habits,
                completions = completions,
                dailyCounts = weeklyCounts,
                wellnessEntry = wellness,
                selectedMonth = month,
                selectedWeekStart = weekStart,
                selectedDate = selectedDate,
                today = LocalDate.now(),
                totalHabits = totalHabits,
                userProfile = profile,
                weeklyAverage = weeklyAvg,
                monthlyAverage = monthlyAvg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitTrackerUiState()
        )
    }

    // ── Navigation actions ──────────────────────────────────────────────────

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        val monday = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _selectedWeekStart.value = monday
        _selectedDate.value = yearMonth.atDay(1)
    }

    fun selectWeekStart(date: LocalDate) {
        _selectedWeekStart.value = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // Sync week start if the selected date is outside current week bounds
        val currentWeek = _selectedWeekStart.value
        if (date.isBefore(currentWeek) || date.isAfter(currentWeek.plusDays(6))) {
            _selectedWeekStart.value = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
    }

    // ── Habit Actions (CRUD) ────────────────────────────────────────────────

    fun addHabit(name: String, colorHex: String = "#7C3AED", frequencyType: String = "DAILY", customDays: String = "1,2,3,4,5,6,7") {
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

    fun toggleHabitCompletion(habitId: Long, dateEpochDay: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, dateEpochDay)
            updateStreakInDatabase()
        }
    }

    // ── Wellness Actions ────────────────────────────────────────────────────

    fun updateMood(moodIndex: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = date.toEpochDay(),
                moodIndex = moodIndex,
                sleepHours = current?.sleepHours ?: 7f
            )
        }
    }

    fun updateSleep(hours: Float) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = date.toEpochDay(),
                moodIndex = current?.moodIndex ?: 2,
                sleepHours = hours
            )
        }
    }

    // ── Profile Actions ─────────────────────────────────────────────────────

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name.trim())
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    fun isCompleted(state: HabitTrackerUiState, habitId: Long, epochDay: Long): Boolean =
        state.completions.any { it.habitId == habitId && it.dateEpochDay == epochDay && it.isCompleted }

    fun isScheduledForDate(habit: Habit, date: LocalDate): Boolean {
        if (habit.frequencyType == "DAILY") return true
        val dayNum = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val daysList = habit.customDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        return dayNum in daysList
    }

    fun getDayProgress(state: HabitTrackerUiState, epochDay: Long): Float {
        val date = LocalDate.ofEpochDay(epochDay)
        val totalScheduled = state.habits.count { isScheduledForDate(it, date) }
        if (totalScheduled == 0) return 0f
        val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
        return (count.toFloat() / totalScheduled).coerceIn(0f, 1f)
    }

    // Calculates the current streak of 100% completions
    private suspend fun calculateCurrentStreak(): Int {
        val habitsList = repository.allHabits.first()
        if (habitsList.isEmpty()) return 0

        val today = LocalDate.now()
        val startRange = today.minusDays(180).toEpochDay()
        val endRange = today.toEpochDay()
        val counts = repository.getDailyCompletionCounts(startRange, endRange).first()

        var streak = 0
        var checkDate = today

        // If today is not fully completed, check if yesterday was
        val todayScheduled = habitsList.count { isScheduledForDate(it, today) }
        val todayCount = counts.find { it.dateEpochDay == today.toEpochDay() }?.count ?: 0
        val todayCompleted = if (todayScheduled > 0) todayCount == todayScheduled else true

        if (!todayCompleted) {
            checkDate = today.minusDays(1)
        }

        while (true) {
            val checkScheduled = habitsList.count { isScheduledForDate(it, checkDate) }
            // If nothing is scheduled on this day, we skip it without breaking the streak!
            if (checkScheduled == 0) {
                checkDate = checkDate.minusDays(1)
                continue
            }
            val checkCount = counts.find { it.dateEpochDay == checkDate.toEpochDay() }?.count ?: 0
            if (checkCount == checkScheduled) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private suspend fun updateStreakInDatabase() {
        val streak = calculateCurrentStreak()
        repository.updateStreak(streak)
    }

    // ── Backup Actions ────────────────────────────────────────────────────────

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
        val habits = repository.allHabits.first()
        val completions = repository.getAllCompletions().first()
        val wellness = repository.getAllWellness().first()
        return com.habittracker.app.data.utils.BackupHelper.exportDataToJson(habits, completions, wellness)
    }
}
