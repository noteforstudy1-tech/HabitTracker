package com.habittracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Your Name",
    val currentStreak: Int = 0,
    val isDarkMode: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val compactHabitGrid: Boolean = false
)
