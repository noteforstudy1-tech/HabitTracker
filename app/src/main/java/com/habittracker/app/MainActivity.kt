package com.habittracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.app.ui.screen.MainDashboardScreen
import com.habittracker.app.ui.theme.HabitTrackerTheme
import com.habittracker.app.ui.viewmodel.HabitTrackerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                val viewModel: HabitTrackerViewModel = viewModel()
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
