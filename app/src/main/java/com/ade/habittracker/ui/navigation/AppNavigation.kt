package com.ade.habittracker.ui.navigation

import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ade.habittracker.data.AssistantRoster
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.data.predefinedHabitTemplates
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitTemplate
import com.ade.habittracker.ui.components.DailyLoginDialog
import com.ade.habittracker.ui.components.DeleteConfirmationDialog
import com.ade.habittracker.ui.components.EditNameDialog
import com.ade.habittracker.ui.components.ForestCampBackground
import com.ade.habittracker.ui.components.FurinaTideBackground
import com.ade.habittracker.ui.components.HuTaoEmberBackground
import com.ade.habittracker.ui.components.IkuyoScarletBackground
import com.ade.habittracker.ui.components.MoonlitQuestBackground
import com.ade.habittracker.ui.components.RamadhanCalmBackground
import com.ade.habittracker.ui.components.RamadhanFestiveBackground
import com.ade.habittracker.ui.components.StarlightArcadeBackground
import com.ade.habittracker.ui.components.TopEventBanner
import com.ade.habittracker.ui.components.sheets.AddOptionsSheet
import com.ade.habittracker.ui.components.sheets.AvatarPickerSheet
import com.ade.habittracker.ui.components.sheets.ManualAddHabitSheet
import com.ade.habittracker.ui.components.sheets.RecommendationHabitSheet
import com.ade.habittracker.ui.components.sheets.TemplateHabitSheet
import com.ade.habittracker.ui.components.sheets.TitlePickerSheet
import com.ade.habittracker.ui.screens.AchievementsScreen
import com.ade.habittracker.ui.screens.AssistantInboxScreen
import com.ade.habittracker.ui.screens.AssistantChatScreen
import com.ade.habittracker.ui.screens.FocusScreen
import com.ade.habittracker.ui.screens.HabitsScreen
import com.ade.habittracker.ui.screens.ProfileScreen
import com.ade.habittracker.ui.screens.ShopScreen
import com.ade.habittracker.ui.screens.StatsScreen
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.DarkBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.ade.habittracker.widget.HabitHomeWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class TopBannerEvent(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val iconTint: Color,
    val highlightText: String? = null
)

private const val PAGE_TRANSITION_DURATION_MS = 420
private const val UI_TRANSITION_DURATION_MS = 280
private const val IDLE_TRANSITION_DURATION_MS = 420

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onScheduleReminderClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val dailyReward by viewModel.dailyRewardState.collectAsStateWithLifecycle()
    val luckySpinResult by viewModel.luckySpinResultState.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val equippedBadges by viewModel.equippedBadgesMap.collectAsStateWithLifecycle()
    val profileImageResId by viewModel.profileImageResId.collectAsStateWithLifecycle()
    val focusState by viewModel.focusTimerState.collectAsStateWithLifecycle()
    val focusSummary by viewModel.focusSummary.collectAsStateWithLifecycle()
    val activeChibiRes = ShopRepository.getChibiResById(appData?.activeChibiSkin ?: "chibi_helper")
    val activeThemeId = appData?.activeTheme ?: "theme_default"
    val isIkuyoTheme = activeThemeId == "theme_ikuyo"
    val isHuTaoTheme = activeThemeId == "theme_hutao"
    val isFurinaTheme = activeThemeId == "theme_furina"
    val isMoonlitTheme = activeThemeId == "theme_moonlit"
    val isForestCampTheme = activeThemeId == "theme_forest_camp"
    val isArcadeTheme = activeThemeId == "theme_arcade"
    val isRamadhanCalmTheme = activeThemeId == "theme_ramadhan"
    val isRamadhanFestiveTheme = activeThemeId == "theme_ramadhan_festive"
    val selectedAssistant = remember(appData?.selectedAssistantId) {
        AssistantRoster.getById(appData?.selectedAssistantId ?: AssistantRoster.DEFAULT_ID)
            ?: AssistantRoster.ITEMS.first()
    }

    val pagerState = rememberPagerState(pageCount = { 8 })

    // --- STATE MANAGEMENT ---
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showRecommendationSheet by remember { mutableStateOf(false) }

    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showTitlePickerSheet by remember { mutableStateOf(false) }

    // --- IDLE DETECTION ---
    var isUserIdle by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val topBannerQueue = remember { mutableStateListOf<TopBannerEvent>() }
    var activeTopBanner by remember { mutableStateOf<TopBannerEvent?>(null) }
    var sessionShownAchievementIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val pageScrollAnimationSpec = remember {
        tween<Float>(
            durationMillis = PAGE_TRANSITION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    }

    fun enqueueTopBanner(event: TopBannerEvent) {
        if (topBannerQueue.size >= 6) {
            topBannerQueue.removeAt(0)
        }
        topBannerQueue.add(event)
    }

    LaunchedEffect(activeTopBanner, topBannerQueue.size) {
        if (activeTopBanner == null && topBannerQueue.isNotEmpty()) {
            activeTopBanner = topBannerQueue.removeAt(0)
        }
    }

    LaunchedEffect(achievements, appData?.shownAchievementBannerIds) {
        val alreadyShownIds = appData?.shownAchievementBannerIds?.toSet().orEmpty()
        val newlyUnlocked = achievements.filter {
            it.isUnlocked &&
                it.id !in alreadyShownIds &&
                it.id !in sessionShownAchievementIds
        }
        if (newlyUnlocked.isEmpty()) return@LaunchedEffect

        newlyUnlocked.forEach { achievement ->
            enqueueTopBanner(
                TopBannerEvent(
                    title = "Achievement Terbuka!",
                    message = achievement.title,
                    icon = Icons.Default.EmojiEvents,
                    iconTint = AccentYellow,
                    highlightText = "Baru"
                )
            )
        }

        val newIds = newlyUnlocked.map { it.id }.toSet()
        sessionShownAchievementIds = sessionShownAchievementIds + newIds
        viewModel.markAchievementsBannerShown(newIds)
    }

    LaunchedEffect(focusState.completionToken) {
        if (focusState.completionToken <= 0L) return@LaunchedEffect
        enqueueTopBanner(
            TopBannerEvent(
                title = "Fokus Selesai",
                message = "${focusState.lastCompletedDurationMinutes} menit selesai",
                icon = Icons.Default.Timer,
                iconTint = AccentYellow,
                highlightText = "+${focusState.lastEarnedXp} XP"
            )
        )
    }

    LaunchedEffect(lastInteractionTime) {
        isUserIdle = false
        delay(3000)
        isUserIdle = true
    }

    LaunchedEffect(appData) {
        HabitHomeWidgetProvider.refreshAll(context)
    }

    // --- DATA EXPORT / IMPORT LAUNCHERS ---

    // 1. Export (Backup) Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val jsonString = viewModel.getBackupJson()
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Backup berhasil disimpan!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal menyimpan backup: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    // 2. Import (Restore) Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        val jsonString = inputStream.bufferedReader().readText()
                        val success = viewModel.restoreFromBackup(jsonString)
                        if (success) {
                            Toast.makeText(context, "Data berhasil dipulihkan!", Toast.LENGTH_SHORT).show()
                            // Refresh UI / Restart Activity logic if needed, but StateFlow should handle it
                        } else {
                            Toast.makeText(context, "File backup tidak valid atau rusak.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membaca file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.importCustomProfileImage(uri) { success ->
            if (success) {
                Toast.makeText(context, "Foto profil berhasil diperbarui.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memuat foto dari galeri.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val coverPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.importCustomCoverImage(uri) { success ->
            if (success) {
                Toast.makeText(context, "Sampul profil berhasil diperbarui.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memuat sampul dari galeri.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- CONTAINER UTAMA ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    lastInteractionTime = System.currentTimeMillis()
                    waitForUpOrCancellation()
                }
            }
    ) {
        if (isMoonlitTheme) {
            MoonlitQuestBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isForestCampTheme) {
            ForestCampBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isArcadeTheme) {
            StarlightArcadeBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isFurinaTheme) {
            FurinaTideBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isHuTaoTheme) {
            HuTaoEmberBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isIkuyoTheme) {
            IkuyoScarletBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.96f)
            )
        }
        if (isRamadhanCalmTheme) {
            RamadhanCalmBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.95f)
            )
        }
        if (isRamadhanFestiveTheme) {
            RamadhanFestiveBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.95f)
            )
        }

        // 1. HORIZONTAL PAGER (KONTEN UTAMA)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .graphicsLayer {
                        val clampedOffset = pageOffset.coerceIn(0f, 1f)
                        alpha = 0.78f + (1f - clampedOffset) * 0.22f
                        scaleX = 0.94f + (1f - clampedOffset) * 0.06f
                        scaleY = 0.96f + (1f - clampedOffset) * 0.04f
                        translationX = clampedOffset * 36f
                    }
            ) {
                when (page) {
                    0 -> {
                        HabitsScreen(
                            habits = appData?.habits ?: emptyList(),
                            userName = appData?.userName ?: "Petualang",
                            chibiRes = activeChibiRes,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
                            activeMissionCardSkinId = appData?.activeMissionCardSkin ?: "mission_card_default",
                            activeChecklistEffectId = appData?.activeChecklistEffect ?: "checklist_effect_default",
                            onHabitCheckedChanged = { habit, isChecked ->
                                if (isChecked && !habit.isCompleted) {
                                    enqueueTopBanner(
                                        TopBannerEvent(
                                            title = "Misi Selesai",
                                            message = habit.name,
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = PrimaryColor,
                                            highlightText = "+${habit.weight} XP"
                                        )
                                    )
                                }
                                viewModel.toggleHabitCompleted(habit.id, isChecked)
                            },
                            onReminderToggle = { habit, enabled ->
                                viewModel.setHabitReminderEnabled(habit.id, enabled)
                            },
                            onEditClick = { habit ->
                                habitToEdit = habit
                                showManualAddSheet = true
                            },
                            onDeleteClick = { habit -> habitToDelete = habit }
                        )
                    }
                    1 -> {
                        val (xpProgress, max) = viewModel.getXpProgress()
                        StatsScreen(
                            level = appData?.level ?: 1,
                            streak = appData?.streak ?: 0,
                            totalXp = appData?.totalXp ?: 0,
                            xpProgress = xpProgress,
                            maxXp = max,
                            totalLoginDays = appData?.totalLoginDays ?: 1,
                            userName = appData?.userName ?: "Petualang",
                            chibiRes = activeChibiRes,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
                            viewModel = viewModel
                        )
                    }
                    2 -> {
                        AchievementsScreen(
                            achievements = achievements,
                            userName = appData?.userName ?: "Petualang",
                            chibiRes = activeChibiRes,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true
                        )
                    }
                    3 -> {
                        val (xpProgress, max) = viewModel.getXpProgress()
                        ProfileScreen(
                            userName = appData?.userName ?: "Petualang",
                            userTitle = appData?.userTitle ?: "Petualang Baru",
                            level = appData?.level ?: 1,
                            totalXp = appData?.totalXp ?: 0,
                            streak = appData?.streak ?: 0,
                            totalLoginDays = appData?.totalLoginDays ?: 1,
                            xpProgress = xpProgress,
                            maxXp = max,
                            profileImageResId = profileImageResId,
                            customProfileImagePath = appData?.customProfileImagePath,
                            customCoverImagePath = appData?.customCoverImagePath,
                            activeAvatarFrameId = appData?.activeAvatarFrame ?: "frame_default",
                            achievements = achievements,
                            equippedBadges = equippedBadges,
                            activeAssistantName = selectedAssistant.name,
                            activeAssistantRole = selectedAssistant.archetype,
                            activeAssistantAssetName = selectedAssistant.assetName,
                            tickets = appData?.tickets ?: 0,
                            canUseLuckySpin = appData?.let {
                                it.lastLuckySpinDate != java.time.LocalDate.now().toString() || it.tickets > 0
                            } ?: true,
                            luckySpinResult = luckySpinResult,
                            currentSoundtrackId = appData?.currentSoundtrack ?: com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_ID,
                            availableSoundtracks = com.ade.habittracker.data.SoundtrackLibrary.ITEMS,
                            selectedSoundtrackIds = appData?.selectedSoundtracks
                                ?: com.ade.habittracker.data.SoundtrackLibrary.DEFAULT_SELECTION,
                            isOriginalSoundtrackEnabled = appData?.isOriginalSoundtrackEnabled ?: true,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
                            isReminderEnabled = appData?.isReminderEnabled ?: false,
                            onNameClick = { showNameEditDialog = true },
                            onCoverClick = {
                                coverPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onAvatarClick = { showAvatarPickerSheet = true },
                            onTitleClick = { showTitlePickerSheet = true },
                            onOriginalSoundtrackToggle = { viewModel.setOriginalSoundtrackEnabled(it) },
                            onToggleSoundtrackSelection = { soundtrackId ->
                                viewModel.toggleSoundtrackSelection(soundtrackId)
                            },
                            onSetCurrentSoundtrack = { soundtrackId ->
                                viewModel.setCurrentSoundtrack(soundtrackId)
                            },
                            onMoveSoundtrackUp = { soundtrackId ->
                                viewModel.moveSoundtrack(soundtrackId, -1)
                            },
                            onMoveSoundtrackDown = { soundtrackId ->
                                viewModel.moveSoundtrack(soundtrackId, 1)
                            },
                            onResetSoundtrackPlaylist = {
                                viewModel.resetSoundtrackPlaylist()
                            },
                            onChibiToggle = { viewModel.setChibiEnabled(it) },
                            onChibiVoiceToggle = { viewModel.setChibiVoiceEnabled(it) },
                            onReminderToggle = { enabled ->
                                viewModel.setReminderEnabled(enabled)
                                if (enabled) {
                                    onScheduleReminderClick()
                                }
                            },
                            onEquipBadge = { slotIndex, achievementId ->
                                viewModel.equipBadge(slotIndex, achievementId)
                            },
                            onOpenAssistantHub = {
                                scope.launch {
                                    pagerState.animateScrollToPage(6, animationSpec = pageScrollAnimationSpec)
                                }
                            },
                            onSpinLuckyReward = { viewModel.spinLuckyReward() },
                            onDismissLuckySpinResult = { viewModel.dismissLuckySpinResult() },
                            onOpenShop = {
                                scope.launch {
                                    pagerState.animateScrollToPage(4, animationSpec = pageScrollAnimationSpec)
                                }
                            },
                            onScheduleNotification = {
                                onScheduleReminderClick()
                                Toast.makeText(context, "Notifikasi Harian Diaktifkan!", Toast.LENGTH_SHORT).show()
                            },
                            onBackupData = {
                                val fileName = "HabitTracker_Backup_${System.currentTimeMillis()}.json"
                                exportLauncher.launch(fileName)
                            },
                            onRestoreData = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            onLogout = { viewModel.logout() }
                        )
                    }
                    4 -> {
                        appData?.let { data ->
                            ShopScreen(
                                appData = data,
                                onBuy = { viewModel.buyItem(it) },
                                onEquip = { viewModel.equipItem(it) }
                            )
                        }
                    }
                    5 -> {
                        val (rewardXp, rewardCoins) = viewModel.getFocusReward(focusState.selectedDurationMinutes)
                        FocusScreen(
                            userName = appData?.userName ?: "Petualang",
                            focusState = focusState,
                            focusSummary = focusSummary,
                            rewardXp = rewardXp,
                            rewardCoins = rewardCoins,
                            habits = appData?.habits ?: emptyList(),
                            linkedHabitId = appData?.focusLinkedHabitId,
                            onBack = {
                                scope.launch {
                                    pagerState.animateScrollToPage(0, animationSpec = pageScrollAnimationSpec)
                                }
                            },
                            onSelectLinkedHabit = { viewModel.setFocusLinkedHabit(it) },
                            onSelectDuration = { viewModel.selectFocusDuration(it) },
                            onUpdateDailyTarget = { viewModel.updateFocusDailyTarget(it) },
                            onToggleTimer = { viewModel.toggleFocusTimer() },
                            onResetTimer = { viewModel.resetFocusTimer() },
                            onDismissCompletion = { viewModel.dismissFocusCompletion() }
                        )
                    }
                    6 -> {
                        appData?.let { data ->
                            AssistantInboxScreen(
                                appData = data,
                                assistants = AssistantRoster.ITEMS,
                                onBack = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(3, animationSpec = pageScrollAnimationSpec)
                                    }
                                },
                                onOpenChat = { assistantId ->
                                    viewModel.selectAssistant(assistantId)
                                    scope.launch {
                                        pagerState.animateScrollToPage(7, animationSpec = pageScrollAnimationSpec)
                                    }
                                }
                            )
                        }
                    }
                    7 -> {
                        appData?.let { data ->
                            AssistantChatScreen(
                                appData = data,
                                assistants = AssistantRoster.ITEMS,
                                selectedAssistantId = data.selectedAssistantId,
                                onBack = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(6, animationSpec = pageScrollAnimationSpec)
                                    }
                                },
                                onSendMessage = { message ->
                                    viewModel.sendMessageToAssistant(message)
                                },
                                onClearConversation = { assistantId ->
                                    viewModel.clearAssistantConversation(assistantId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. TOMBOL FLOAT (FAB)
        val fabScale by animateFloatAsState(
            targetValue = if (isUserIdle) 0f else 1f,
            animationSpec = tween(
                durationMillis = UI_TRANSITION_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            label = "fabScale"
        )

        AnimatedVisibility(
            visible = pagerState.currentPage == 0,
            enter = scaleIn(
                animationSpec = tween(UI_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(UI_TRANSITION_DURATION_MS)),
            exit = scaleOut(animationSpec = tween(220)) + fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 90.dp, end = 20.dp)
                .scale(fabScale)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(5, animationSpec = pageScrollAnimationSpec)
                        }
                    },
                    containerColor = if (isIkuyoTheme || isHuTaoTheme || isFurinaTheme || isMoonlitTheme || isForestCampTheme || isArcadeTheme) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                        CardBackground
                    },
                    contentColor = if (isIkuyoTheme || isHuTaoTheme || isFurinaTheme || isMoonlitTheme || isForestCampTheme || isArcadeTheme) MaterialTheme.colorScheme.primary else TextColorPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Timer, "Fokus")
                }

                FloatingActionButton(
                    onClick = { showAddOptionsSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah")
                }
            }
        }

        // 3. MENU NAVIGASI GLASS
        GlassBottomNavigation(
            selectedIndex = if (pagerState.currentPage >= 4) 3 else pagerState.currentPage,
            isIdle = isUserIdle,
            onItemSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index, animationSpec = pageScrollAnimationSpec)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
        )

        activeTopBanner?.let { banner ->
            TopEventBanner(
                title = banner.title,
                message = banner.message,
                icon = banner.icon,
                iconTint = banner.iconTint,
                highlightText = banner.highlightText,
                onDismiss = { activeTopBanner = null }
            )
        }
    }

    // --- MODAL SHEETS & DIALOGS ---

    dailyReward?.let { reward ->
        DailyLoginDialog(
            currentDayIndex = appData?.loginStreakIndex ?: 0,
            rewardToday = reward,
            onClaim = { viewModel.claimDailyReward() }
        )
    }

    if (showNameEditDialog) {
        val userName by viewModel.userName.collectAsStateWithLifecycle()
        EditNameDialog(userName, { showNameEditDialog = false }) { viewModel.updateUserName(it) }
    }

    if (showAvatarPickerSheet) {
        val avatarList by viewModel.avatarListWithLockStatus.collectAsStateWithLifecycle()
        ModalBottomSheet(onDismissRequest = { showAvatarPickerSheet = false }, containerColor = CardBackground) {
            AvatarPickerSheet(
                avatarList = avatarList,
                currentAvatarId = appData?.profileImageId ?: "avatar_level1",
                onAvatarSelected = {
                    viewModel.updateProfileImageId(it)
                    scope.launch { showAvatarPickerSheet = false }
                },
                onPickFromGallery = {
                    showAvatarPickerSheet = false
                    profilePhotoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    }

    if (showTitlePickerSheet) {
        val titleList by viewModel.titleListWithLockStatus.collectAsStateWithLifecycle()
        val currentTitle by viewModel.userTitle.collectAsStateWithLifecycle()
        ModalBottomSheet(onDismissRequest = { showTitlePickerSheet = false }, containerColor = DarkBackground) {
            TitlePickerSheet(titleList, currentTitle) {
                viewModel.updateUserTitle(it)
                scope.launch { showTitlePickerSheet = false }
            }
        }
    }

    if (showAddOptionsSheet) {
        ModalBottomSheet(onDismissRequest = { showAddOptionsSheet = false }, containerColor = CardBackground) {
            AddOptionsSheet(
                onManualAddClick = { scope.launch { showAddOptionsSheet = false; habitToEdit = null; showManualAddSheet = true } },
                onTemplateAddClick = { scope.launch { showAddOptionsSheet = false; showTemplateSheet = true } },
                onRecommendationClick = { scope.launch { showAddOptionsSheet = false; showRecommendationSheet = true } }
            )
        }
    }

    if (showRecommendationSheet) {
        ModalBottomSheet(onDismissRequest = { showRecommendationSheet = false }, containerColor = DarkBackground) {
            RecommendationHabitSheet(
                userLevel = appData?.level ?: 1,
                userStreak = appData?.streak ?: 0,
                initialActivityType = appData?.recommendationActivityType.orEmpty(),
                initialField = appData?.recommendationField.orEmpty(),
                initialStage = appData?.recommendationStage.orEmpty(),
                initialAge = appData?.recommendationAge,
                onPreferencesChanged = { activityType, field, stage, age ->
                    viewModel.updateRecommendationPreferences(activityType, field, stage, age)
                }
            ) { template ->
                scope.launch {
                    viewModel.addHabit(template.name, template.schedule, template.weight)
                }
                Toast.makeText(context, "${template.name} ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showManualAddSheet) {
        ModalBottomSheet(onDismissRequest = { showManualAddSheet = false }, containerColor = CardBackground) {
            ManualAddHabitSheet(
                habitToEdit = habitToEdit,
                defaultReminderTime = appData?.defaultReminderTime ?: "20:00",
                onConfirm = { name, weight, scheduleType, customDays, targetPerWeek, reminderEnabled, reminderTime ->
                scope.launch {
                    if (habitToEdit == null) {
                        viewModel.addHabit(
                            name = name,
                            weight = weight,
                            scheduleType = scheduleType,
                            customDays = customDays,
                            targetPerWeek = targetPerWeek,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime
                        )
                    } else {
                        viewModel.updateHabit(
                            id = habitToEdit!!.id,
                            name = name,
                            weight = weight,
                            scheduleType = scheduleType,
                            customDays = customDays,
                            targetPerWeek = targetPerWeek,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime
                        )
                    }
                    showManualAddSheet = false
                }
            },
                onCancel = { scope.launch { showManualAddSheet = false } }
            )
        }
    }

    if (showTemplateSheet) {
        ModalBottomSheet(onDismissRequest = { showTemplateSheet = false }, containerColor = DarkBackground) {
            TemplateHabitSheet(predefinedHabitTemplates) { t ->
                scope.launch { viewModel.addHabit(t.name, t.schedule, t.weight) }
                Toast.makeText(context, "${t.name} ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (habitToDelete != null) {
        DeleteConfirmationDialog(habitToDelete!!.name, {
            viewModel.deleteHabit(habitToDelete!!.id)
            habitToDelete = null
        }, { habitToDelete = null })
    }

}


// Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ GLASS MENU Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
@Composable
fun GlassBottomNavigation(
    selectedIndex: Int,
    isIdle: Boolean,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.AutoMirrored.Filled.List, "Habits"),
        Triple(1, Icons.Filled.BarChart, "Stats"),
        Triple(2, Icons.Filled.EmojiEvents, "Prestasi"),
        Triple(3, Icons.Filled.Person, "Profil")
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isIdle) 0.85f else 1f,
        animationSpec = tween(IDLE_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "scale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isIdle) 0.4f else 0.95f,
        animationSpec = tween(IDLE_TRANSITION_DURATION_MS),
        label = "alpha"
    )
    val animatedWidthFraction by animateFloatAsState(
        targetValue = if (isIdle) 0.6f else 1f,
        animationSpec = tween(IDLE_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "width"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedWidthFraction)
                .scale(animatedScale)
                .alpha(animatedAlpha)
                .height(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(Color(0xFF252525).copy(alpha = 0.90f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(35.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (index, icon, label) ->
                    val isSelected = selectedIndex == index
                    val targetColor = if (isSelected) MaterialTheme.colorScheme.primary else TextColorSecondary.copy(alpha = 0.6f)
                    val iconColor by animateColorAsState(
                        targetColor,
                        tween(durationMillis = UI_TRANSITION_DURATION_MS),
                        label = "iconColor"
                    )

                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onItemSelected(index) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, label, tint = iconColor, modifier = Modifier.size(26.dp))
                        AnimatedVisibility(
                            visible = isSelected && !isIdle,
                            enter = scaleIn(
                                animationSpec = tween(UI_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(UI_TRANSITION_DURATION_MS)),
                            exit = scaleOut(animationSpec = tween(180)) + fadeOut(animationSpec = tween(150))
                        ) {
                            Box(modifier = Modifier.padding(top = 4.dp).size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        }
                    }
                }
            }
        }
    }
}





