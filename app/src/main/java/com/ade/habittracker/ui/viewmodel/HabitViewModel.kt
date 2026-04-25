package com.ade.habittracker.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ade.habittracker.R
import com.ade.habittracker.data.DailyReward
import com.ade.habittracker.data.DailyRewardsConfig
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.data.ShopItem
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.data.ShopType
import com.ade.habittracker.data.BundleItem
import com.ade.habittracker.data.AssistantRoster
import com.ade.habittracker.data.LuckySpinConfig
import com.ade.habittracker.data.LuckySpinResult
import com.ade.habittracker.data.LuckyRewardType
import com.ade.habittracker.data.generateAssistantReply
import com.ade.habittracker.data.predefinedHabitTemplates
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.AssistantChatMessage
import com.ade.habittracker.model.AssistantMessageSender
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitHistoryItem
import com.ade.habittracker.model.HabitScheduleType
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.model.normalizeForDate
import com.ade.habittracker.model.scheduleLabel
import com.ade.habittracker.notification.HabitReminderWorker
import com.ade.habittracker.notification.TimeSlotReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val REMINDER_TAG = "habit_reminder"
private const val SNOOZE_REMINDER_TAG = "habit_reminder_snooze"
private const val TIME_SLOT_REMINDER_TAG = "habit_time_slot_reminder"
private val TIME_PATTERN = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

data class AvatarItem(
    val id: String,
    @DrawableRes val resId: Int,
    val requiredLevel: Int
)

data class TitleItem(
    val title: String,
    val requiredLevel: Int
)

data class WeeklySummary(
    val weekLabel: String,
    val completedThisWeek: Int,
    val dueThisWeek: Int,
    val completionRate: Float,
    val mostMissedHabitName: String,
    val mostMissedCount: Int,
    val bestStreakDays: Int,
    val nextWeekTarget: Int,
    val nextWeekDueCount: Int
)

data class FocusTimerUiState(
    val selectedDurationMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val lastCompletedDurationMinutes: Int = 0,
    val lastEarnedXp: Int = 0,
    val lastEarnedCoins: Int = 0,
    val completionToken: Long = 0L
)

data class FocusSummary(
    val dailyTargetSessions: Int = 4,
    val sessionsCompletedToday: Int = 0,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0
)

private data class ScheduleConfig(
    val type: HabitScheduleType,
    val customDays: List<Int> = emptyList(),
    val targetPerWeek: Int = 3
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application.applicationContext)
    private val appContext = application.applicationContext

    private val allAvatars = listOf(
        AvatarItem("avatar_level1", R.drawable.avatar_level1, 1),
        AvatarItem("avatar_level5", R.drawable.avatar_level5, 5),
        AvatarItem("avatar_level10", R.drawable.avatar_level10, 10),
        AvatarItem("avatar_level15", R.drawable.avatar_level20, 15),
        AvatarItem("avatar_level20", R.drawable.avatar_master, 20)
    )

    private val allTitles = listOf(
        TitleItem("Petualang Baru", 1),
        TitleItem("Pemula Produktif", 5),
        TitleItem("Pejuang Disiplin", 10),
        TitleItem("Ahli Kebiasaan", 15),
        TitleItem("Sang Legenda", 20)
    )

    val appData: StateFlow<AppData?> = repository.appData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val habitHistory: StateFlow<List<HabitHistoryItem>> = appData.map { data ->
        data?.history?.sortedByDescending { it.timestamp } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heatmapData: StateFlow<Map<String, Int>> = habitHistory.map { history ->
        history
            .groupBy {
                Instant.ofEpochMilli(it.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
            }
            .mapValues { entry -> entry.value.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val achievements: StateFlow<List<Achievement>> = appData.map { data ->
        if (data == null) emptyList()
        else getAllAchievements(data.level, data.streak, data.totalXp, data.totalHabitsCompleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equippedBadgesMap: StateFlow<Map<Int, Achievement>> = combine(appData, achievements) { data, list ->
        val map = mutableMapOf<Int, Achievement>()
        data?.equippedBadges?.forEach { (slotIndex, achievementId) ->
            val found = list.find { it.id == achievementId }
            if (found != null && found.isUnlocked) {
                map[slotIndex] = found
            }
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val userName: StateFlow<String> = appData.map { data ->
        data?.userName ?: "Petualang"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang")

    val userTitle: StateFlow<String> = appData.map { data ->
        data?.userTitle ?: "Petualang Baru"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang Baru")

    val avatarListWithLockStatus: StateFlow<List<Pair<AvatarItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allAvatars.map { avatar -> avatar to (currentLevel >= avatar.requiredLevel) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val titleListWithLockStatus: StateFlow<List<Pair<TitleItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allTitles.map { title -> title to (currentLevel >= title.requiredLevel) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profileImageResId: StateFlow<Int> = appData.map { data ->
        val savedId = data?.profileImageId ?: "avatar_level1"
        allAvatars.find { it.id == savedId }?.resId ?: R.drawable.avatar_level1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), R.drawable.avatar_level1)

    val weeklySummary: StateFlow<WeeklySummary?> = appData.map { data ->
        data?.let { buildWeeklySummary(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _focusTimerState = MutableStateFlow(FocusTimerUiState())
    val focusTimerState: StateFlow<FocusTimerUiState> = _focusTimerState.asStateFlow()

    val focusSummary: StateFlow<FocusSummary> = appData.map { data ->
        buildFocusSummary(data)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusSummary())

    private val _dailyRewardState = MutableStateFlow<DailyReward?>(null)
    val dailyRewardState: StateFlow<DailyReward?> = _dailyRewardState.asStateFlow()
    private val _luckySpinResultState = MutableStateFlow<LuckySpinResult?>(null)
    val luckySpinResultState: StateFlow<LuckySpinResult?> = _luckySpinResultState.asStateFlow()
    private var focusTimerJob: Job? = null

    fun equipBadge(slotIndex: Int, achievementId: String?) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val currentBadges = currentData.equippedBadges.toMutableMap()

            if (achievementId == null) {
                currentBadges.remove(slotIndex)
            } else {
                currentBadges[slotIndex] = achievementId
            }

            repository.saveAppData(currentData.copy(equippedBadges = currentBadges))
        }
    }

    fun buyItem(item: ShopItem) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if (currentData.coins >= item.price) {
                val newCoins = currentData.coins - item.price
                val updatedData = when (item.type) {
                    ShopType.THEME -> currentData.copy(
                        coins = newCoins,
                        ownedThemes = currentData.ownedThemes + item.id
                    )
                    ShopType.AVATAR -> currentData.copy(coins = newCoins)
                    ShopType.CHIBI -> currentData.copy(
                        coins = newCoins,
                        ownedChibiSkins = currentData.ownedChibiSkins + item.id
                    )
                    ShopType.SPLASH -> currentData.copy(
                        coins = newCoins,
                        ownedSplashVideos = currentData.ownedSplashVideos + item.id
                    )
                    ShopType.AVATAR_FRAME -> currentData.copy(
                        coins = newCoins,
                        ownedAvatarFrames = currentData.ownedAvatarFrames + item.id
                    )
                    ShopType.MISSION_CARD -> currentData.copy(
                        coins = newCoins,
                        ownedMissionCardSkins = currentData.ownedMissionCardSkins + item.id
                    )
                    ShopType.CHECKLIST_EFFECT -> currentData.copy(
                        coins = newCoins,
                        ownedChecklistEffects = currentData.ownedChecklistEffects + item.id
                    )
                    ShopType.BUNDLE -> {
                        val bundle = item as? BundleItem ?: return@launch
                        applyBundle(currentData.copy(coins = newCoins), bundle, equipNow = false)
                    }
                }
                repository.saveAppData(updatedData)
            }
        }
    }

    fun equipItem(item: ShopItem) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val updatedData = when (item.type) {
                ShopType.THEME -> currentData.copy(activeTheme = item.id)
                ShopType.AVATAR -> currentData.copy(profileImageId = item.id)
                ShopType.CHIBI -> currentData.copy(activeChibiSkin = item.id)
                ShopType.SPLASH -> currentData.copy(activeSplashVideo = item.id)
                ShopType.AVATAR_FRAME -> currentData.copy(activeAvatarFrame = item.id)
                ShopType.MISSION_CARD -> currentData.copy(activeMissionCardSkin = item.id)
                ShopType.CHECKLIST_EFFECT -> currentData.copy(activeChecklistEffect = item.id)
                ShopType.BUNDLE -> {
                    val bundle = item as? BundleItem ?: return@launch
                    applyBundle(currentData, bundle, equipNow = true)
                }
            }
            repository.saveAppData(updatedData)
        }
    }

    private fun applyBundle(data: AppData, bundle: BundleItem, equipNow: Boolean): AppData {
        var updated = data
        bundle.includedItemIds.forEach { id ->
            when {
                id.startsWith("theme_") -> {
                    updated = updated.copy(
                        ownedThemes = (updated.ownedThemes + id).distinct(),
                        activeTheme = if (equipNow) id else updated.activeTheme
                    )
                }
                id.startsWith("frame_") -> {
                    updated = updated.copy(
                        ownedAvatarFrames = (updated.ownedAvatarFrames + id).distinct(),
                        activeAvatarFrame = if (equipNow) id else updated.activeAvatarFrame
                    )
                }
                id.startsWith("mission_card_") -> {
                    updated = updated.copy(
                        ownedMissionCardSkins = (updated.ownedMissionCardSkins + id).distinct(),
                        activeMissionCardSkin = if (equipNow) id else updated.activeMissionCardSkin
                    )
                }
                id.startsWith("checklist_effect_") -> {
                    updated = updated.copy(
                        ownedChecklistEffects = (updated.ownedChecklistEffects + id).distinct(),
                        activeChecklistEffect = if (equipNow) id else updated.activeChecklistEffect
                    )
                }
            }
        }
        return updated
    }

    fun setOriginalSoundtrackEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(
                currentData.copy(isOriginalSoundtrackEnabled = isEnabled)
            )
        }
    }

    fun setCurrentSoundtrack(soundtrackId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if (com.ade.habittracker.data.SoundtrackLibrary.getById(soundtrackId) == null) return@launch

            val updatedSelections = if (soundtrackId in currentData.selectedSoundtracks) {
                currentData.selectedSoundtracks
            } else {
                currentData.selectedSoundtracks + soundtrackId
            }

            repository.saveAppData(
                currentData.copy(
                    currentSoundtrack = soundtrackId,
                    selectedSoundtracks = updatedSelections
                )
            )
        }
    }

    fun toggleSoundtrackSelection(soundtrackId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if (com.ade.habittracker.data.SoundtrackLibrary.getById(soundtrackId) == null) return@launch

            val currentSelections = currentData.selectedSoundtracks
                .filter { com.ade.habittracker.data.SoundtrackLibrary.getById(it) != null }
                .ifEmpty { com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_SELECTION }
                .toMutableList()

            val updatedSelections = if (soundtrackId in currentSelections) {
                if (currentSelections.size == 1) return@launch
                currentSelections.apply { remove(soundtrackId) }
            } else {
                currentSelections.apply { add(soundtrackId) }
            }

            val nextCurrent = if (currentData.currentSoundtrack in updatedSelections) {
                currentData.currentSoundtrack
            } else {
                updatedSelections.first()
            }

            repository.saveAppData(
                currentData.copy(
                    currentSoundtrack = nextCurrent,
                    selectedSoundtracks = updatedSelections
                )
            )
        }
    }

    fun moveSoundtrack(soundtrackId: String, direction: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if (direction == 0) return@launch

            val currentSelections = currentData.selectedSoundtracks
                .filter { com.ade.habittracker.data.SoundtrackLibrary.getById(it) != null }
                .ifEmpty { com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_SELECTION }
                .toMutableList()

            val currentIndex = currentSelections.indexOf(soundtrackId)
            if (currentIndex < 0) return@launch

            val targetIndex = (currentIndex + direction).coerceIn(0, currentSelections.lastIndex)
            if (targetIndex == currentIndex) return@launch

            val item = currentSelections.removeAt(currentIndex)
            currentSelections.add(targetIndex, item)

            repository.saveAppData(
                currentData.copy(selectedSoundtracks = currentSelections)
            )
        }
    }

    fun resetSoundtrackPlaylist() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(
                currentData.copy(
                    currentSoundtrack = com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_ID,
                    selectedSoundtracks = com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_SELECTION
                )
            )
        }
    }

    fun selectAssistant(assistantId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val assistant = AssistantRoster.getById(assistantId) ?: return@launch
            val updatedHistories = ensureAssistantGreeting(
                histories = currentData.assistantChatHistories,
                assistantId = assistant.id,
                greeting = assistant.greeting
            )
            repository.saveAppData(
                currentData.copy(
                    selectedAssistantId = assistant.id,
                    assistantChatHistories = updatedHistories
                )
            )
        }
    }

    fun sendMessageToAssistant(text: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val sanitized = text.trim()
            if (sanitized.isBlank()) return@launch

            val assistant = AssistantRoster.getById(currentData.selectedAssistantId)
                ?: AssistantRoster.getById(AssistantRoster.DEFAULT_ID)
                ?: return@launch

            val historiesWithGreeting = ensureAssistantGreeting(
                histories = currentData.assistantChatHistories,
                assistantId = assistant.id,
                greeting = assistant.greeting
            )
            val currentHistory = historiesWithGreeting[assistant.id].orEmpty()

            val userMessage = AssistantChatMessage(
                id = UUID.randomUUID().toString(),
                assistantId = assistant.id,
                sender = AssistantMessageSender.USER,
                text = sanitized,
                timestamp = System.currentTimeMillis()
            )
            val assistantMessage = AssistantChatMessage(
                id = UUID.randomUUID().toString(),
                assistantId = assistant.id,
                sender = AssistantMessageSender.ASSISTANT,
                text = generateAssistantReply(assistant, sanitized, currentData),
                timestamp = System.currentTimeMillis() + 1
            )

            repository.saveAppData(
                currentData.copy(
                    selectedAssistantId = assistant.id,
                    assistantChatHistories = historiesWithGreeting + (
                        assistant.id to (currentHistory + userMessage + assistantMessage).takeLast(40)
                    )
                )
            )
        }
    }

    fun clearAssistantConversation(assistantId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val assistant = AssistantRoster.getById(assistantId) ?: return@launch
            val resetHistory = listOf(
                AssistantChatMessage(
                    id = UUID.randomUUID().toString(),
                    assistantId = assistant.id,
                    sender = AssistantMessageSender.ASSISTANT,
                    text = assistant.greeting,
                    timestamp = System.currentTimeMillis()
                )
            )
            repository.saveAppData(
                currentData.copy(
                    assistantChatHistories = currentData.assistantChatHistories + (assistant.id to resetHistory)
                )
            )
        }
    }

    fun setChibiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(isChibiEnabled = isEnabled))
        }
    }

    fun setChibiVoiceEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(isChibiVoiceEnabled = isEnabled))
        }
    }

    fun setReminderEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val updated = currentData.copy(isReminderEnabled = isEnabled)
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun updateDefaultReminderTime(time: String) {
        viewModelScope.launch {
            if (!TIME_PATTERN.matches(time)) return@launch
            val currentData = appData.value ?: return@launch
            val updated = currentData.copy(defaultReminderTime = time)
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun setHabitReminderEnabled(habitId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            val updatedHabits = currentData.habits.map { habit ->
                if (habit.id == habitId && !habit.isDailyQuest) {
                    habit.copy(
                        reminderEnabled = isEnabled,
                        reminderTime = sanitizeReminderTime(habit.reminderTime) ?: currentData.defaultReminderTime
                    )
                } else {
                    habit
                }
            }

            val updated = currentData.copy(habits = updatedHabits)
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: AppData()
            if (newName.isNotBlank()) {
                repository.saveAppData(currentData.copy(userName = newName))
            }
        }
    }

    fun updateProfileImageId(newId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(
                currentData.copy(
                    profileImageId = newId,
                    customProfileImagePath = null
                )
            )
        }
    }

    fun importCustomProfileImage(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = saveCustomProfileImage(uri)
            onResult(success)
        }
    }

    fun importCustomCoverImage(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = saveCustomCoverImage(uri)
            onResult(success)
        }
    }

    fun updateUserTitle(newTitle: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(userTitle = newTitle))
        }
    }

    fun updateRecommendationPreferences(
        activityType: String,
        field: String,
        stage: String,
        age: Int?
    ) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val updated = currentData.copy(
                recommendationActivityType = activityType,
                recommendationField = field,
                recommendationStage = stage,
                recommendationAge = age
            )
            if (updated != currentData) {
                repository.saveAppData(updated)
            }
        }
    }

    fun toggleHabitCompactMode() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(isHabitCompactMode = !currentData.isHabitCompactMode))
        }
    }

    fun togglePinnedHabit(habitId: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val currentPins = currentData.pinnedHabitIds.filter { id -> currentData.habits.any { it.id == id } }
            val updatedPins = if (habitId in currentPins) {
                currentPins - habitId
            } else {
                (currentPins + habitId).takeLast(3)
            }
            repository.saveAppData(currentData.copy(pinnedHabitIds = updatedPins))
        }
    }

    fun setFocusLinkedHabit(habitId: Int?) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val validId = habitId?.takeIf { id -> currentData.habits.any { it.id == id } }
            repository.saveAppData(currentData.copy(focusLinkedHabitId = validId))
        }
    }

    fun selectFocusDuration(minutes: Int) {
        if (minutes !in listOf(15, 25, 50, 90)) return
        if (_focusTimerState.value.isRunning) return
        _focusTimerState.value = _focusTimerState.value.copy(
            selectedDurationMinutes = minutes,
            remainingSeconds = minutes * 60
        )
    }

    fun updateFocusDailyTarget(target: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(focusDailyTargetSessions = target.coerceIn(1, 8)))
        }
    }

    fun toggleFocusTimer() {
        if (_focusTimerState.value.isRunning) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    fun startFocusTimer() {
        if (_focusTimerState.value.isRunning) return
        if (_focusTimerState.value.remainingSeconds <= 0) {
            val durationMinutes = _focusTimerState.value.selectedDurationMinutes
            _focusTimerState.value = _focusTimerState.value.copy(remainingSeconds = durationMinutes * 60)
        }

        focusTimerJob?.cancel()
        _focusTimerState.value = _focusTimerState.value.copy(isRunning = true)
        focusTimerJob = viewModelScope.launch {
            while (_focusTimerState.value.isRunning && _focusTimerState.value.remainingSeconds > 0) {
                delay(1000)
                val current = _focusTimerState.value
                if (!current.isRunning) break
                val nextRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
                _focusTimerState.value = current.copy(remainingSeconds = nextRemaining)
                if (nextRemaining == 0) {
                    completeFocusSession()
                }
            }
        }
    }

    fun pauseFocusTimer() {
        focusTimerJob?.cancel()
        _focusTimerState.value = _focusTimerState.value.copy(isRunning = false)
    }

    fun resetFocusTimer() {
        focusTimerJob?.cancel()
        val durationMinutes = _focusTimerState.value.selectedDurationMinutes
        _focusTimerState.value = _focusTimerState.value.copy(
            isRunning = false,
            remainingSeconds = durationMinutes * 60
        )
    }

    fun dismissFocusCompletion() {
        _focusTimerState.value = _focusTimerState.value.copy(completionToken = 0L)
    }

    private fun completeFocusSession() {
        focusTimerJob?.cancel()
        val durationMinutes = _focusTimerState.value.selectedDurationMinutes
        val reward = focusRewardFor(durationMinutes)
        val today = LocalDate.now()
        val todayStr = today.toString()

        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val completedToday = if (currentData.focusLastSessionDate == todayStr) {
                currentData.focusSessionsCompletedToday + 1
            } else {
                1
            }

            val newStreak = when (currentData.focusLastSessionDate) {
                todayStr -> currentData.focusCurrentStreakDays.coerceAtLeast(1)
                today.minusDays(1).toString() -> currentData.focusCurrentStreakDays + 1
                else -> 1
            }

            val updated = currentData.copy(
                totalXp = currentData.totalXp + reward.xp,
                level = calculateLevel(currentData.totalXp + reward.xp),
                coins = currentData.coins + reward.coins,
                focusSessionsCompletedToday = completedToday,
                focusLastSessionDate = todayStr,
                focusTotalSessions = currentData.focusTotalSessions + 1,
                focusTotalMinutes = currentData.focusTotalMinutes + durationMinutes,
                focusCurrentStreakDays = newStreak,
                focusBestStreakDays = maxOf(currentData.focusBestStreakDays, newStreak)
            )
            repository.saveAppData(updated)
        }

        _focusTimerState.value = _focusTimerState.value.copy(
            isRunning = false,
            remainingSeconds = durationMinutes * 60,
            lastCompletedDurationMinutes = durationMinutes,
            lastEarnedXp = reward.xp,
            lastEarnedCoins = reward.coins,
            completionToken = System.currentTimeMillis()
        )
    }

    fun markAchievementsBannerShown(achievementIds: Set<String>) {
        if (achievementIds.isEmpty()) return
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val merged = (currentData.shownAchievementBannerIds + achievementIds).distinct()
            if (merged != currentData.shownAchievementBannerIds) {
                repository.saveAppData(currentData.copy(shownAchievementBannerIds = merged))
            }
        }
    }

    fun onAuthenticated(suggestedUserName: String? = null) {
        viewModelScope.launch {
            val currentData = repository.getCurrentAppData()

            val normalizedName = suggestedUserName
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it != "Petualang" }

            val shouldApplyName = normalizedName != null &&
                (currentData.userName.isBlank() || currentData.userName == "Petualang")

            val updated = currentData.copy(
                isLoggedIn = true,
                userName = if (shouldApplyName) normalizedName else currentData.userName
            )

            if (updated != currentData) {
                repository.saveAppData(updated)
            }
        }
    }

    fun onUnauthenticated() {
        viewModelScope.launch {
            val currentData = repository.getCurrentAppData()
            repository.saveAppData(currentData.copy(isLoggedIn = true))
            rescheduleHabitReminders(currentData)
        }
    }

    fun logout() {
        onUnauthenticated()
    }

    fun resetHabitsIfNewDay() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val today = LocalDate.now()
            val todayStr = today.toString()
            val currentMonthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            var updatedData = currentData

            if (updatedData.lastMonthlyResetDate != currentMonthStr) {
                updatedData = updatedData.copy(lastMonthlyResetDate = currentMonthStr)
            }

            val isNewLoginDay = updatedData.lastLoginDate != todayStr
            if (isNewLoginDay) {
                val currentStreakIndex = calculateLoginStreakIndex(
                    lastLoginDate = updatedData.lastLoginDate,
                    currentStreakIndex = updatedData.loginStreakIndex,
                    today = today
                )

                val reward = DailyRewardsConfig.REWARDS.getOrElse(currentStreakIndex) { DailyRewardsConfig.REWARDS[0] }
                _dailyRewardState.value = reward

                updatedData = updatedData.copy(
                    lastLoginDate = todayStr,
                    loginStreakIndex = currentStreakIndex,
                    totalLoginDays = updatedData.totalLoginDays + 1
                )
            }

            val persistentHabits = updatedData.habits
                .filterNot { it.isDailyQuest }
                .map { migrateLegacySchedule(it).normalizeForDate(today) }

            val mergedHabits = if (isNewLoginDay) {
                val list = persistentHabits.toMutableList()
                createDailyQuest(updatedData.habits.maxOfOrNull { it.id } ?: 0)?.let { list.add(it) }
                list
            } else {
                val existingQuest = updatedData.habits.filter { it.isDailyQuest }
                persistentHabits + existingQuest
            }

            updatedData = updatedData.copy(
                habits = mergedHabits,
                lastResetDate = todayStr
            )

            if (updatedData != currentData) {
                repository.saveAppData(updatedData)
                rescheduleHabitReminders(updatedData)
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val reward = _dailyRewardState.value ?: return@launch
            val newCoins = currentData.coins + reward.coins
            val newLevel = calculateLevel(currentData.totalXp + 20)

            repository.saveAppData(
                currentData.copy(
                    coins = newCoins,
                    totalXp = currentData.totalXp + 20,
                    level = newLevel
                )
            )
            _dailyRewardState.value = null
        }
    }

    fun spinLuckyReward() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val today = LocalDate.now()
            val todayStr = today.toString()
            val hasFreeSpinToday = currentData.lastLuckySpinDate != todayStr
            val canUseTicket = currentData.tickets > 0
            if (!hasFreeSpinToday && !canUseTicket) return@launch

            val reward = LuckySpinConfig.spin(
                Random(
                    System.currentTimeMillis() +
                        currentData.totalXp +
                        currentData.coins +
                        currentData.tickets +
                        currentData.totalHabitsCompleted
                )
            )
            val usedTicket = !hasFreeSpinToday
            val rewardXp = if (reward.type == LuckyRewardType.XP) reward.amount else 0
            val rewardCoins = if (reward.type == LuckyRewardType.COINS) reward.amount else 0
            val rewardTickets = if (reward.type == LuckyRewardType.TICKETS) reward.amount else 0
            val nextXp = currentData.totalXp + rewardXp
            val spentTickets = if (usedTicket) 1 else 0
            val nextTickets = (currentData.tickets - spentTickets + rewardTickets).coerceAtLeast(0)

            repository.saveAppData(
                currentData.copy(
                    totalXp = nextXp,
                    level = calculateLevel(nextXp),
                    coins = currentData.coins + rewardCoins,
                    tickets = nextTickets,
                    lastLuckySpinDate = if (hasFreeSpinToday) todayStr else currentData.lastLuckySpinDate
                )
            )

            _luckySpinResultState.value = LuckySpinResult(
                reward = reward,
                usedTicket = usedTicket
            )
        }
    }

    fun dismissLuckySpinResult() {
        _luckySpinResultState.value = null
    }

    fun addHabit(name: String, schedule: String, weight: Int) {
        val inferred = inferScheduleFromText(schedule)
        addHabit(
            name = name,
            weight = weight,
            scheduleType = inferred.type,
            customDays = inferred.customDays,
            targetPerWeek = inferred.targetPerWeek,
            reminderEnabled = true,
            reminderTime = null
        )
    }

    fun addHabit(
        name: String,
        weight: Int,
        scheduleType: HabitScheduleType,
        customDays: List<Int>,
        targetPerWeek: Int,
        reminderEnabled: Boolean,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            val normalizedType = normalizeScheduleType(scheduleType, customDays)
            val sanitizedDays = customDays.distinct().sorted().filter { it in 1..7 }
            val sanitizedTime = sanitizeReminderTime(reminderTime) ?: currentData.defaultReminderTime

            val newHabit = Habit(
                id = (currentData.habits.maxOfOrNull { it.id } ?: 0) + 1,
                name = name.trim(),
                schedule = buildScheduleLabel(normalizedType, sanitizedDays, targetPerWeek),
                weight = weight,
                isDailyQuest = false,
                scheduleType = normalizedType,
                customDays = sanitizedDays,
                targetPerWeek = targetPerWeek.coerceIn(1, 7),
                completionDates = emptyList(),
                reminderEnabled = reminderEnabled,
                reminderTime = sanitizedTime
            ).normalizeForDate(LocalDate.now())

            val updated = currentData.copy(habits = currentData.habits + newHabit)
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun updateHabit(id: Int, name: String, schedule: String, weight: Int) {
        val inferred = inferScheduleFromText(schedule)
        val current = appData.value?.habits?.find { it.id == id }
        updateHabit(
            id = id,
            name = name,
            weight = weight,
            scheduleType = inferred.type,
            customDays = inferred.customDays,
            targetPerWeek = inferred.targetPerWeek,
            reminderEnabled = current?.reminderEnabled ?: true,
            reminderTime = current?.reminderTime
        )
    }

    fun updateHabit(
        id: Int,
        name: String,
        weight: Int,
        scheduleType: HabitScheduleType,
        customDays: List<Int>,
        targetPerWeek: Int,
        reminderEnabled: Boolean,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val today = LocalDate.now()

            val normalizedType = normalizeScheduleType(scheduleType, customDays)
            val sanitizedDays = customDays.distinct().sorted().filter { it in 1..7 }
            val sanitizedTime = sanitizeReminderTime(reminderTime) ?: currentData.defaultReminderTime

            val updatedHabits = currentData.habits.map { habit ->
                if (habit.id == id) {
                    habit.copy(
                        name = name.trim(),
                        weight = weight,
                        scheduleType = normalizedType,
                        customDays = sanitizedDays,
                        targetPerWeek = targetPerWeek.coerceIn(1, 7),
                        reminderEnabled = reminderEnabled,
                        reminderTime = sanitizedTime,
                        schedule = buildScheduleLabel(normalizedType, sanitizedDays, targetPerWeek)
                    ).normalizeForDate(today)
                } else {
                    habit
                }
            }

            val updated = currentData.copy(habits = updatedHabits)
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun deleteHabit(id: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val habitToDelete = currentData.habits.find { it.id == id }
            var newTotalXp = currentData.totalXp
            var newTotalHabitsCompleted = currentData.totalHabitsCompleted

            if (habitToDelete?.isCompleted == true) {
                newTotalXp -= habitToDelete.weight
                newTotalHabitsCompleted -= 1
            }
            if (newTotalXp < 0) newTotalXp = 0
            if (newTotalHabitsCompleted < 0) newTotalHabitsCompleted = 0

            val updatedHabits = currentData.habits.filterNot { it.id == id }
            val newLevel = calculateLevel(newTotalXp)
            val updated = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel,
                totalHabitsCompleted = newTotalHabitsCompleted
            )
            repository.saveAppData(updated)
            rescheduleHabitReminders(updated)
        }
    }

    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val today = LocalDate.now()
            val targetHabit = currentData.habits.find { it.id == habitId } ?: return@launch
            val normalizedTarget = migrateLegacySchedule(targetHabit).normalizeForDate(today)

            if (!normalizedTarget.isDueOn(today)) return@launch
            if (!isCompleted) return@launch
            if (normalizedTarget.isCompleted) return@launch

            var newTotalXp = currentData.totalXp + normalizedTarget.weight
            var newTotalHabitsCompleted = currentData.totalHabitsCompleted + 1
            val currentHistory = currentData.history.toMutableList()

            val updatedHabits = currentData.habits.map { habit ->
                if (habit.id == habitId) {
                    val updatedDates = (habit.completionDates + today.toString()).distinct().sorted()
                    val updatedHabit = habit.copy(
                        completionDates = updatedDates,
                        isCompleted = true,
                        schedule = if (habit.isDailyQuest) habit.schedule else habit.scheduleLabel()
                    )

                    if (habit.isDailyQuest) updatedHabit else updatedHabit.normalizeForDate(today)
                } else {
                    habit
                }
            }

            currentHistory.add(
                HabitHistoryItem(
                    id = UUID.randomUUID().toString(),
                    habitId = normalizedTarget.id,
                    habitName = normalizedTarget.name,
                    xpEarned = normalizedTarget.weight,
                    timestamp = System.currentTimeMillis()
                )
            )

            if (newTotalXp < 0) newTotalXp = 0
            if (newTotalHabitsCompleted < 0) newTotalHabitsCompleted = 0

            val newLevel = calculateLevel(newTotalXp)
            var finalData = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel,
                totalHabitsCompleted = newTotalHabitsCompleted,
                history = currentHistory
            )

            val dueHabits = updatedHabits.filter { !it.isDailyQuest && it.isDueOn(today) }
            if (dueHabits.isNotEmpty() && dueHabits.all { it.isCompleted }) {
                finalData = checkStreaks(finalData, today)
            }

            repository.saveAppData(finalData)
        }
    }

    private fun checkStreaks(currentData: AppData, today: LocalDate): AppData {
        val todayStr = today.toString()
        if (todayStr == currentData.lastCompletionDate) return currentData

        val yesterdayStr = today.minusDays(1).toString()
        val newStreak = if (currentData.lastCompletionDate == yesterdayStr) currentData.streak + 1 else 1
        return currentData.copy(streak = newStreak, lastCompletionDate = todayStr)
    }

    fun getXpProgress(): Pair<Int, Int> {
        val totalXp = appData.value?.totalXp ?: 0
        val currentLevelXp = (appData.value?.level?.minus(1) ?: 0) * 100
        return Pair(totalXp - currentLevelXp, 100)
    }

    fun getFocusReward(minutes: Int): Pair<Int, Int> {
        val reward = focusRewardFor(minutes)
        return reward.xp to reward.coins
    }

    fun scheduleDailyReminder(context: Context) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val updated = if (currentData.isReminderEnabled) {
                currentData
            } else {
                val changed = currentData.copy(isReminderEnabled = true)
                repository.saveAppData(changed)
                changed
            }
            rescheduleHabitReminders(updated)
        }
    }

    fun refreshReminderSchedulesIfEnabled() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if (currentData.isReminderEnabled) {
                rescheduleHabitReminders(currentData)
            }
        }
    }

    fun getBackupJson(): String {
        return appData.value?.toJson() ?: ""
    }

    fun restoreFromBackup(jsonString: String): Boolean {
        return try {
            val restoredData = AppData.fromJson(jsonString)
            if (restoredData.level >= 1) {
                viewModelScope.launch {
                    val migrated = restoredData.copy(
                        habits = restoredData.habits.map { migrateLegacySchedule(it).normalizeForDate(LocalDate.now()) }
                    )
                    repository.saveAppData(migrated)
                    rescheduleHabitReminders(migrated)
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun calculateLevel(totalXp: Int): Int = (totalXp / 100) + 1

    private fun buildFocusSummary(data: AppData?): FocusSummary {
        if (data == null) return FocusSummary()
        val today = LocalDate.now()
        val todayStr = today.toString()
        val currentStreak = when (data.focusLastSessionDate) {
            todayStr, today.minusDays(1).toString() -> data.focusCurrentStreakDays
            else -> 0
        }
        val sessionsToday = if (data.focusLastSessionDate == todayStr) data.focusSessionsCompletedToday else 0
        return FocusSummary(
            dailyTargetSessions = data.focusDailyTargetSessions.coerceIn(1, 8),
            sessionsCompletedToday = sessionsToday,
            totalSessions = data.focusTotalSessions,
            totalMinutes = data.focusTotalMinutes,
            currentStreakDays = currentStreak,
            bestStreakDays = data.focusBestStreakDays
        )
    }

    private data class FocusReward(val xp: Int, val coins: Int)

    private fun focusRewardFor(minutes: Int): FocusReward = when {
        minutes >= 90 -> FocusReward(xp = 50, coins = 12)
        minutes >= 50 -> FocusReward(xp = 30, coins = 8)
        minutes >= 25 -> FocusReward(xp = 20, coins = 5)
        else -> FocusReward(xp = 10, coins = 3)
    }

    private suspend fun saveCustomProfileImage(uri: Uri): Boolean {
        return saveCustomImage(uri, "profile_images", "custom_profile.jpg") { currentData, path ->
            currentData.copy(customProfileImagePath = path)
        }
    }

    private suspend fun saveCustomCoverImage(uri: Uri): Boolean {
        return saveCustomImage(uri, "cover_images", "custom_cover.jpg") { currentData, path ->
            currentData.copy(customCoverImagePath = path)
        }
    }

    private fun ensureAssistantGreeting(
        histories: Map<String, List<AssistantChatMessage>>,
        assistantId: String,
        greeting: String
    ): Map<String, List<AssistantChatMessage>> {
        val existing = histories[assistantId].orEmpty()
        if (existing.isNotEmpty()) return histories
        return histories + (
            assistantId to listOf(
                AssistantChatMessage(
                    id = UUID.randomUUID().toString(),
                    assistantId = assistantId,
                    sender = AssistantMessageSender.ASSISTANT,
                    text = greeting,
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }

    private suspend fun saveCustomImage(
        uri: Uri,
        folderName: String,
        fileName: String,
        updater: (AppData, String) -> AppData
    ): Boolean {
        return runCatching {
            val sourceBitmap = appContext.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return false

            val maxSide = 1024
            val width = sourceBitmap.width.coerceAtLeast(1)
            val height = sourceBitmap.height.coerceAtLeast(1)
            val scale = minOf(maxSide.toFloat() / width.toFloat(), maxSide.toFloat() / height.toFloat(), 1f)
            val targetWidth = (width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (height * scale).toInt().coerceAtLeast(1)

            val finalBitmap = if (targetWidth != width || targetHeight != height) {
                Bitmap.createScaledBitmap(sourceBitmap, targetWidth, targetHeight, true)
            } else {
                sourceBitmap
            }

            val dir = File(appContext.filesDir, folderName).apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { output ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            }

            if (finalBitmap !== sourceBitmap) {
                finalBitmap.recycle()
            }
            sourceBitmap.recycle()

            val currentData = appData.value ?: repository.getCurrentAppData()
            repository.saveAppData(updater(currentData, file.absolutePath))
            true
        }.getOrElse { false }
    }

    private fun calculateLoginStreakIndex(lastLoginDate: String, currentStreakIndex: Int, today: LocalDate): Int {
        val lastDate = parseLocalDate(lastLoginDate) ?: return 0
        val diffDays = ChronoUnit.DAYS.between(lastDate, today)
        val next = if (diffDays == 1L) currentStreakIndex + 1 else 0
        return if (next >= 7) 0 else next
    }

    private fun createDailyQuest(maxId: Int): Habit? {
        val sideQuestTemplate = predefinedHabitTemplates
            .find { it.first == "Tantangan (Side Quest)" }
            ?.second
            ?.randomOrNull()
            ?: return null

        return Habit(
            id = maxId + 1,
            name = "⭐ ${sideQuestTemplate.name}",
            schedule = "Misi Spesial Hari Ini",
            weight = sideQuestTemplate.weight + 50,
            isCompleted = false,
            scheduleType = HabitScheduleType.DAILY,
            reminderEnabled = false,
            reminderTime = null,
            isDailyQuest = true
        )
    }

    private fun migrateLegacySchedule(habit: Habit): Habit {
        if (habit.isDailyQuest) return habit

        val alreadyConfigured = habit.scheduleType != HabitScheduleType.DAILY ||
            habit.customDays.isNotEmpty() ||
            habit.targetPerWeek != 3
        if (alreadyConfigured) {
            return habit.copy(schedule = habit.scheduleLabel())
        }

        val inferred = inferScheduleFromText(habit.schedule)
        return habit.copy(
            scheduleType = inferred.type,
            customDays = inferred.customDays,
            targetPerWeek = inferred.targetPerWeek,
            schedule = buildScheduleLabel(inferred.type, inferred.customDays, inferred.targetPerWeek)
        )
    }

    private fun inferScheduleFromText(scheduleText: String): ScheduleConfig {
        val text = scheduleText.lowercase().trim()

        val targetMatch = Regex("(\\d+)\\s*x").find(text)
        if (targetMatch != null && text.contains("minggu")) {
            val target = targetMatch.groupValues[1].toIntOrNull()?.coerceIn(1, 7) ?: 3
            return ScheduleConfig(HabitScheduleType.TIMES_PER_WEEK, targetPerWeek = target)
        }

        if ((text.contains("senin") && text.contains("jumat")) || text.contains("weekday")) {
            return ScheduleConfig(HabitScheduleType.WEEKDAYS)
        }

        val customDays = listOf(
            "senin" to 1,
            "selasa" to 2,
            "rabu" to 3,
            "kamis" to 4,
            "jumat" to 5,
            "sabtu" to 6,
            "minggu" to 7
        ).filter { (name, _) -> text.contains(name) }
            .map { it.second }
            .distinct()
            .sorted()

        if (customDays.isNotEmpty()) {
            return ScheduleConfig(HabitScheduleType.CUSTOM_DAYS, customDays = customDays)
        }

        return ScheduleConfig(HabitScheduleType.DAILY)
    }

    private fun normalizeScheduleType(scheduleType: HabitScheduleType, customDays: List<Int>): HabitScheduleType {
        return if (scheduleType == HabitScheduleType.CUSTOM_DAYS && customDays.isEmpty()) {
            HabitScheduleType.DAILY
        } else {
            scheduleType
        }
    }

    private fun buildScheduleLabel(
        scheduleType: HabitScheduleType,
        customDays: List<Int>,
        targetPerWeek: Int
    ): String {
        return when (normalizeScheduleType(scheduleType, customDays)) {
            HabitScheduleType.DAILY -> "Setiap Hari"
            HabitScheduleType.WEEKDAYS -> "Senin-Jumat"
            HabitScheduleType.TIMES_PER_WEEK -> "${targetPerWeek.coerceIn(1, 7)}x / Minggu"
            HabitScheduleType.CUSTOM_DAYS -> {
                customDays
                    .distinct()
                    .sorted()
                    .joinToString(", ") { dayToLongName(it) }
                    .ifBlank { "Setiap Hari" }
            }
        }
    }

    private fun dayToLongName(day: Int): String {
        return when (day) {
            1 -> "Senin"
            2 -> "Selasa"
            3 -> "Rabu"
            4 -> "Kamis"
            5 -> "Jumat"
            6 -> "Sabtu"
            7 -> "Minggu"
            else -> "-"
        }
    }

    private fun sanitizeReminderTime(time: String?): String? {
        val trimmed = time?.trim() ?: return null
        return if (TIME_PATTERN.matches(trimmed)) trimmed else null
    }

    private fun rescheduleHabitReminders(data: AppData) {
        val workManager = WorkManager.getInstance(appContext)
        workManager.cancelAllWorkByTag(REMINDER_TAG)
        workManager.cancelAllWorkByTag(SNOOZE_REMINDER_TAG)
        workManager.cancelAllWorkByTag(TIME_SLOT_REMINDER_TAG)

        if (!data.isReminderEnabled) return

        val timeSlots = listOf("06:00", "09:00", "12:00", "15:00", "18:00", "21:00")

        timeSlots.forEachIndexed { index, time ->
            val label = "Pengingat $time"
            val notificationId = 8200 + index
            val initialDelayMillis = calculateInitialDelayMillis(time)
            val request = PeriodicWorkRequestBuilder<TimeSlotReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        TimeSlotReminderWorker.KEY_SLOT_LABEL to label,
                        TimeSlotReminderWorker.KEY_NOTIFICATION_ID to notificationId
                    )
                )
                .addTag(REMINDER_TAG)
                .addTag(TIME_SLOT_REMINDER_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "time_slot_reminder_${time.replace(':', '_')}",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    private fun calculateInitialDelayMillis(time: String): Long {
        val parsedTime = try {
            LocalTime.parse(time)
        } catch (_: DateTimeParseException) {
            LocalTime.of(20, 0)
        }

        val now = LocalDateTime.now()
        var triggerAt = now.toLocalDate().atTime(parsedTime)
        if (!triggerAt.isAfter(now)) {
            triggerAt = triggerAt.plusDays(1)
        }

        return Duration.between(now, triggerAt).toMillis().coerceAtLeast(TimeUnit.MINUTES.toMillis(1))
    }

    private fun parseLocalDate(value: String): LocalDate? {
        return try {
            if (value.isBlank()) null else LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun buildWeeklySummary(data: AppData): WeeklySummary {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val nextWeekStart = weekStart.plusWeeks(1)
        val nextWeekEnd = nextWeekStart.plusDays(6)
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("id-ID"))

        val trackedHabits = data.habits
            .filterNot { it.isDailyQuest }
            .map { migrateLegacySchedule(it).normalizeForDate(today) }

        var dueThisWeek = 0
        var completedThisWeek = 0
        val missedCountByHabit = mutableMapOf<String, Int>()

        trackedHabits.forEach { habit ->
            var missedCount = 0
            var cursor = weekStart
            while (!cursor.isAfter(today)) {
                if (habit.isDueOn(cursor)) {
                    dueThisWeek += 1
                    val completedOnDay = habit.completionDates.any { it == cursor.toString() }
                    if (completedOnDay) {
                        completedThisWeek += 1
                    } else {
                        missedCount += 1
                    }
                }
                cursor = cursor.plusDays(1)
            }
            missedCountByHabit[habit.name] = missedCount
        }

        val mostMissed = missedCountByHabit.maxByOrNull { it.value }
        val mostMissedName = if ((mostMissed?.value ?: 0) > 0) {
            mostMissed?.key ?: "-"
        } else {
            "Belum ada habit bolong"
        }
        val mostMissedCount = mostMissed?.value ?: 0

        val bestStreakDays = calculateBestCompletionStreak(data.history)
        val nextWeekDueCount = calculateDueCountInRange(trackedHabits, nextWeekStart, nextWeekEnd)
        val nextWeekTarget = when {
            nextWeekDueCount == 0 -> 0
            completedThisWeek == 0 -> minOf(3, nextWeekDueCount)
            else -> (completedThisWeek + 1).coerceAtMost(nextWeekDueCount).coerceAtLeast(1)
        }
        val completionRate = if (dueThisWeek > 0) {
            completedThisWeek.toFloat() / dueThisWeek.toFloat()
        } else {
            0f
        }

        return WeeklySummary(
            weekLabel = "${weekStart.format(dateFormatter)} - ${weekEnd.format(dateFormatter)}",
            completedThisWeek = completedThisWeek,
            dueThisWeek = dueThisWeek,
            completionRate = completionRate,
            mostMissedHabitName = mostMissedName,
            mostMissedCount = mostMissedCount,
            bestStreakDays = bestStreakDays,
            nextWeekTarget = nextWeekTarget,
            nextWeekDueCount = nextWeekDueCount
        )
    }

    private fun calculateBestCompletionStreak(history: List<HabitHistoryItem>): Int {
        if (history.isEmpty()) return 0

        val dates = history
            .asSequence()
            .map {
                Instant.ofEpochMilli(it.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .distinct()
            .sorted()
            .toList()

        if (dates.isEmpty()) return 0

        var best = 1
        var current = 1

        for (index in 1 until dates.size) {
            val previous = dates[index - 1]
            val currentDate = dates[index]
            if (ChronoUnit.DAYS.between(previous, currentDate) == 1L) {
                current += 1
                if (current > best) best = current
            } else {
                current = 1
            }
        }

        return best
    }

    private fun calculateDueCountInRange(habits: List<Habit>, startDate: LocalDate, endDate: LocalDate): Int {
        var count = 0
        habits.forEach { habit ->
            var cursor = startDate
            while (!cursor.isAfter(endDate)) {
                if (habit.isDueOn(cursor)) {
                    count += 1
                }
                cursor = cursor.plusDays(1)
            }
        }
        return count
    }

    private fun getAllAchievements(
        level: Int,
        streak: Int,
        totalXp: Int,
        totalHabitsCompleted: Int
    ): List<Achievement> {
        val allAchievements = mutableListOf<Achievement>()
        allAchievements.add(Achievement("level_5", "Kekuatan Baru", "Tunjukkan potensimu dan capai Level 5.", R.drawable.level5, level >= 5, minOf(level, 5), 5))
        allAchievements.add(Achievement("level_10", "Pejuang Tangguh", "Disiplin adalah senjatamu. Capai Level 10.", R.drawable.level10, level >= 10, minOf(level, 10), 10))
        allAchievements.add(Achievement("level_20", "Legenda Hidup", "Kamu telah menguasai dirimu. Capai Level 20.", R.drawable.level20, level >= 20, minOf(level, 20), 20))
        allAchievements.add(Achievement("streak_3", "Api Mulai Menyala", "Jaga apinya tetap menyala selama 3 hari beruntun.", R.drawable.streak3, streak >= 3, minOf(streak, 3), 3))
        allAchievements.add(Achievement("streak_7", "Kekuatan Kebiasaan", "Kamu tak terhentikan! Selesaikan 7 hari streak.", R.drawable.streak7, streak >= 7, minOf(streak, 7), 7))
        allAchievements.add(Achievement("streak_30", "Penguasa Waktu", "Satu bulan penuh dedikasi. Capai 30 hari streak.", R.drawable.streak30, streak >= 30, minOf(streak, 30), 30))
        allAchievements.add(Achievement("xp_1000", "Pemburu Poin", "Setiap poin berharga. Kumpulkan 1000 total XP.", R.drawable.xp1000, totalXp >= 1000, minOf(totalXp, 1000), 1000))
        allAchievements.add(Achievement("xp_5000", "Veteran Elit", "Hanya untuk yang terkuat. Kumpulkan 5000 total XP.", R.drawable.xp5000, totalXp >= 5000, minOf(totalXp, 5000), 5000))
        allAchievements.add(Achievement("habits_1", "Awal Perjalanan", "Perjalanan seribu mil dimulai dengan satu misi.", R.drawable.misi1, totalHabitsCompleted >= 1, minOf(totalHabitsCompleted, 1), 1))
        allAchievements.add(Achievement("habits_50", "Ksatria Produktif", "Terus maju! Selesaikan 50 total misi.", R.drawable.misi50, totalHabitsCompleted >= 50, minOf(totalHabitsCompleted, 50), 50))
        allAchievements.add(Achievement("habits_200", "Sang Penakluk Misi", "Tidak ada misi yang terlalu sulit. Selesaikan 200 misi.", R.drawable.misi200, totalHabitsCompleted >= 200, minOf(totalHabitsCompleted, 200), 200))
        return allAchievements.sortedWith(compareBy({ it.isUnlocked }, { !(it.progress > 0 && !it.isUnlocked) }))
    }
}
