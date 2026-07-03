package com.habittracker.app.data.dao

import androidx.room.*
import com.habittracker.app.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    /**
     * Returns all completions for a given date range (inclusive).
     */
    @Query("""
        SELECT * FROM habit_completions
        WHERE dateEpochDay >= :startDay AND dateEpochDay <= :endDay
    """)
    fun getCompletionsInRange(startDay: Long, endDay: Long): Flow<List<HabitCompletion>>

    /**
     * Returns the count of completed habits for each day in a range.
     * Used to build the progress chart.
     */
    @Query("""
        SELECT dateEpochDay, COUNT(*) as count FROM habit_completions
        WHERE dateEpochDay >= :startDay AND dateEpochDay <= :endDay
        AND isCompleted = 1
        GROUP BY dateEpochDay
    """)
    fun getDailyCompletionCounts(startDay: Long, endDay: Long): Flow<List<DailyCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(completion: HabitCompletion)

    @Query("""
        DELETE FROM habit_completions
        WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay
    """)
    suspend fun deleteCompletion(habitId: Long, dateEpochDay: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM habit_completions
            WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay AND isCompleted = 1
        )
    """)
    suspend fun isCompleted(habitId: Long, dateEpochDay: Long): Boolean
}

data class DailyCount(
    val dateEpochDay: Long,
    val count: Int
)
