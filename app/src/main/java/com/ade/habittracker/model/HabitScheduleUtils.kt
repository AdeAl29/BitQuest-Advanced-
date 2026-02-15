package com.ade.habittracker.model

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters

private const val MAX_COMPLETION_HISTORY_DAYS = 120L

fun Habit.scheduleLabel(): String {
    return when (scheduleType) {
        HabitScheduleType.DAILY -> "Setiap Hari"
        HabitScheduleType.WEEKDAYS -> "Senin-Jumat"
        HabitScheduleType.TIMES_PER_WEEK -> "${targetPerWeek.coerceIn(1, 7)}x / Minggu"
        HabitScheduleType.CUSTOM_DAYS -> {
            val labels = customDays
                .distinct()
                .sorted()
                .mapNotNull { isoDayToShortName(it) }
            if (labels.isEmpty()) schedule.ifBlank { "Custom" } else labels.joinToString(", ")
        }
    }
}

fun Habit.isDueOn(date: LocalDate): Boolean {
    if (isDailyQuest) return true

    return when (scheduleType) {
        HabitScheduleType.DAILY -> true
        HabitScheduleType.WEEKDAYS -> date.dayOfWeek.value in 1..5
        HabitScheduleType.TIMES_PER_WEEK -> {
            val target = targetPerWeek.coerceIn(1, 7)
            completedCountInWeek(date) < target || isCompletedOn(date)
        }
        HabitScheduleType.CUSTOM_DAYS -> {
            val days = customDays.distinct()
            if (days.isEmpty()) true else date.dayOfWeek.value in days
        }
    }
}

fun Habit.isCompletedOn(date: LocalDate): Boolean {
    val dateKey = date.toString()
    return completionDates.any { it == dateKey }
}

fun Habit.completedCountInWeek(date: LocalDate): Int {
    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val endOfWeek = startOfWeek.plusDays(6)

    return completionDates
        .asSequence()
        .mapNotNull { it.toLocalDateOrNull() }
        .count { !it.isBefore(startOfWeek) && !it.isAfter(endOfWeek) }
}

fun Habit.normalizeForDate(date: LocalDate): Habit {
    val minDate = date.minusDays(MAX_COMPLETION_HISTORY_DAYS)

    val sanitizedCompletionDates = completionDates
        .asSequence()
        .mapNotNull { it.toLocalDateOrNull() }
        .filter { !it.isBefore(minDate) && !it.isAfter(date) }
        .distinct()
        .sorted()
        .map { it.toString() }
        .toList()

    val sanitizedHabit = copy(completionDates = sanitizedCompletionDates)
    val completedToday = sanitizedHabit.isCompletedOn(date)

    val completedState = when (sanitizedHabit.scheduleType) {
        HabitScheduleType.TIMES_PER_WEEK -> {
            val target = sanitizedHabit.targetPerWeek.coerceIn(1, 7)
            completedToday || sanitizedHabit.completedCountInWeek(date) >= target
        }
        else -> if (sanitizedHabit.isDueOn(date)) completedToday else false
    }

    return sanitizedHabit.copy(
        isCompleted = completedState,
        schedule = sanitizedHabit.scheduleLabel()
    )
}

private fun isoDayToShortName(value: Int): String? {
    return when (value) {
        1 -> "Sen"
        2 -> "Sel"
        3 -> "Rab"
        4 -> "Kam"
        5 -> "Jum"
        6 -> "Sab"
        7 -> "Min"
        else -> null
    }
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}
