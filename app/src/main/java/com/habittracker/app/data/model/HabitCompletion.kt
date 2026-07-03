package com.habittracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Tracks which habits were completed on which date.
 * The composite primary key (habitId, dateEpochDay) ensures
 * one entry per habit per calendar day.
 * dateEpochDay = LocalDate.toEpochDay() for easy date math.
 */
@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "dateEpochDay"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitCompletion(
    val habitId: Long,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val isCompleted: Boolean = true
)
