package com.habittracker.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        scheduleDailyReminder()

        setContent {
            val viewModel: HabitTrackerViewModel = viewModel()
            // Collect the user's saved dark-mode preference from Room
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDarkMode = uiState.userProfile?.isDarkMode ?: false

            HabitTrackerTheme(darkTheme = isDarkMode) {
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }

    private fun scheduleDailyReminder() {
        val reminderRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "habit_daily_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}
