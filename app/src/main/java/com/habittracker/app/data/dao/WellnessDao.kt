package com.habittracker.app.data.dao

import androidx.room.*
import com.habittracker.app.data.model.WellnessEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Query("SELECT * FROM wellness_entries WHERE dateEpochDay = :dateEpochDay")
    fun getWellnessEntry(dateEpochDay: Long): Flow<WellnessEntry?>

    @Query("SELECT * FROM wellness_entries WHERE dateEpochDay >= :startDay AND dateEpochDay <= :endDay ORDER BY dateEpochDay ASC")
    fun getWellnessInRange(startDay: Long, endDay: Long): Flow<List<WellnessEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWellness(entry: WellnessEntry)

    @Query("DELETE FROM wellness_entries")
    suspend fun clearWellness()
}
