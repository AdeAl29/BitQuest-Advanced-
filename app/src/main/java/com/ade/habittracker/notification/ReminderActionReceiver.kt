package com.ade.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != HabitReminderWorker.ACTION_SNOOZE) return

        val habitId = intent.getIntExtra(HabitReminderWorker.KEY_HABIT_ID, -1)
        if (habitId < 0) return

        val snoozeMinutes = intent
            .getIntExtra(HabitReminderWorker.EXTRA_SNOOZE_MINUTES, 15)
            .coerceIn(5, 120)

        val snoozeWork = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(snoozeMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    HabitReminderWorker.KEY_HABIT_ID to habitId,
                    HabitReminderWorker.KEY_IS_SNOOZE to true
                )
            )
            .addTag("habit_reminder_snooze")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "habit_reminder_snooze_$habitId",
            ExistingWorkPolicy.REPLACE,
            snoozeWork
        )
    }
}
