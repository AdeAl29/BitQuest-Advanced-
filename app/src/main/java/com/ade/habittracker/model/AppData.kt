package com.ade.habittracker.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class HabitScheduleType {
    DAILY,
    WEEKDAYS,
    TIMES_PER_WEEK,
    CUSTOM_DAYS
}

@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val schedule: String,
    val weight: Int,
    val isCompleted: Boolean = false,
    val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    val customDays: List<Int> = emptyList(), // ISO day number (1 = Monday, 7 = Sunday)
    val targetPerWeek: Int = 3, // Used when scheduleType == TIMES_PER_WEEK
    val completionDates: List<String> = emptyList(), // yyyy-MM-dd
    val reminderEnabled: Boolean = true,
    val reminderTime: String? = null, // HH:mm
    val isDailyQuest: Boolean = false
)

@Serializable
data class HabitHistoryItem(
    val id: String,
    val habitId: Int = -1,
    val habitName: String,
    val xpEarned: Int,
    val timestamp: Long
)

@Serializable
data class AppData(
    val userName: String = "Petualang",
    val userTitle: String = "Petualang Baru",
    val profileImageId: String = "avatar_level1",
    val customProfileImagePath: String? = null,
    val customCoverImagePath: String? = null,
    val isLoggedIn: Boolean = false,
    val level: Int = 1,
    val totalXp: Int = 0,
    val streak: Int = 0,
    val totalHabitsCompleted: Int = 0,
    val coins: Int = 0,
    val totalLoginDays: Int = 1,
    val loginStreakIndex: Int = 0,
    val tickets: Int = 0,
    val lastLuckySpinDate: String = "",
    val ownedThemes: List<String> = listOf(
        "theme_default",
        "theme_ikuyo",
        "theme_hutao",
        "theme_furina",
        "theme_moonlit",
        "theme_forest_camp",
        "theme_arcade"
    ),
    val activeTheme: String = "theme_default",
    val ownedChibiSkins: List<String> = listOf("chibi_helper"),
    val activeChibiSkin: String = "chibi_helper",
    val ownedSplashVideos: List<String> = listOf("splash_default"),
    val activeSplashVideo: String = "splash_default",
    val ownedAvatarFrames: List<String> = listOf("frame_default"),
    val activeAvatarFrame: String = "frame_default",
    val ownedMissionCardSkins: List<String> = listOf("mission_card_default"),
    val activeMissionCardSkin: String = "mission_card_default",
    val ownedChecklistEffects: List<String> = listOf("checklist_effect_default"),
    val activeChecklistEffect: String = "checklist_effect_default",
    val pinnedHabitIds: List<Int> = emptyList(),
    val isHabitCompactMode: Boolean = false,
    val focusLinkedHabitId: Int? = null,
    val shownAchievementBannerIds: List<String> = emptyList(),
    val equippedBadges: Map<Int, String> = emptyMap(),
    val lastCompletionDate: String = "", // yyyy-MM-dd
    val lastResetDate: String = "", // yyyy-MM-dd
    val lastLoginDate: String = "", // yyyy-MM-dd
    val lastMonthlyResetDate: String = "", // yyyy-MM
    val isOriginalSoundtrackEnabled: Boolean = true,
    val currentSoundtrack: String = "soundtrack_1",
    val selectedSoundtracks: List<String> = listOf(
        "soundtrack_1",
        "soundtrack_2",
        "soundtrack_3",
        "soundtrack_4",
        "soundtrack_5",
        "soundtrack_6"
    ),
    val isChibiEnabled: Boolean = true,
    val isChibiVoiceEnabled: Boolean = true,
    val isReminderEnabled: Boolean = false,
    val defaultReminderTime: String = "20:00",
    val reminderSnoozeMinutes: Int = 15,
    val recommendationActivityType: String = "",
    val recommendationField: String = "",
    val recommendationStage: String = "",
    val recommendationAge: Int? = null,
    val selectedAssistantId: String = "aika_tsundere",
    val assistantChatHistories: Map<String, List<AssistantChatMessage>> = emptyMap(),
    val focusDailyTargetSessions: Int = 4,
    val focusSessionsCompletedToday: Int = 0,
    val focusLastSessionDate: String = "",
    val focusTotalSessions: Int = 0,
    val focusTotalMinutes: Int = 0,
    val focusCurrentStreakDays: Int = 0,
    val focusBestStreakDays: Int = 0,
    val habits: List<Habit> = emptyList(),
    val history: List<HabitHistoryItem> = emptyList()
) {
    fun toJson(): String {
        val jsonConfig = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        return jsonConfig.encodeToString(this)
    }

    companion object {
        fun fromJson(jsonString: String): AppData {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
            }
            return try {
                jsonConfig.decodeFromString(jsonString)
            } catch (_: Exception) {
                AppData()
            }
        }
    }
}
