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

            // Calculate weekly average
            val weeklyAvg = if (totalHabits > 0) {
                val totalCompletions = weeklyCounts.sumOf { it.count }
                (totalCompletions.toFloat() / (totalHabits * 7)) * 100f
            } else 0f

            // Calculate monthly average
            val monthlyAvg = if (totalHabits > 0) {
                val totalDays = month.lengthOfMonth()
                val totalCompletions = monthlyCounts.sumOf { it.count }
                (totalCompletions.toFloat() / (totalHabits * totalDays)) * 100f
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

    fun addHabit(name: String, colorHex: String = "#7C3AED") {
        viewModelScope.launch {
            repository.addHabit(name.trim(), colorHex)
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

    fun getDayProgress(state: HabitTrackerUiState, epochDay: Long): Float {
        if (state.totalHabits == 0) return 0f
        val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
        return (count.toFloat() / state.totalHabits).coerceIn(0f, 1f)
    }

    // Calculates the current streak of 100% completions
    private suspend fun calculateCurrentStreak(): Int {
        val habitsList = repository.allHabits.first()
        if (habitsList.isEmpty()) return 0
        val total = habitsList.size

        val today = LocalDate.now()
        val startRange = today.minusDays(180).toEpochDay()
        val endRange = today.toEpochDay()
        val counts = repository.getDailyCompletionCounts(startRange, endRange).first()

        var streak = 0
        var checkDate = today

        // If today is not fully completed, check if yesterday was
        val todayCount = counts.find { it.dateEpochDay == today.toEpochDay() }?.count ?: 0
        val todayCompleted = todayCount == total

        if (!todayCompleted) {
            checkDate = today.minusDays(1)
        }

        while (true) {
            val checkCount = counts.find { it.dateEpochDay == checkDate.toEpochDay() }?.count ?: 0
            if (checkCount == total) {
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
}
