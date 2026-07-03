package com.habittracker.app.data.database

import android.content.Context
import androidx.room.*
import com.habittracker.app.data.dao.HabitCompletionDao
import com.habittracker.app.data.dao.HabitDao
import com.habittracker.app.data.dao.WellnessDao
import com.habittracker.app.data.dao.UserProfileDao
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import com.habittracker.app.data.model.UserProfile

@Database(
    entities = [Habit::class, HabitCompletion::class, WellnessEntry::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun wellnessDao(): WellnessDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
