package com.habittracker.app

import android.app.Application
import com.habittracker.app.data.database.HabitDatabase

class HabitTrackerApplication : Application() {
    val database: HabitDatabase by lazy { HabitDatabase.getDatabase(this) }
}
