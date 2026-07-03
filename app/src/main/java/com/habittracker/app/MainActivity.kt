package com.habittracker.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.app.ui.screen.MainDashboardScreen
import com.habittracker.app.ui.theme.HabitTrackerTheme
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel
import com.habittracker.app.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Request POST_NOTIFICATIONS permission for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 2. Schedule daily check-in reminder notifications via WorkManager
        scheduleDailyReminder()

        setContent {
            HabitTrackerTheme {
                val viewModel: HabitTrackerViewModel = viewModel()
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }

    private fun scheduleDailyReminder() {
        val reminderRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
        .setInitialDelay(12, TimeUnit.HOURS) // Delay to notify in the evening / next cycle
        .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "habit_daily_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing to avoid rescheduling on every launch
            reminderRequest
        )
    }
}
