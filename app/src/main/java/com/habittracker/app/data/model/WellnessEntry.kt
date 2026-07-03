package com.habittracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores daily wellness data: mood and sleep hours.
 * dateEpochDay = LocalDate.toEpochDay()
 */
@Entity(tableName = "wellness_entries")
data class WellnessEntry(
    @PrimaryKey
    val dateEpochDay: Long,         // One entry per calendar day
    val moodIndex: Int = 2,         // 0=Awful,1=Bad,2=Okay,3=Good,4=Great
    val sleepHours: Float = 7f      // 0..12 in 0.5 increments
)
