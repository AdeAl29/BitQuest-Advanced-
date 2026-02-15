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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.data.predefinedHabitTemplates
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.components.DailyLoginDialog
import com.ade.habittracker.ui.components.DeleteConfirmationDialog
import com.ade.habittracker.ui.components.EditNameDialog
import com.ade.habittracker.ui.components.RamadhanCalmBackground
import com.ade.habittracker.ui.components.RamadhanFestiveBackground
import com.ade.habittracker.ui.components.TopEventBanner
import com.ade.habittracker.ui.components.sheets.AddOptionsSheet
import com.ade.habittracker.ui.components.sheets.AvatarPickerSheet
import com.ade.habittracker.ui.components.sheets.ManualAddHabitSheet
import com.ade.habittracker.ui.components.sheets.TemplateHabitSheet
import com.ade.habittracker.ui.components.sheets.TitlePickerSheet
import com.ade.habittracker.ui.screens.AchievementsScreen
import com.ade.habittracker.ui.screens.HabitsScreen
import com.ade.habittracker.ui.screens.ShopScreen
import com.ade.habittracker.ui.screens.StatsScreen
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.DarkBackground
import com.ade.habittracker.ui.theme.ErrorColor
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.ade.habittracker.widget.HabitHomeWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val activeChibiRes = ShopRepository.getChibiResById(appData?.activeChibiSkin ?: "chibi_helper")
    val activeThemeId = appData?.activeTheme ?: "theme_default"
    val isRamadhanCalmTheme = activeThemeId == "theme_ramadhan"
    val isRamadhanFestiveTheme = activeThemeId == "theme_ramadhan_festive"

    val pagerState = rememberPagerState(pageCount = { 3 })

    // --- STATE MANAGEMENT ---
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showShopSheet by remember { mutableStateOf(false) }

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                when (page) {
                    0 -> {
                        HabitsScreen(
                            habits = appData?.habits ?: emptyList(),
                            userName = appData?.userName ?: "Petualang",
                            chibiRes = activeChibiRes,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
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
                            userTitle = appData?.userTitle ?: "Baru",
                            profileImageResId = viewModel.profileImageResId.collectAsStateWithLifecycle().value,
                            customProfileImagePath = appData?.customProfileImagePath,
                            chibiRes = activeChibiRes,
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
                            isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
                            onNameClick = { showNameEditDialog = true },
                            onAvatarClick = { showAvatarPickerSheet = true },
                            onTitleClick = { showTitlePickerSheet = true },
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
                    onClick = { showSettingsSheet = true },
                    containerColor = CardBackground,
                    contentColor = TextColorPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Settings, "Set")
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
            selectedIndex = pagerState.currentPage,
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

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SettingsSheet(
                isMusicEnabled = appData?.isMusicEnabled ?: true,
                onMusicToggle = { viewModel.setMusicEnabled(it) },
                isChibiEnabled = appData?.isChibiEnabled ?: true,
                onChibiToggle = { viewModel.setChibiEnabled(it) },
                isChibiVoiceEnabled = appData?.isChibiVoiceEnabled ?: true,
                onChibiVoiceToggle = { viewModel.setChibiVoiceEnabled(it) },
                isReminderEnabled = appData?.isReminderEnabled ?: false,
                onReminderToggle = { enabled ->
                    viewModel.setReminderEnabled(enabled)
                    if (enabled) {
                        onScheduleReminderClick()
                    }
                },
                defaultReminderTime = appData?.defaultReminderTime ?: "20:00",
                onReminderTimeChange = { time -> viewModel.updateDefaultReminderTime(time) },
                onOpenShop = {
                    showSettingsSheet = false
                    showShopSheet = true
                },
                onScheduleNotification = {
                    onScheduleReminderClick()
                    Toast.makeText(context, "Notifikasi Harian Diaktifkan!", Toast.LENGTH_SHORT).show()
                },
                onBackupData = {
                    // Pemicu Export
                    val fileName = "HabitTracker_Backup_${System.currentTimeMillis()}.json"
                    exportLauncher.launch(fileName)
                },
                onRestoreData = {
                    // Pemicu Import
                    importLauncher.launch(arrayOf("application/json"))
                },
                onLogout = {
                    showSettingsSheet = false
                    viewModel.logout()
                }
            )
        }
    }

    if (showShopSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShopSheet = false },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxHeight()
        ) {
            appData?.let { data ->
                ShopScreen(
                    appData = data,
                    onBuy = { viewModel.buyItem(it) },
                    onEquip = { viewModel.equipItem(it) }
                )
            }
        }
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
                onTemplateAddClick = { scope.launch { showAddOptionsSheet = false; showTemplateSheet = true } }
            )
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

// â”€â”€â”€ SETTINGS SHEET (UPDATED) â”€â”€â”€
@Composable
fun SettingsSheet(
    isMusicEnabled: Boolean,
    onMusicToggle: (Boolean) -> Unit,
    isChibiEnabled: Boolean,
    onChibiToggle: (Boolean) -> Unit,
    isChibiVoiceEnabled: Boolean,
    onChibiVoiceToggle: (Boolean) -> Unit,
    isReminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    defaultReminderTime: String,
    onReminderTimeChange: (String) -> Unit,
    onOpenShop: () -> Unit,
    onScheduleNotification: () -> Unit,
    onBackupData: () -> Unit, // Parameter Baru
    onRestoreData: () -> Unit, // Parameter Baru
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Pengaturan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
        Spacer(Modifier.height(24.dp))

        // --- SECTION AUDIO & VISUAL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Musik Latar", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isMusicEnabled,
                onCheckedChange = onMusicToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.3f))
            )
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tampilkan Asisten Chibi", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isChibiEnabled,
                onCheckedChange = onChibiToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.3f))
            )
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Suara Asisten Chibi", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isChibiVoiceEnabled,
                onCheckedChange = onChibiVoiceToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.3f))
            )
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Aktifkan Reminder", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isReminderEnabled,
                onCheckedChange = onReminderToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryColor,
                    checkedTrackColor = PrimaryColor.copy(alpha = 0.3f)
                )
            )
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        if (isReminderEnabled) {
            Button(
                onClick = { onScheduleNotification() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = TextColorPrimary)
            ) {
                Icon(Icons.Default.Notifications, null)
                Spacer(Modifier.width(8.dp))
                Text("Reminder aktif: tiap 3 jam (06,09,12,15,18,21)", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }

        // --- SECTION FITUR ---
        Button(
            onClick = onOpenShop,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.ShoppingCart, null)
            Spacer(Modifier.width(8.dp))
            Text("Buka Toko & Ganti Tema", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onScheduleNotification,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = PrimaryColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Notifications, null)
            Spacer(Modifier.width(8.dp))
            Text("Sinkronkan Reminder Sekarang", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // --- SECTION MANAJEMEN DATA (BARU) ---
        Text("Manajemen Data", fontSize = 14.sp, color = TextColorSecondary, modifier = Modifier.padding(vertical = 8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Tombol Backup
            Button(
                onClick = onBackupData,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = TextColorPrimary)
            ) {
                // Menggunakan Icon Save sebagai representasi Backup
                Icon(Icons.Filled.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Backup", fontSize = 13.sp)
            }

            // Tombol Restore
            Button(
                onClick = onRestoreData,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = TextColorPrimary)
            ) {
                // Menggunakan Icon Refresh/Restore
                Icon(Icons.Filled.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Restore", fontSize = 13.sp)
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))

        // --- SECTION AKUN ---
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor, contentColor = Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("Keluar", fontWeight = FontWeight.Bold)
        }
    }
}

// â”€â”€â”€ GLASS MENU â”€â”€â”€
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
        Triple(2, Icons.Filled.EmojiEvents, "Prestasi")
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


