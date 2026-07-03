package com.habittracker.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.app.HabitTrackerApplication
import com.habittracker.app.data.dao.DailyCount
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.data.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class HabitTrackerUiState(
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val dailyCounts: List<DailyCount> = emptyList(),
    val wellnessEntry: WellnessEntry? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedWeekStart: LocalDate = LocalDate.now().with(
        java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
    ),
    val today: LocalDate = LocalDate.now(),
    val totalHabits: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedWeekStart = MutableStateFlow(
        LocalDate.now().with(
            java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
        )
    )

    val uiState: StateFlow<HabitTrackerUiState>

    init {
        val db = (application as HabitTrackerApplication).database
        repository = HabitRepository(db.habitDao(), db.habitCompletionDao(), db.wellnessDao())

        // Seed default habits if none exist
        viewModelScope.launch { seedDefaultHabitsIfEmpty() }

        val habitsFlow = repository.allHabits

        val completionsFlow = _selectedWeekStart.flatMapLatest { weekStart ->
            val startDay = weekStart.toEpochDay()
            val endDay = weekStart.plusDays(6).toEpochDay()
            repository.getCompletionsInRange(startDay, endDay)
        }

        val dailyCountsFlow = _selectedWeekStart.flatMapLatest { weekStart ->
            val startDay = weekStart.toEpochDay()
            val endDay = weekStart.plusDays(6).toEpochDay()
            repository.getDailyCompletionCounts(startDay, endDay)
        }

        val wellnessFlow = flow {
            repository.getWellnessEntry(LocalDate.now().toEpochDay()).collect { emit(it) }
        }

        uiState = combine(
            habitsFlow,
            completionsFlow,
            dailyCountsFlow,
            wellnessFlow,
            _selectedMonth,
            _selectedWeekStart
        ) { habits, completions, dailyCounts, wellness, month, weekStart ->
            HabitTrackerUiState(
                habits = habits,
                completions = completions,
                dailyCounts = dailyCounts,
                wellnessEntry = wellness,
                selectedMonth = month,
                selectedWeekStart = weekStart,
                today = LocalDate.now(),
                totalHabits = habits.size
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitTrackerUiState()
        )
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        // Move week start to the first Monday of that month
        val firstOfMonth = yearMonth.atDay(1)
        val mondayOfFirstWeek = firstOfMonth.with(
            java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)
        )
        _selectedWeekStart.value = mondayOfFirstWeek
    }

    fun selectWeekStart(date: LocalDate) {
        _selectedWeekStart.value = date
    }

    fun toggleHabitCompletion(habitId: Long, dateEpochDay: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, dateEpochDay)
        }
    }

    fun addHabit(name: String, colorHex: String = "#7C3AED") {
        viewModelScope.launch {
            repository.addHabit(name.trim(), colorHex)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun updateMood(moodIndex: Int) {
        viewModelScope.launch {
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = LocalDate.now().toEpochDay(),
                moodIndex = moodIndex,
                sleepHours = current?.sleepHours ?: 7f
            )
        }
    }

    fun updateSleep(hours: Float) {
        viewModelScope.launch {
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = LocalDate.now().toEpochDay(),
                moodIndex = current?.moodIndex ?: 2,
                sleepHours = hours
            )
        }
    }

    // Helper: check if a habit is completed for a given epoch day
    fun isCompleted(state: HabitTrackerUiState, habitId: Long, epochDay: Long): Boolean =
        state.completions.any { it.habitId == habitId && it.dateEpochDay == epochDay && it.isCompleted }

    // Helper: get completion % for a day (0..1f)
    fun getDayProgress(state: HabitTrackerUiState, epochDay: Long): Float {
        if (state.totalHabits == 0) return 0f
        val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
        return (count.toFloat() / state.totalHabits.toFloat()).coerceIn(0f, 1f)
    }

    private suspend fun seedDefaultHabitsIfEmpty() {
        repository.allHabits.first().let { habits ->
            if (habits.isEmpty()) {
                val defaults = listOf(
                    "Morning Workout" to "#E11D48",
                    "Read 30 min" to "#7C3AED",
                    "Meditate" to "#0891B2",
                    "Drink 2L Water" to "#059669",
                    "No Sugar" to "#D97706"
                )
                defaults.forEach { (name, color) -> repository.addHabit(name, color) }
            }
        }
    }
}
