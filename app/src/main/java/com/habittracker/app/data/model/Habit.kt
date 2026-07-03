package com.habittracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#7C3AED", // Default purple accent
    val createdAt: Long = System.currentTimeMillis(),
    val frequencyType: String = "DAILY", // "DAILY" or "SPECIFIC_DAYS"
    val customDays: String = "1,2,3,4,5,6,7" // Comma-separated day-of-week integers (1 = Monday, 7 = Sunday)
)
