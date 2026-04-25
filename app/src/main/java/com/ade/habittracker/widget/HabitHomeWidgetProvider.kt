package com.ade.habittracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.ade.habittracker.R
import com.ade.habittracker.SplashActivity
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitHistoryItem
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.model.normalizeForDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class HabitHomeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1)
            if (habitId > 0) {
                toggleHabitFromWidget(context, habitId)
            }
        }

        refreshAll(context)
    }

    companion object {
        private const val ACTION_TOGGLE_HABIT = "com.ade.habittracker.widget.ACTION_TOGGLE_HABIT"
        private const val EXTRA_HABIT_ID = "extra_habit_id"

        private enum class WidgetSize {
            MINI,
            MEDIUM,
            LARGE
        }

        private val rowIds = intArrayOf(R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4)
        private val titleIds = intArrayOf(R.id.habit_title_1, R.id.habit_title_2, R.id.habit_title_3, R.id.habit_title_4)
        private val actionIds = intArrayOf(R.id.habit_action_1, R.id.habit_action_2, R.id.habit_action_3, R.id.habit_action_4)

        fun refreshAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitHomeWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    buildWidgetViews(context, appWidgetManager, appWidgetId)
                )
            }
        }

        private fun buildWidgetViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_habits)
            val openAppIntent = openAppPendingIntent(context, appWidgetId * 11)
            val widgetSize = resolveWidgetSize(appWidgetManager, appWidgetId)

            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_title, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_subtitle, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_summary, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_focus, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_stats_row, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_stat_level, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_stat_focus, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_stat_tickets, openAppIntent)
            views.setOnClickPendingIntent(R.id.mini_container, openAppIntent)
            views.setOnClickPendingIntent(R.id.mini_title, openAppIntent)
            views.setOnClickPendingIntent(R.id.mini_progress, openAppIntent)
            views.setOnClickPendingIntent(R.id.mini_subtitle, openAppIntent)

            views.setViewVisibility(R.id.mini_container, View.GONE)
            views.setViewVisibility(R.id.full_container, View.VISIBLE)
            views.setViewVisibility(R.id.progress_section, View.GONE)
            views.setViewVisibility(R.id.widget_streak, View.GONE)
            views.setViewVisibility(R.id.row_4, View.GONE)

            val data = runBlocking { HabitRepository(context).appData.first() } ?: AppData()
            val today = LocalDate.now()
            val normalizedHabits = data.habits.map { habit ->
                if (habit.isDailyQuest) habit else habit.normalizeForDate(today)
            }
            val visibleHabitCount = if (widgetSize == WidgetSize.LARGE) 4 else 3
            val todayHabits = normalizedHabits
                .filter { it.isDueOn(today) }
                .sortedWith(compareBy<Habit> { it.isCompleted }.thenBy { it.name.lowercase() })
                .take(visibleHabitCount)
            val totalToday = normalizedHabits.count { it.isDueOn(today) }
            val completedToday = normalizedHabits.count { it.isDueOn(today) && it.isCompleted }
            val pendingToday = normalizedHabits
                .filter { it.isDueOn(today) && !it.isCompleted && !it.isDailyQuest }
            val focusHabit = pendingToday
                .sortedWith(compareByDescending<Habit> { it.weight }.thenBy { it.name.lowercase() })
                .firstOrNull()
            val displayName = sanitizeDisplayName(data.userName)
            val dayLabel = today.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("id-ID")))
            val progressPercent = if (totalToday > 0) {
                ((completedToday * 100f) / totalToday).toInt().coerceIn(0, 100)
            } else {
                0
            }
            val levelLabel = "Lv ${data.level}"
            val focusLabel = "Fokus ${data.focusSessionsCompletedToday}"
            val ticketLabel = "Tiket ${data.tickets}"
            val widgetAccent = resolveWidgetAccentColor(data)
            val widgetTicketAccent = if (data.tickets > 0) 0xFFA5D6FF.toInt() else 0x80FFFFFF.toInt()

            when (widgetSize) {
                WidgetSize.MINI -> {
                    views.setViewVisibility(R.id.mini_container, View.VISIBLE)
                    views.setViewVisibility(R.id.full_container, View.GONE)
                    views.setTextViewText(R.id.mini_title, trimLabel(displayName, 12))
                    views.setTextViewText(R.id.mini_progress, "$progressPercent%")
                    views.setTextViewText(
                        R.id.mini_subtitle,
                        "Lv ${data.level} • $completedToday/$totalToday misi"
                    )
                }
                WidgetSize.MEDIUM -> {
                    views.setViewVisibility(R.id.mini_container, View.GONE)
                    views.setViewVisibility(R.id.full_container, View.VISIBLE)
                    views.setViewVisibility(R.id.progress_section, View.GONE)
                    views.setViewVisibility(R.id.widget_streak, View.GONE)
                }
                WidgetSize.LARGE -> {
                    views.setViewVisibility(R.id.mini_container, View.GONE)
                    views.setViewVisibility(R.id.full_container, View.VISIBLE)
                    views.setViewVisibility(R.id.progress_section, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_streak, View.VISIBLE)
                    views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                    views.setTextViewText(R.id.widget_progress_percent, "$progressPercent%")
                    views.setTextViewText(R.id.widget_streak, "Streak: ${data.streak} hari")
                }
            }

            if (!data.isLoggedIn) {
                if (widgetSize == WidgetSize.MINI) {
                    views.setTextViewText(R.id.mini_title, "Belum Login")
                    views.setTextViewText(R.id.mini_progress, "Tap")
                    views.setTextViewText(R.id.mini_subtitle, "Buka aplikasi dulu")
                } else {
                    views.setTextViewText(R.id.widget_title, "Habit Tracker")
                    views.setTextViewText(R.id.widget_subtitle, dayLabel)
                    views.setTextViewText(R.id.widget_summary, "Masuk dulu untuk lihat misi hari ini.")
                    views.setTextViewText(R.id.widget_focus, "Login untuk aktifkan rekomendasi prioritas")
                    views.setTextViewText(R.id.widget_stat_level, "Lv -")
                    views.setTextViewText(R.id.widget_stat_focus, "Fokus -")
                    views.setTextViewText(R.id.widget_stat_tickets, "Tiket -")
                    showSingleInfoRow(views, "Tap untuk buka aplikasi", openAppIntent)
                }
                return views
            }

            views.setTextViewText(R.id.widget_title, "Halo, ${trimLabel(displayName, 16)}")
            views.setTextViewText(R.id.widget_subtitle, dayLabel)
            views.setTextViewText(
                R.id.widget_summary,
                "$completedToday/$totalToday misi selesai ($progressPercent%)"
            )
            views.setTextViewText(
                R.id.widget_focus,
                if (focusHabit != null) {
                    "Prioritas: ${trimLabel(focusHabit.name, 24)} (+${focusHabit.weight} XP)"
                } else {
                    "Prioritas: semua misi hari ini beres"
                }
            )
            if (widgetSize == WidgetSize.LARGE) {
                views.setTextViewText(R.id.widget_title, "BitQuest Lv ${data.level}")
                views.setTextViewText(R.id.widget_subtitle, "${trimLabel(displayName, 14)} - $dayLabel")
            }
            views.setTextViewText(R.id.widget_stat_level, levelLabel)
            views.setTextViewText(R.id.widget_stat_focus, focusLabel)
            views.setTextViewText(R.id.widget_stat_tickets, ticketLabel)
            views.setTextColor(R.id.widget_title, widgetAccent)
            views.setTextColor(R.id.widget_focus, widgetAccent)
            views.setTextColor(R.id.widget_progress_percent, widgetAccent)
            views.setTextColor(R.id.widget_stat_level, widgetAccent)
            views.setTextColor(R.id.widget_stat_tickets, widgetTicketAccent)

            if (widgetSize == WidgetSize.MINI) {
                views.setTextViewText(R.id.mini_title, trimLabel(displayName, 12))
                views.setTextViewText(R.id.mini_progress, "$progressPercent%")
                views.setTextViewText(
                    R.id.mini_subtitle,
                    "Lv ${data.level} • Streak ${data.streak}"
                )
                return views
            }

            if (todayHabits.isEmpty()) {
                showSingleInfoRow(views, "Tidak ada misi terjadwal hari ini.", openAppIntent)
                return views
            }

            rowIds.forEachIndexed { index, rowId ->
                val habit = todayHabits.getOrNull(index)
                if (habit == null) {
                    views.setViewVisibility(rowId, View.GONE)
                    return@forEachIndexed
                }

                views.setViewVisibility(rowId, View.VISIBLE)
                views.setTextViewText(
                    titleIds[index],
                    if (habit.isCompleted) {
                        "\u2713 ${trimLabel(habit.name, 24)}"
                    } else {
                        "${trimLabel(habit.name, 22)} (+${habit.weight})"
                    }
                )
                views.setImageViewResource(
                    actionIds[index],
                    if (habit.isCompleted) android.R.drawable.checkbox_on_background
                    else android.R.drawable.checkbox_off_background
                )

                views.setOnClickPendingIntent(titleIds[index], openAppIntent)

                val actionIntent = if (habit.isCompleted) {
                    openAppIntent
                } else {
                    toggleHabitPendingIntent(context, appWidgetId, habit.id, index)
                }
                views.setOnClickPendingIntent(actionIds[index], actionIntent)
            }

            return views
        }

        private fun resolveWidgetSize(
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): WidgetSize {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth)
            val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight)

            val width = maxOf(minWidth, maxWidth)
            val height = maxOf(minHeight, maxHeight)

            return when {
                width <= 140 && height <= 140 -> WidgetSize.MINI
                width >= 280 || height >= 180 -> WidgetSize.LARGE
                else -> WidgetSize.MEDIUM
            }
        }

        private fun showSingleInfoRow(
            views: RemoteViews,
            message: String,
            pendingIntent: PendingIntent
        ) {
            rowIds.forEach { views.setViewVisibility(it, View.GONE) }
            views.setViewVisibility(R.id.row_1, View.VISIBLE)
            views.setTextViewText(R.id.habit_title_1, message)
            views.setImageViewResource(R.id.habit_action_1, android.R.drawable.ic_menu_view)
            views.setOnClickPendingIntent(R.id.habit_title_1, pendingIntent)
            views.setOnClickPendingIntent(R.id.habit_action_1, pendingIntent)
        }

        private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun toggleHabitPendingIntent(
            context: Context,
            appWidgetId: Int,
            habitId: Int,
            slotIndex: Int
        ): PendingIntent {
            val intent = Intent(context, HabitHomeWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_HABIT
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val requestCode = appWidgetId * 100 + slotIndex + habitId
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun toggleHabitFromWidget(context: Context, habitId: Int) {
            runBlocking {
                val repository = HabitRepository(context)
                val currentData = repository.appData.first() ?: return@runBlocking
                val today = LocalDate.now()
                val todayStr = today.toString()

                val targetHabit = currentData.habits.find { it.id == habitId } ?: return@runBlocking
                val normalizedTarget = if (targetHabit.isDailyQuest) targetHabit else targetHabit.normalizeForDate(today)

                if (!normalizedTarget.isDueOn(today) || normalizedTarget.isCompleted) return@runBlocking

                val updatedHabits = currentData.habits.map { habit ->
                    if (habit.id == habitId) {
                        val updatedDates = (habit.completionDates + todayStr).distinct().sorted()
                        val updated = habit.copy(completionDates = updatedDates, isCompleted = true)
                        if (habit.isDailyQuest) updated else updated.normalizeForDate(today)
                    } else {
                        if (habit.isDailyQuest) habit else habit.normalizeForDate(today)
                    }
                }

                var updatedData = currentData.copy(
                    habits = updatedHabits,
                    totalXp = (currentData.totalXp + normalizedTarget.weight).coerceAtLeast(0),
                    level = ((currentData.totalXp + normalizedTarget.weight).coerceAtLeast(0) / 100) + 1,
                    totalHabitsCompleted = (currentData.totalHabitsCompleted + 1).coerceAtLeast(0),
                    history = currentData.history + HabitHistoryItem(
                        id = UUID.randomUUID().toString(),
                        habitId = normalizedTarget.id,
                        habitName = normalizedTarget.name,
                        xpEarned = normalizedTarget.weight,
                        timestamp = System.currentTimeMillis()
                    )
                )

                val dueHabits = updatedHabits.filter { !it.isDailyQuest && it.isDueOn(today) }
                if (dueHabits.isNotEmpty() && dueHabits.all { it.isCompleted }) {
                    updatedData = applyStreakUpdate(updatedData, today)
                }

                repository.saveAppData(updatedData)
            }
        }

        private fun applyStreakUpdate(data: AppData, today: LocalDate): AppData {
            val todayStr = today.toString()
            if (data.lastCompletionDate == todayStr) return data

            val yesterdayStr = today.minusDays(1).toString()
            val newStreak = if (data.lastCompletionDate == yesterdayStr) data.streak + 1 else 1
            return data.copy(
                streak = newStreak,
                lastCompletionDate = todayStr
            )
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

        private fun resolveWidgetAccentColor(data: AppData): Int {
            return when (data.activeTheme) {
                "theme_ramadhan" -> 0xFF6ED39B.toInt()
                "theme_ramadhan_festive" -> 0xFF9AF5C0.toInt()
                "theme_ikuyo" -> 0xFFFF7085.toInt()
                "theme_hutao" -> 0xFFFF8A65.toInt()
                "theme_furina" -> 0xFF7ED8FF.toInt()
                "theme_moonlit" -> 0xFF99D9FF.toInt()
                "theme_forest_camp" -> 0xFF8EE7A8.toInt()
                "theme_arcade" -> 0xFFAA9BFF.toInt()
                "theme_ocean" -> 0xFF68C7FF.toInt()
                "theme_forest" -> 0xFF7DFF9A.toInt()
                "theme_sunset" -> 0xFFFFB066.toInt()
                "theme_royal" -> 0xFFFFD54F.toInt()
                "theme_blood" -> 0xFFFF7A7A.toInt()
                else -> when {
                    data.level >= 50 -> 0xFFFF9BC6.toInt()
                    data.level >= 40 -> 0xFFCFA7FF.toInt()
                    data.level >= 30 -> 0xFF83C8FF.toInt()
                    data.level >= 20 -> 0xFF67E8F9.toInt()
                    data.level >= 10 -> 0xFF92E6A7.toInt()
                    else -> 0xFFFFE082.toInt()
                }
            }
        }
    }
}

