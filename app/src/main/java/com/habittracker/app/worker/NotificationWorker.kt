package com.habittracker.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.app.MainActivity
import com.habittracker.app.R

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        sendReminderNotification()
        return Result.success()
    }

    private fun sendReminderNotification() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you to check in on your habits and wellness targets."
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            Pair("⚠️ Streak at Risk!", "Don't break your streak! You have habits left to complete today."),
            Pair("Future You Will Thank You 🌱", "Take 2 minutes to log your progress for today. It matters!"),
            Pair("Stay Consistent! 🔥", "Consistency is key to mastery. Open Aham to check off today's habits.")
        )
        val selectedMessage = messages.random()

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(selectedMessage.first)
            .setContentText(selectedMessage.second)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
