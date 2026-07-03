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
    val selectedWeekStart: LocalDate = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val today: LocalDate = LocalDate.now(),
    val totalHabits: Int = 0
)

// Internal container to avoid 6-flow combine (max typed overload is 5)
private data class HabitData(
    val habits: List<Habit>,
    val completions: List<HabitCompletion>,
    val dailyCounts: List<DailyCount>
)

private data class ContextData(
    val wellness: WellnessEntry?,
    val month: YearMonth,
    val weekStart: LocalDate
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    private val _selectedMonth    = MutableStateFlow(YearMonth.now())
    private val _selectedWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )

    val uiState: StateFlow<HabitTrackerUiState>

    init {
        val db = (application as HabitTrackerApplication).database
        repository = HabitRepository(db.habitDao(), db.habitCompletionDao(), db.wellnessDao())

        viewModelScope.launch { seedDefaultHabitsIfEmpty() }

        // ── Flow 1-3: habit list + week completions + daily counts ────────────
        val habitDataFlow: Flow<HabitData> = _selectedWeekStart.flatMapLatest { weekStart ->
            val startDay = weekStart.toEpochDay()
            val endDay   = weekStart.plusDays(6).toEpochDay()
            combine(
                repository.allHabits,
                repository.getCompletionsInRange(startDay, endDay),
                repository.getDailyCompletionCounts(startDay, endDay)
            ) { habits, completions, counts ->
                HabitData(habits, completions, counts)
            }
        }

        // ── Flow 4-6: wellness entry + selected month + selected week ─────────
        val contextDataFlow: Flow<ContextData> = combine(
            repository.getWellnessEntry(LocalDate.now().toEpochDay()),
            _selectedMonth,
            _selectedWeekStart
        ) { wellness, month, weekStart ->
            ContextData(wellness, month, weekStart)
        }

        // ── Final combine: only 2 flows → well within typed-overload limit ────
        uiState = combine(habitDataFlow, contextDataFlow) { habitData, ctx ->
            HabitTrackerUiState(
                habits           = habitData.habits,
                completions      = habitData.completions,
                dailyCounts      = habitData.dailyCounts,
                wellnessEntry    = ctx.wellness,
                selectedMonth    = ctx.month,
                selectedWeekStart = ctx.weekStart,
                today            = LocalDate.now(),
                totalHabits      = habitData.habits.size
            )
        }.stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitTrackerUiState()
        )
    }

    // ── Public actions ────────────────────────────────────────────────────────

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        val monday = yearMonth.atDay(1)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _selectedWeekStart.value = monday
    }

    fun selectWeekStart(date: LocalDate) {
        _selectedWeekStart.value =
            date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun toggleHabitCompletion(habitId: Long, dateEpochDay: Long) {
        viewModelScope.launch { repository.toggleCompletion(habitId, dateEpochDay) }
    }

    fun addHabit(name: String, colorHex: String = "#7C3AED") {
        viewModelScope.launch { repository.addHabit(name.trim(), colorHex) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun updateMood(moodIndex: Int) {
        viewModelScope.launch {
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = LocalDate.now().toEpochDay(),
                moodIndex    = moodIndex,
                sleepHours   = current?.sleepHours ?: 7f
            )
        }
    }

    fun updateSleep(hours: Float) {
        viewModelScope.launch {
            val current = uiState.value.wellnessEntry
            repository.upsertWellness(
                dateEpochDay = LocalDate.now().toEpochDay(),
                moodIndex    = current?.moodIndex ?: 2,
                sleepHours   = hours
            )
        }
    }

    // ── Helpers used by the UI ────────────────────────────────────────────────

    fun isCompleted(state: HabitTrackerUiState, habitId: Long, epochDay: Long): Boolean =
        state.completions.any { it.habitId == habitId && it.dateEpochDay == epochDay && it.isCompleted }

    fun getDayProgress(state: HabitTrackerUiState, epochDay: Long): Float {
        if (state.totalHabits == 0) return 0f
        val count = state.dailyCounts.find { it.dateEpochDay == epochDay }?.count ?: 0
        return (count.toFloat() / state.totalHabits).coerceIn(0f, 1f)
    }

    // ── Seed ──────────────────────────────────────────────────────────────────

    private suspend fun seedDefaultHabitsIfEmpty() {
        repository.allHabits.first().let { habits ->
            if (habits.isEmpty()) {
                listOf(
                    "Morning Workout" to "#E11D48",
                    "Read 30 min"     to "#7C3AED",
                    "Meditate"        to "#0891B2",
                    "Drink 2L Water"  to "#059669",
                    "No Sugar"        to "#D97706"
                ).forEach { (name, color) -> repository.addHabit(name, color) }
            }
        }
    }
}
