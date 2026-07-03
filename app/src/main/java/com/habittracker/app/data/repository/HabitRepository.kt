package com.habittracker.app.data.repository

import com.habittracker.app.data.dao.DailyCount
import com.habittracker.app.data.dao.HabitCompletionDao
import com.habittracker.app.data.dao.HabitDao
import com.habittracker.app.data.dao.WellnessDao
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val wellnessDao: WellnessDao
) {

    // ── Habits ────────────────────────────────────────────────────────────────

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun addHabit(name: String, colorHex: String) {
        habitDao.insertHabit(Habit(name = name, colorHex = colorHex))
    }

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    // ── Completions ───────────────────────────────────────────────────────────

    fun getCompletionsInRange(startDay: Long, endDay: Long): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsInRange(startDay, endDay)

    fun getDailyCompletionCounts(startDay: Long, endDay: Long): Flow<List<DailyCount>> =
        completionDao.getDailyCompletionCounts(startDay, endDay)

    suspend fun toggleCompletion(habitId: Long, dateEpochDay: Long) {
        val completed = completionDao.isCompleted(habitId, dateEpochDay)
        if (completed) {
            completionDao.deleteCompletion(habitId, dateEpochDay)
        } else {
            completionDao.upsertCompletion(HabitCompletion(habitId, dateEpochDay))
        }
    }

    // ── Wellness ──────────────────────────────────────────────────────────────

    fun getWellnessEntry(dateEpochDay: Long): Flow<WellnessEntry?> =
        wellnessDao.getWellnessEntry(dateEpochDay)

    suspend fun upsertWellness(dateEpochDay: Long, moodIndex: Int, sleepHours: Float) {
        wellnessDao.upsertWellness(WellnessEntry(dateEpochDay, moodIndex, sleepHours))
    }
}
