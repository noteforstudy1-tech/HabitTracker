package com.habittracker.app.data.repository

import com.habittracker.app.data.dao.DailyCount
import com.habittracker.app.data.dao.HabitCompletionDao
import com.habittracker.app.data.dao.HabitDao
import com.habittracker.app.data.dao.WellnessDao
import com.habittracker.app.data.dao.UserProfileDao
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val wellnessDao: WellnessDao,
    private val userProfileDao: UserProfileDao
) {

    // ── User Profile ──────────────────────────────────────────────────────────

    val userProfile: Flow<UserProfile?> = userProfileDao.getProfile()

    suspend fun checkAndInitProfile() {
        if (userProfileDao.profileExists() == 0) {
            userProfileDao.upsertProfile(UserProfile(name = "Raghav Parashar", currentStreak = 0))
        }
    }

    suspend fun updateUserProfile(name: String) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(name = name))
    }

    suspend fun updateStreak(streak: Int) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(currentStreak = streak))
    }

    // ── Habits ────────────────────────────────────────────────────────────────

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun addHabit(name: String, colorHex: String) {
        habitDao.insertHabit(Habit(name = name, colorHex = colorHex))
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
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

    fun getWellnessInRange(startDay: Long, endDay: Long): Flow<List<WellnessEntry>> =
        wellnessDao.getWellnessInRange(startDay, endDay)

    suspend fun upsertWellness(dateEpochDay: Long, moodIndex: Int, sleepHours: Float) {
        wellnessDao.upsertWellness(WellnessEntry(dateEpochDay, moodIndex, sleepHours))
    }
}
