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
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.model.normalizeForDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class TimeSlotReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val appData = runBlocking {
            HabitRepository(context).appData.first()
        } ?: return Result.success()

        if (!appData.isReminderEnabled) return Result.success()
        val forceNotify = inputData.getBoolean(KEY_FORCE_NOTIFY, false)

        val today = LocalDate.now()
        val pendingHabits = appData.habits
            .asSequence()
            .filterNot { it.isDailyQuest }
            .filter { it.reminderEnabled }
            .map { it.normalizeForDate(today) }
            .filter { it.isDueOn(today) && !it.isCompleted }
            .toList()

        val slotLabel = inputData.getString(KEY_SLOT_LABEL) ?: "Pengingat"
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, slotLabel.hashCode())
        val displayName = sanitizeDisplayName(appData.userName)
        val focusHabit = pickPriorityHabit(pendingHabits)

        val copy = ReminderMessageFactory.buildTimeSlotReminder(
            displayName = displayName,
            focusHabitName = focusHabit?.name,
            pendingCount = pendingHabits.size,
            forceNotify = forceNotify,
            seedHint = notificationId
        )

        sendReminderNotification(
            notificationId = notificationId,
            title = copy.title,
            message = copy.message
        )

        return Result.success()
    }

    private fun pickPriorityHabit(pendingHabits: List<Habit>): Habit? {
        return pendingHabits.maxWithOrNull(
            compareBy<Habit> { it.weight }
                .thenByDescending { it.name.length }
                .thenBy { it.id }
        )
    }

    private fun sanitizeDisplayName(rawName: String?): String {
        val trimmed = rawName?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed.equals("petualang", ignoreCase = true)) {
            return "Petualang"
        }
        return trimmed
    }
    private fun sendReminderNotification(
        notificationId: Int,
        title: String,
        message: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val appPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(appPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat habit harian berdasarkan slot waktu"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_SLOT_LABEL = "slot_label"
        const val KEY_NOTIFICATION_ID = "slot_notification_id"
        const val KEY_FORCE_NOTIFY = "force_notify"
        private const val CHANNEL_ID = "habit_reminder_channel"
    }
}
