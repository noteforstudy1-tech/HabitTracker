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
            userProfileDao.upsertProfile(UserProfile(name = "Your Name", currentStreak = 0, isDarkMode = false))
        }
    }

    suspend fun updateUserProfile(name: String) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(name = name))
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(isDarkMode = isDark))
    }

    suspend fun updateHaptics(enabled: Boolean) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(hapticsEnabled = enabled))
    }

    suspend fun updateCompactMode(enabled: Boolean) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(compactHabitGrid = enabled))
    }

    suspend fun updateStreak(streak: Int) {
        val current = userProfile.first() ?: UserProfile()
        userProfileDao.upsertProfile(current.copy(currentStreak = streak))
    }

    // ── Habits ────────────────────────────────────────────────────────────────

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun addHabit(name: String, colorHex: String, frequencyType: String = "DAILY", customDays: String = "1,2,3,4,5,6,7") {
        habitDao.insertHabit(Habit(name = name, colorHex = colorHex, frequencyType = frequencyType, customDays = customDays))
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

    /**
     * Toggle: if already completed → delete the row entirely.
     * If not completed → insert with isCompleted=true.
     * Using delete-then-insert (not flip) ensures the Flow emits a real change event.
     */
    suspend fun toggleCompletion(habitId: Long, dateEpochDay: Long) {
        val alreadyDone = completionDao.isCompleted(habitId, dateEpochDay)
        if (alreadyDone) {
            completionDao.deleteCompletion(habitId, dateEpochDay)
        } else {
            completionDao.upsertCompletion(HabitCompletion(habitId = habitId, dateEpochDay = dateEpochDay, isCompleted = true))
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

    // ── Backup / Export Support ───────────────────────────────────────────────

    fun getAllCompletions(): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsInRange(0, Long.MAX_VALUE)

    fun getAllWellness(): Flow<List<WellnessEntry>> =
        wellnessDao.getWellnessInRange(0, Long.MAX_VALUE)

    suspend fun importBackup(data: com.habittracker.app.data.utils.ImportedData) {
        habitDao.clearHabits()
        completionDao.clearCompletions()
        wellnessDao.clearWellness()

        data.habits.forEach { habitDao.insertHabit(it) }
        data.completions.forEach { completionDao.upsertCompletion(it) }
        data.wellness.forEach { wellnessDao.upsertWellness(it) }
    }
}
