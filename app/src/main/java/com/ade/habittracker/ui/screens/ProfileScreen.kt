package com.ade.habittracker.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ade.habittracker.data.LuckySpinConfig
import com.ade.habittracker.data.LuckyRewardType
import com.ade.habittracker.data.LuckySpinResult
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.data.SoundtrackItem
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.ErrorColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userTitle: String,
    level: Int,
    totalXp: Int,
    streak: Int,
    totalLoginDays: Int,
    xpProgress: Int,
    maxXp: Int,
    @DrawableRes profileImageResId: Int,
    customProfileImagePath: String?,
    customCoverImagePath: String?,
    activeAvatarFrameId: String,
    achievements: List<Achievement>,
    equippedBadges: Map<Int, Achievement>,
    activeAssistantName: String,
    activeAssistantRole: String,
    activeAssistantAssetName: String,
    tickets: Int,
    canUseLuckySpin: Boolean,
    luckySpinResult: LuckySpinResult?,
    currentSoundtrackId: String,
    availableSoundtracks: List<SoundtrackItem>,
    selectedSoundtrackIds: List<String>,
    isOriginalSoundtrackEnabled: Boolean,
    isChibiEnabled: Boolean,
    isChibiVoiceEnabled: Boolean,
    isReminderEnabled: Boolean,
    onNameClick: () -> Unit,
    onCoverClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onTitleClick: () -> Unit,
    onOriginalSoundtrackToggle: (Boolean) -> Unit,
    onToggleSoundtrackSelection: (String) -> Unit,
    onSetCurrentSoundtrack: (String) -> Unit,
    onMoveSoundtrackUp: (String) -> Unit,
    onMoveSoundtrackDown: (String) -> Unit,
    onResetSoundtrackPlaylist: () -> Unit,
    onChibiToggle: (Boolean) -> Unit,
    onChibiVoiceToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onEquipBadge: (Int, String?) -> Unit,
    onOpenAssistantHub: () -> Unit,
    onSpinLuckyReward: () -> Unit,
    onDismissLuckySpinResult: () -> Unit,
    onOpenShop: () -> Unit,
    onScheduleNotification: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onLogout: () -> Unit
) {
    val palette = remember(level) { profilePaletteForLevel(level) }
    val avatarFrame = remember(activeAvatarFrameId) { ShopRepository.getAvatarFrameById(activeAvatarFrameId) }
    val unlockedAchievements = remember(achievements) { achievements.filter { it.isUnlocked } }
    val safeProgress = if (maxXp > 0) xpProgress.toFloat() / maxXp.toFloat() else 0f
    val normalizedPlaylist = remember(availableSoundtracks, selectedSoundtrackIds) {
        selectedSoundtrackIds.filter { selectedId -> availableSoundtracks.any { it.id == selectedId } }
    }
    val currentSoundtrackName = availableSoundtracks.firstOrNull { it.id == currentSoundtrackId }?.name ?: "Morning Quest"
    val selectedSoundtracks = remember(availableSoundtracks, normalizedPlaylist) {
        availableSoundtracks.filter { it.id in normalizedPlaylist }
    }
    val nextSoundtrackName = remember(currentSoundtrackId, normalizedPlaylist, availableSoundtracks) {
        if (normalizedPlaylist.size <= 1) {
            null
        } else {
            val currentIndex = normalizedPlaylist.indexOf(currentSoundtrackId).takeIf { it >= 0 } ?: 0
            val nextId = normalizedPlaylist[(currentIndex + 1) % normalizedPlaylist.size]
            availableSoundtracks.firstOrNull { it.id == nextId }?.name
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "profileXpProgress"
    )
    var showBadgeSelector by remember { mutableStateOf(false) }
    var selectedSlotIndex by remember { mutableStateOf<Int?>(null) }
    var showSoundtrackSheet by remember { mutableStateOf(false) }
    var isLuckySpinAnimating by remember { mutableStateOf(false) }
    var luckySpinPreviewIndex by remember { mutableStateOf(0) }
    var luckyWheelRotationTarget by remember { mutableStateOf(0f) }
    val luckyPreviewReward = remember(luckySpinPreviewIndex) {
        LuckySpinConfig.REWARDS[luckySpinPreviewIndex % LuckySpinConfig.REWARDS.size]
    }

    LaunchedEffect(isLuckySpinAnimating) {
        if (!isLuckySpinAnimating) return@LaunchedEffect
        repeat(10) {
            delay(85)
            luckySpinPreviewIndex = (luckySpinPreviewIndex + 1) % LuckySpinConfig.REWARDS.size
        }
        onSpinLuckyReward()
    }

    LaunchedEffect(luckySpinResult) {
        if (luckySpinResult != null) {
            isLuckySpinAnimating = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PROFIL PETUALANG",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rapikan identitasmu, ganti sampul, dan atur badge favorit di satu tempat.",
                    fontSize = 12.sp,
                    color = TextColorSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.28f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        if (!customCoverImagePath.isNullOrBlank()) {
                            AsyncImage(
                                model = File(customCoverImagePath),
                                contentDescription = "Sampul profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(122.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .clickable { onCoverClick() }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(122.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                palette.primary.copy(alpha = 0.88f),
                                                palette.secondary.copy(alpha = 0.74f),
                                                CardBackground
                                            )
                                        )
                                    )
                                    .clickable { onCoverClick() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 20.dp, y = (-18).dp)
                                        .size(112.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(x = (-14).dp, y = 16.dp)
                                        .size(86.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.12f))
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(122.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.12f),
                                            Color.Black.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )

                        CoverActionChip(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 12.dp, end = 12.dp),
                            onClick = onCoverClick
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                avatarFrame.primaryColor.copy(alpha = 0.45f),
                                                palette.glow,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            if (!customProfileImagePath.isNullOrBlank()) {
                                AsyncImage(
                                    model = File(customProfileImagePath),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = profileImageResId),
                                    placeholder = painterResource(id = profileImageResId),
                                    modifier = Modifier
                                        .size(102.dp)
                                        .clip(CircleShape)
                                        .border(5.dp, avatarFrame.secondaryColor.copy(alpha = 0.72f), CircleShape)
                                        .border(2.dp, avatarFrame.primaryColor, CircleShape)
                                        .clickable { onAvatarClick() }
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = profileImageResId),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(102.dp)
                                        .clip(CircleShape)
                                        .border(5.dp, avatarFrame.secondaryColor.copy(alpha = 0.72f), CircleShape)
                                        .border(2.dp, avatarFrame.primaryColor, CircleShape)
                                        .clickable { onAvatarClick() }
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(containerColor = palette.primary),
                                border = BorderStroke(2.dp, CardBackground),
                                modifier = Modifier.offset(y = 10.dp)
                            ) {
                                Text(
                                    text = "LVL $level | ${avatarFrame.displayName}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(34.dp))

                    Text(
                        text = "Ubah avatar",
                        color = palette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onAvatarClick() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName,
                            color = TextColorPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 52.dp)
                        )
                        IconButton(
                            onClick = onNameClick,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit nama",
                                tint = TextColorSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userTitle,
                        color = palette.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTitleClick() }
                            .background(palette.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 13.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress ke level berikutnya",
                                color = TextColorSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$xpProgress / $maxXp XP",
                                color = palette.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(11.dp)
                                .clip(RoundedCornerShape(40.dp)),
                            color = palette.primary,
                            trackColor = Color(0xFF333333),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            title = "Tier",
                            value = palette.label,
                            accent = palette.primary
                        )
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            title = "XP",
                            value = totalXp.toString(),
                            accent = palette.secondary
                        )
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            title = "Streak",
                            value = "$streak h",
                            accent = AccentYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileInfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Hari Login",
                    value = "$totalLoginDays",
                    subtitle = "Kehadiran",
                    accent = Color(0xFF42A5F5)
                )
                ProfileInfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Sisa XP",
                    value = "${(maxXp - xpProgress).coerceAtLeast(0)}",
                    subtitle = "Menuju level baru",
                    accent = palette.primary
                )
            }
        }

        item {
            ProfileSectionCard(
                title = "Badge Aktif",
                subtitle = "Pilih sampai 3 badge favorit untuk tampil di profilmu."
            ) {
                ActiveBadgesCard(
                    equippedBadges = equippedBadges,
                    onBadgeClick = { slotIndex ->
                        selectedSlotIndex = slotIndex
                        showBadgeSelector = true
                    }
                )
            }
        }

        item {
            ProfileSectionCard(
                title = "Asisten BitQuest",
                subtitle = "Buka ruang chat waifu assistant dan pilih partner yang paling cocok sama mood produktifmu."
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = palette.secondary.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.22f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = activeAssistantName,
                                    color = TextColorPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = activeAssistantRole,
                                    color = palette.secondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Nama file gambar: $activeAssistantAssetName",
                                    color = TextColorSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(palette.secondary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = palette.secondary
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAssistantHub,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.secondary.copy(alpha = 0.18f),
                                contentColor = palette.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka Chat Asisten",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            val luckyDisplayReward = if (isLuckySpinAnimating) luckyPreviewReward else luckySpinResult?.reward
            val luckyAccent = luckyDisplayReward?.let { Color(it.accentHex) } ?: palette.primary
            val isFreeSpinReady = canUseLuckySpin && tickets <= 0
            val animatedLuckyScale by animateFloatAsState(
                targetValue = if (isLuckySpinAnimating) 1.12f else 1f,
                animationSpec = tween(durationMillis = 280),
                label = "luckySpinPulse"
            )
            val animatedLuckyRotation by animateFloatAsState(
                targetValue = luckyWheelRotationTarget,
                animationSpec = tween(durationMillis = 960),
                label = "luckySpinRotation"
            )
            ProfileSectionCard(
                title = "Lucky Reward",
                subtitle = "Ambil 1 spin gratis per hari. Setelah itu, kamu masih bisa lanjut pakai tiket kalau mau lanjut ngetes hoki."
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = luckyAccent.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, luckyAccent.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isFreeSpinReady) "Spin gratis hari ini masih siap" else "Spin berikutnya pakai tiket",
                                    color = TextColorPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isFreeSpinReady) {
                                        "Sekali tap buat klaim hadiah acak. Kalau mau lanjut lagi, tinggal pakai tiket yang kamu punya."
                                    } else {
                                        "Spin gratis hari ini sudah kepakai. Kamu masih bisa lanjut selama stok tiketmu belum habis."
                                    },
                                    color = TextColorSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            LuckySpinWheelPreview(
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = animatedLuckyRotation
                                    }
                                    .graphicsLayer {
                                        scaleX = animatedLuckyScale
                                        scaleY = animatedLuckyScale
                                    },
                                accent = luckyAccent,
                                rewardLabel = luckyDisplayReward?.title ?: "Mystery"
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = luckyAccent.copy(alpha = 0.11f)
                            ),
                            border = BorderStroke(1.dp, luckyAccent.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isLuckySpinAnimating) "Hadiah sedang diacak..." else "Preview hadiah yang bisa keluar",
                                    color = TextColorSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = luckyDisplayReward?.title ?: "Coin, XP, atau tiket bonus",
                                    color = TextColorPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = luckyDisplayReward?.let { "${luckyRewardAmountLabel(it.type, it.amount)} • ${it.rarityLabel}" }
                                        ?: "Pool reward dibuat campuran biar tiap spin terasa beda.",
                                    color = luckyAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuckyRewardStatChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.LocalActivity,
                                title = "Tiket",
                                value = tickets.toString(),
                                accent = Color(0xFF64B5F6)
                            )
                            LuckyRewardStatChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Refresh,
                                title = "Status",
                                value = if (isFreeSpinReady) "Gratis" else if (canUseLuckySpin) "Pakai Tiket" else "Habis",
                                accent = luckyAccent
                            )
                        }

                        Button(
                            onClick = {
                                if (!isLuckySpinAnimating) {
                                    luckyWheelRotationTarget += 1080f
                                    isLuckySpinAnimating = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canUseLuckySpin && !isLuckySpinAnimating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = luckyAccent.copy(alpha = 0.2f),
                                contentColor = luckyAccent,
                                disabledContainerColor = Color.White.copy(alpha = 0.06f),
                                disabledContentColor = TextColorSecondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isLuckySpinAnimating -> "Mengacak Hadiah..."
                                    isFreeSpinReady -> "Spin Gratis Sekarang"
                                    canUseLuckySpin -> "Pakai 1 Tiket untuk Spin"
                                    else -> "Tiket Habis"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            ProfileSectionCard(
                title = "Playlist Soundtrack",
                subtitle = "Atur antrean soundtrack favoritmu. Yang tampil di sini hanya track yang sudah kamu masukkan ke playlist."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentSoundtrackName,
                                color = TextColorPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    !isOriginalSoundtrackEnabled -> "Soundtrack sedang dimatikan. Nyalakan kapan saja kalau mau suasana app terasa hidup lagi."
                                    normalizedPlaylist.size <= 1 -> "Playlist kamu sedang fokus di satu track ini."
                                    else -> "${normalizedPlaylist.size} track siap diputar. Setelah ini lanjut ke ${nextSoundtrackName ?: currentSoundtrackName}."
                                },
                                color = TextColorSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isOriginalSoundtrackEnabled,
                            onCheckedChange = onOriginalSoundtrackToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = palette.secondary,
                                uncheckedThumbColor = TextColorSecondary,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.14f)
                            )
                        )
                    }

                    if (selectedSoundtracks.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedSoundtracks.forEach { soundtrack ->
                                val playlistPosition = normalizedPlaylist.indexOf(soundtrack.id).takeIf { it >= 0 }?.plus(1)
                                CompactSoundtrackChip(
                                    soundtrack = soundtrack,
                                    playlistPosition = playlistPosition ?: 1,
                                    isCurrent = currentSoundtrackId == soundtrack.id,
                                    isSoundtrackEnabled = isOriginalSoundtrackEnabled,
                                    accent = if (currentSoundtrackId == soundtrack.id) palette.secondary else palette.primary,
                                    onClick = { onSetCurrentSoundtrack(soundtrack.id) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showSoundtrackSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.secondary.copy(alpha = 0.16f),
                            contentColor = palette.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Atur Playlist",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            ProfileSectionCard(
                title = "Pengaturan",
                subtitle = "Atur perilaku aplikasi dan pengalaman harianmu."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ProfileToggleRow(
                        title = "Notifikasi harian",
                        subtitle = "Aktifkan pengingat agar ritme misi tetap terjaga.",
                        checked = isReminderEnabled,
                        accent = palette.primary,
                        onCheckedChange = onReminderToggle
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileToggleRow(
                        title = "Tampilkan chibi",
                        subtitle = "Tampilkan karakter pendamping di layar.",
                        checked = isChibiEnabled,
                        accent = AccentYellow,
                        onCheckedChange = onChibiToggle
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileToggleRow(
                        title = "Suara chibi",
                        subtitle = "Mainkan suara saat chibi aktif di aplikasi.",
                        checked = isChibiVoiceEnabled,
                        accent = Color(0xFF81C784),
                        onCheckedChange = onChibiVoiceToggle
                    )
                }
            }
        }

        item {
            ProfileSectionCard(
                title = "Akun dan Data",
                subtitle = "Kelola toko, backup lokal, dan sesi akun dari sini."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ShoppingCart,
                            label = "Buka Toko",
                            accent = palette.primary,
                            onClick = onOpenShop
                        )
                        ProfileActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Notifications,
                            label = "Atur Alarm",
                            accent = palette.secondary,
                            onClick = onScheduleNotification
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Save,
                            label = "Backup",
                            accent = Color(0xFF4DB6AC),
                            onClick = onBackupData
                        )
                        ProfileActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Refresh,
                            label = "Restore",
                            accent = AccentYellow,
                            onClick = onRestoreData
                        )
                    }
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorColor.copy(alpha = 0.18f),
                            contentColor = ErrorColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keluar dari Akun",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showBadgeSelector) {
        ModalBottomSheet(
            onDismissRequest = { showBadgeSelector = false },
            containerColor = CardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih Badge",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow
                )
                Text(
                    text = "Badge yang terbuka bisa dipasang ke slot profilmu.",
                    fontSize = 12.sp,
                    color = TextColorSecondary
                )

                val slotIndex = selectedSlotIndex
                if (slotIndex != null && equippedBadges[slotIndex] != null) {
                    Button(
                        onClick = {
                            onEquipBadge(slotIndex, null)
                            showBadgeSelector = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = TextColorPrimary
                        )
                    ) {
                        Text("Lepas badge dari slot ini", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (unlockedAchievements.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.92f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = "Belum ada badge yang terbuka. Selesaikan lebih banyak pencapaian dulu.",
                            color = TextColorSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 340.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(unlockedAchievements, key = { it.id }) { achievement ->
                            val isEquipped = equippedBadges.values.any { it.id == achievement.id }
                            BadgeSelectionItem(
                                achievement = achievement,
                                isEquipped = isEquipped,
                                onClick = {
                                    selectedSlotIndex?.let { index ->
                                        onEquipBadge(index, achievement.id)
                                        showBadgeSelector = false
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showSoundtrackSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSoundtrackSheet = false },
            containerColor = CardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kelola Playlist Soundtrack",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow
                )
                Text(
                    text = "Pilih track yang mau masuk antrean, susun urutannya, lalu tentukan titik mulai sesuai mood yang kamu mau.",
                    fontSize = 12.sp,
                    color = TextColorSecondary
                )

                Button(
                    onClick = onResetSoundtrackPlaylist,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = TextColorPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset ke Default",
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    availableSoundtracks.forEach { soundtrack ->
                        val playlistPosition = normalizedPlaylist.indexOf(soundtrack.id).takeIf { it >= 0 }?.plus(1)
                        SoundtrackTrackCard(
                            soundtrack = soundtrack,
                            isCurrent = currentSoundtrackId == soundtrack.id,
                            isSelected = soundtrack.id in normalizedPlaylist,
                            playlistPosition = playlistPosition,
                            isSoundtrackEnabled = isOriginalSoundtrackEnabled,
                            canMoveUp = playlistPosition != null && playlistPosition > 1,
                            canMoveDown = playlistPosition != null && playlistPosition < normalizedPlaylist.size,
                            accent = if (currentSoundtrackId == soundtrack.id) palette.secondary else palette.primary,
                            onToggleSelected = { onToggleSoundtrackSelection(soundtrack.id) },
                            onSetCurrent = { onSetCurrentSoundtrack(soundtrack.id) },
                            onMoveUp = { onMoveSoundtrackUp(soundtrack.id) },
                            onMoveDown = { onMoveSoundtrackDown(soundtrack.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    luckySpinResult?.let { result ->
        val rewardAccent = Color(result.reward.accentHex)
        AlertDialog(
            onDismissRequest = onDismissLuckySpinResult,
            containerColor = CardBackground,
            title = {
                Text(
                    text = "Lucky Reward Dapat!",
                    color = TextColorPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = rewardAccent.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(1.dp, rewardAccent.copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = result.reward.title,
                                color = TextColorPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = luckyRewardAmountLabel(result.reward.type, result.reward.amount),
                                color = rewardAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${result.reward.rarityLabel} • ${result.reward.description}",
                                color = TextColorSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = if (result.usedTicket) {
                            "Spin ini memakai 1 tiket. Kalau masih ada stok, kamu bisa lanjut putar lagi."
                        } else {
                            "Ini spin gratis harianmu. Besok kamu bisa klaim gratis lagi, atau lanjut sekarang pakai tiket."
                        },
                        color = TextColorSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissLuckySpinResult) {
                    Text(
                        text = "Sip",
                        color = rewardAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun CoverActionChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Ubah Sampul",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextColorPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                text = subtitle,
                color = TextColorSecondary,
                fontSize = 12.sp
            )
            content()
        }
    }
}

@Composable
private fun ProfileStatPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                color = TextColorSecondary,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = TextColorPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                color = TextColorSecondary,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = TextColorPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                color = TextColorPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextColorSecondary,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedThumbColor = TextColorSecondary,
                uncheckedTrackColor = Color.White.copy(alpha = 0.14f)
            )
        )
    }
}

@Composable
private fun ProfileActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.16f),
            contentColor = accent
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun LuckyRewardStatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.11f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = TextColorSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    color = TextColorPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LuckySpinWheelPreview(
    modifier: Modifier = Modifier,
    accent: Color,
    rewardLabel: String
) {
    Box(
        modifier = modifier
            .size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .width(18.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(AccentYellow)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.95f),
                            AccentYellow.copy(alpha = 0.9f),
                            accent.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.75f),
                            accent.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(2.dp, accent.copy(alpha = 0.35f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(CardBackground.copy(alpha = 0.96f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = rewardLabel,
                    color = TextColorPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

private fun luckyRewardAmountLabel(type: LuckyRewardType, amount: Int): String {
    return when (type) {
        LuckyRewardType.COINS -> "+$amount Coin"
        LuckyRewardType.XP -> "+$amount XP"
        LuckyRewardType.TICKETS -> "+$amount Tiket"
    }
}

@Composable
private fun SoundtrackTrackCard(
    soundtrack: SoundtrackItem,
    isCurrent: Boolean,
    isSelected: Boolean,
    playlistPosition: Int?,
    isSoundtrackEnabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    accent: Color,
    onToggleSelected: () -> Unit,
    onSetCurrent: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val isLive = isCurrent && isSoundtrackEnabled
    val pulseTransition = rememberInfiniteTransition(label = "soundtrackCardPulse")
    val pulseAlpha = if (isLive) {
        pulseTransition.animateFloat(
            initialValue = 0.18f,
            targetValue = 0.34f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "soundtrackCardPulseAlpha"
        ).value
    } else {
        0.14f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) accent.copy(alpha = pulseAlpha) else Color.Black.copy(alpha = 0.16f)
        ),
        border = BorderStroke(
            1.dp,
            if (isCurrent) accent.copy(alpha = if (isLive) 0.58f else 0.42f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = if (isCurrent) 0.28f else 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLive) {
                                LiveIndicatorDot(accent = accent)
                                MiniEqualizer(accent = accent)
                            }
                            Text(
                                text = soundtrack.name,
                                color = TextColorPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = when {
                                isLive -> "Sedang diputar sekarang"
                                isCurrent && isSelected -> "Sedang jadi titik mulai playlist"
                                isSelected -> "Masuk antrean putar"
                                else -> "Tidak dipakai di playlist"
                            },
                            color = TextColorSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = when {
                        isLive && playlistPosition != null -> "Now Playing • #$playlistPosition"
                        isCurrent && playlistPosition != null -> "Now • #$playlistPosition"
                        isSelected && playlistPosition != null -> "Queue #$playlistPosition"
                        else -> "Off"
                    },
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleSelected,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent
                    )
                ) {
                    Text(
                        text = if (isSelected) "Keluarkan" else "Masukkan",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onSetCurrent,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = TextColorPrimary
                    )
                ) {
                    Text(
                        text = if (isCurrent) "Track Sekarang" else "Mulai Dari Sini",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            if (isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (canMoveUp) accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)
                            ),
                        enabled = canMoveUp
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (canMoveUp) accent else TextColorSecondary.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (canMoveDown) accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)
                            ),
                        enabled = canMoveDown
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (canMoveDown) accent else TextColorSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactSoundtrackChip(
    soundtrack: SoundtrackItem,
    playlistPosition: Int,
    isCurrent: Boolean,
    isSoundtrackEnabled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val isLive = isCurrent && isSoundtrackEnabled
    val pulseTransition = rememberInfiniteTransition(label = "soundtrackChipPulse")
    val pulseAlpha = if (isLive) {
        pulseTransition.animateFloat(
            initialValue = 0.16f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "soundtrackChipPulseAlpha"
        ).value
    } else {
        if (isCurrent) 0.16f else 0.04f
    }

    Card(
        modifier = Modifier
            .width(166.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) accent.copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.04f)
        ),
        border = BorderStroke(
            1.dp,
            if (isCurrent) accent.copy(alpha = if (isLive) 0.62f else 0.46f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when {
                    isLive -> "Now Playing • #$playlistPosition"
                    isCurrent -> "Now • #$playlistPosition"
                    else -> "Queue #$playlistPosition"
                },
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLive) {
                    LiveIndicatorDot(accent = accent)
                    MiniEqualizer(accent = accent)
                }
                Text(
                    text = soundtrack.name,
                    color = TextColorPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = when {
                    isLive -> "Sedang diputar sekarang"
                    isCurrent -> "Sedang jadi titik mulai"
                    else -> "Ketuk untuk jadikan titik mulai"
                },
                color = TextColorSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveIndicatorDot(
    accent: Color
) {
    val transition = rememberInfiniteTransition(label = "liveDotPulse")
    val alpha = transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    ).value

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = alpha))
    )
}

@Composable
private fun MiniEqualizer(
    accent: Color
) {
    val transition = rememberInfiniteTransition(label = "miniEqualizer")
    val bar1 = transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar1"
    ).value
    val bar2 = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar2"
    ).value
    val bar3 = transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 460),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar3"
    ).value

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        EqualizerBar(heightFactor = bar1, accent = accent)
        EqualizerBar(heightFactor = bar2, accent = accent)
        EqualizerBar(heightFactor = bar3, accent = accent)
    }
}

@Composable
private fun EqualizerBar(
    heightFactor: Float,
    accent: Color
) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .height((14f * heightFactor).dp)
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.95f))
    )
}

@Composable
private fun BadgeSelectionItem(
    achievement: Achievement,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
        border = BorderStroke(
            1.dp,
            if (isEquipped) AccentYellow.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = achievement.imageResId),
                contentDescription = achievement.title,
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = achievement.title,
                color = TextColorPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isEquipped) "Dipakai" else "Pilih",
                color = if (isEquipped) AccentYellow else TextColorSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class ProfilePalette(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val glow: Color
)

private fun profilePaletteForLevel(level: Int): ProfilePalette {
    return when {
        level >= 30 -> ProfilePalette(
            label = "Legend",
            primary = Color(0xFFFFB300),
            secondary = Color(0xFFFF7043),
            glow = Color(0x66FFB300)
        )
        level >= 24 -> ProfilePalette(
            label = "Master",
            primary = Color(0xFFEF5350),
            secondary = Color(0xFFFFA726),
            glow = Color(0x66EF5350)
        )
        level >= 18 -> ProfilePalette(
            label = "Elite",
            primary = Color(0xFF29B6F6),
            secondary = Color(0xFF26A69A),
            glow = Color(0x6629B6F6)
        )
        level >= 12 -> ProfilePalette(
            label = "Challenger",
            primary = Color(0xFFAB47BC),
            secondary = Color(0xFF5C6BC0),
            glow = Color(0x66AB47BC)
        )
        level >= 6 -> ProfilePalette(
            label = "Explorer",
            primary = Color(0xFF66BB6A),
            secondary = Color(0xFFFFCA28),
            glow = Color(0x6666BB6A)
        )
        else -> ProfilePalette(
            label = "Rookie",
            primary = Color(0xFFFFA726),
            secondary = Color(0xFFFFD54F),
            glow = Color(0x66FFA726)
        )
    }
}

