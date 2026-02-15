package com.ade.habittracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ade.habittracker.MainActivity
import com.ade.habittracker.R
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.model.normalizeForDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val habitId = inputData.getInt(KEY_HABIT_ID, -1)
        if (habitId < 0) return Result.success()

        val appData = runBlocking {
            HabitRepository(context).appData.first()
        } ?: return Result.success()

        if (!appData.isReminderEnabled) return Result.success()

        val today = LocalDate.now()
        val habit = appData.habits
            .firstOrNull { it.id == habitId && !it.isDailyQuest }
            ?.normalizeForDate(today)
            ?: return Result.success()

        if (!habit.reminderEnabled) return Result.success()
        if (!habit.isDueOn(today)) return Result.success()
        if (habit.isCompleted) return Result.success()

        sendReminderNotification(
            habitId = habit.id,
            displayName = sanitizeDisplayName(appData.userName),
            habitName = habit.name,
            habitWeight = habit.weight,
            schedule = habit.schedule,
            snoozeMinutes = appData.reminderSnoozeMinutes.coerceIn(5, 120),
            isSnooze = inputData.getBoolean(KEY_IS_SNOOZE, false)
        )

        return Result.success()
    }

    private fun sendReminderNotification(
        habitId: Int,
        displayName: String,
        habitName: String,
        habitWeight: Int,
        schedule: String,
        snoozeMinutes: Int,
        isSnooze: Boolean
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val appPendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(KEY_HABIT_ID, habitId)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            habitId + 10_000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortHabit = trimLabel(habitName, 26)
        val title = if (isSnooze) {
            "$displayName, lanjutkan: $shortHabit"
        } else {
            "$displayName, waktunya: $shortHabit"
        }
        val message = "Target +$habitWeight XP. Jadwal $schedule, gas sekarang biar streak aman."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(appPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Snooze ${snoozeMinutes}m", snoozePendingIntent)
            .build()

        notificationManager.notify(habitId, notification)
    }

    private fun createChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat habit berdasarkan jadwal"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_IS_SNOOZE = "is_snooze"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val ACTION_SNOOZE = "com.ade.habittracker.action.SNOOZE"
        const val CHANNEL_ID = "habit_reminder_channel"
    }

    private fun sanitizeDisplayName(rawName: String?): String {
        val trimmed = rawName?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed.equals("petualang", ignoreCase = true)) {
            return "Petualang"
        }
        return trimmed
    }

    private fun trimLabel(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        if (maxLength <= 1) return text.take(1)
        return text.take(maxLength - 1) + "..."
    }
}
