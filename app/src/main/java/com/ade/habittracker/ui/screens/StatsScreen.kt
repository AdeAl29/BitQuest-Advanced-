package com.ade.habittracker.ui.screens

import androidx.annotation.DrawableRes
import coil.compose.AsyncImage
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ade.habittracker.R
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.HabitHistoryItem
import com.ade.habittracker.ui.components.ChibiMessage
import com.ade.habittracker.ui.components.DraggableChibiWithBubble
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.components.ProductivityHeatmap
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.ade.habittracker.ui.viewmodel.WeeklySummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    level: Int,
    streak: Int,
    totalXp: Int,
    xpProgress: Int,
    maxXp: Int,
    totalLoginDays: Int,
    userName: String,
    @DrawableRes chibiRes: Int = R.drawable.chibi_helper,
    isChibiEnabled: Boolean = true,
    isChibiVoiceEnabled: Boolean = true,
    viewModel: HabitViewModel? = null
) {
    val historyList = viewModel?.habitHistory?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val allAchievements = viewModel?.achievements?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val heatmapData = viewModel?.heatmapData?.collectAsStateWithLifecycle()?.value ?: emptyMap()
    val weeklySummary = viewModel?.weeklySummary?.collectAsStateWithLifecycle()?.value
    val appData = viewModel?.appData?.collectAsStateWithLifecycle()?.value

    val unlockedAchievements = remember(allAchievements) {
        allAchievements.filter { it.isUnlocked }
    }
    val levelPalette = remember(level) { statsPaletteForLevel(level) }
    val completionRate = weeklySummary?.completionRate?.coerceIn(0f, 1f)
        ?: if (historyList.isNotEmpty()) 1f else 0f
    val completedMissions = historyList.size
    val xpTrend7Days = remember(historyList) { buildXpTrend7Days(historyList) }
    val badgeCompletion = if (allAchievements.isNotEmpty()) {
        unlockedAchievements.size.toFloat() / allAchievements.size.toFloat()
    } else {
        0f
    }
    val bestStreak = maxOf(streak, weeklySummary?.bestStreakDays ?: 0)
    val nextTarget = when {
        streak < 3 -> 3
        streak < 7 -> 7
        streak < 14 -> 14
        streak < 30 -> 30
        else -> ((streak / 10) + 1) * 10
    }
    val xpRemaining = (maxXp - xpProgress).coerceAtLeast(0)
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showCompletedDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    val currentHour = remember { LocalTime.now().hour }
    val chibiMessages = remember(streak, totalXp, totalLoginDays, weeklySummary, currentHour) {
        val base = mutableListOf(
            ChibiMessage("Stat kamu naik, lanjut ya $userName!", R.raw.chibi_konsisten_keren),
            ChibiMessage("Cek progress mingguan dulu!", R.raw.chibi_ayo_satu_misi),
            ChibiMessage("Jangan bolong hari ini.", R.raw.chibi_jangan_bolos)
        )

        if (streak >= 7) {
            base += ChibiMessage("Streak kamu panas banget, pertahankan!", R.raw.chibi_konsisten_keren)
        } else if (streak == 0) {
            base += ChibiMessage("Yuk mulai nyalakan streak lagi hari ini.", R.raw.chibi_ayo_satu_misi)
        }

        if (weeklySummary != null && weeklySummary.completionRate >= 0.8f) {
            base += ChibiMessage("Progress mingguanmu bagus banget.", R.raw.chibi_misi_selesai)
        } else if (weeklySummary != null && weeklySummary.completionRate < 0.4f) {
            base += ChibiMessage("Minggu ini masih bisa dikejar kok.", R.raw.chibi_sedikit_lagi)
        }

        if (currentHour >= 21) {
            base += ChibiMessage("Malam juga tetap semangat ya, jangan bolong.", R.raw.chibi_jangan_bolos)
        }

        base.distinctBy { it.text }
    }

    val safeProgress = if (maxXp > 0) xpProgress.toFloat() / maxXp.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "xpProgress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        FallingSnowEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "STATISTIK PETUALANG",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentYellow,
                            letterSpacing = 1.6.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Level, konsistensi, dan performa produktivitasmu ada di sini.",
                            fontSize = 12.sp,
                            color = TextColorSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    StatsLevelHeroCard(
                        level = level,
                        xpProgress = xpProgress,
                        maxXp = maxXp,
                        totalXp = totalXp,
                        streak = streak,
                        completionRate = completionRate,
                        xpProgressFraction = animatedProgress,
                        palette = levelPalette
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Streak",
                            value = "$streak hari",
                            description = "Target berikutnya $nextTarget",
                            accent = Color(0xFFFF7043),
                            onClick = { showStreakDialog = true }
                        )
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total XP",
                            value = "$totalXp",
                            description = if (xpRemaining > 0) "$xpRemaining XP lagi" else "Naik level siap",
                            accent = levelPalette.primary,
                            onClick = { showHistoryDialog = true }
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Misi Selesai",
                            value = "$completedMissions",
                            description = "Buka detail misi",
                            accent = Color(0xFF4CAF50),
                            onClick = { showCompletedDialog = true }
                        )
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Login",
                            value = "$totalLoginDays hari",
                            description = "Cek hadiah login",
                            accent = Color(0xFF42A5F5),
                            onClick = { showLoginDialog = true }
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Prestasi",
                            value = "${unlockedAchievements.size}/${allAchievements.size}",
                            description = "${(badgeCompletion * 100).toInt()}% koleksi",
                            accent = Color(0xFFFFC107)
                        )
                        ProfileMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Konsistensi",
                            value = "${(completionRate * 100).toInt()}%",
                            description = "Best streak $bestStreak hari",
                            accent = levelPalette.secondary
                        )
                    }
                }

                item {
                    StatsInsightCard(
                        palette = levelPalette,
                        level = level,
                        xpProgress = xpProgress,
                        maxXp = maxXp,
                        streak = streak,
                        unlockedAchievements = unlockedAchievements.size,
                        totalAchievements = allAchievements.size,
                        weeklySummary = weeklySummary
                    )
                }

                item {
                    XpTrendCard(
                        data = xpTrend7Days,
                        accent = levelPalette.primary
                    )
                }
                item {
                    weeklySummary?.let {
                        WeeklySummaryCard(summary = it)
                    }
                }

                item {
                    ProductivityHeatmap(heatmapData = heatmapData)
                }
            }
        }
        if (showHistoryDialog) {
            HistoryLogDialog(historyList) { showHistoryDialog = false }
        }

        if (showStreakDialog) {
            StreakDetailDialog(
                currentStreak = streak,
                bestStreak = maxOf(streak, weeklySummary?.bestStreakDays ?: 0),
                onDismiss = { showStreakDialog = false }
            )
        }

        if (showCompletedDialog) {
            CompletedMissionsDialog(
                historyList = historyList,
                onDismiss = { showCompletedDialog = false }
            )
        }

        if (showLoginDialog) {
            LoginDetailDialog(
                totalLoginDays = totalLoginDays,
                loginStreakIndex = appData?.loginStreakIndex ?: 0,
                lastLoginDate = appData?.lastLoginDate.orEmpty(),
                onDismiss = { showLoginDialog = false }
            )
        }

        if (isChibiEnabled) {
            DraggableChibiWithBubble(
                chibiRes = chibiRes,
                messages = chibiMessages,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f),
                voiceEnabled = isChibiVoiceEnabled,
                autoBehaviorEnabled = true
            )
        }
    }
}

private data class LevelPalette(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val glow: Color
)

private fun statsPaletteForLevel(level: Int): LevelPalette = when {
    level >= 30 -> LevelPalette("LEGEND", Color(0xFFFFC857), Color(0xFFFF8A65), Color(0x66FFC857))
    level >= 20 -> LevelPalette("MASTER", Color(0xFFFF8A65), Color(0xFFFFB74D), Color(0x66FF8A65))
    level >= 15 -> LevelPalette("ELITE", Color(0xFF66BB6A), Color(0xFF26C6DA), Color(0x6666BB6A))
    level >= 10 -> LevelPalette("CHALLENGER", Color(0xFF29B6F6), Color(0xFF26A69A), Color(0x6629B6F6))
    level >= 5 -> LevelPalette("EXPLORER", Color(0xFF7E57C2), Color(0xFF42A5F5), Color(0x667E57C2))
    else -> LevelPalette("ROOKIE", Color(0xFFFFD54F), Color(0xFFFFA726), Color(0x66FFD54F))
}

@Composable
private fun StatsLevelHeroCard(
    level: Int,
    xpProgress: Int,
    maxXp: Int,
    totalXp: Int,
    streak: Int,
    completionRate: Float,
    xpProgressFraction: Float,
    palette: LevelPalette
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            palette.glow.copy(alpha = 0.35f),
                            CardBackground.copy(alpha = 0.98f),
                            CardBackground
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEVEL SAAT INI",
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = palette.label,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextColorPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total XP $totalXp • streak $streak hari",
                        fontSize = 12.sp,
                        color = TextColorSecondary
                    )
                }

                Box(
                    modifier = Modifier.size(168.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(palette.glow, Color.Transparent)
                                )
                            )
                    )
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(148.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        strokeWidth = 14.dp
                    )
                    CircularProgressIndicator(
                        progress = { xpProgressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.size(148.dp),
                        color = palette.primary,
                        strokeWidth = 14.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LVL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = palette.secondary
                        )
                        Text(
                            text = level.toString(),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextColorPrimary
                        )
                        Text(
                            text = "$xpProgress / $maxXp XP",
                            fontSize = 11.sp,
                            color = TextColorSecondary
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatsMiniStrip(
                    modifier = Modifier.weight(1f),
                    title = "Progress Level",
                    value = "${(xpProgressFraction * 100).toInt()}%",
                    accent = palette.primary
                )
                StatsMiniStrip(
                    modifier = Modifier.weight(1f),
                    title = "Ritme Mingguan",
                    value = "${(completionRate * 100).toInt()}%",
                    accent = palette.secondary
                )
            }
        }
    }
}

@Composable
private fun StatsMiniStrip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, fontSize = 11.sp, color = TextColorSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
private fun ProfileMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    description: String,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TextColorSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = TextColorPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(description, color = accent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun StatsInsightCard(
    palette: LevelPalette,
    level: Int,
    xpProgress: Int,
    maxXp: Int,
    streak: Int,
    unlockedAchievements: Int,
    totalAchievements: Int,
    weeklySummary: WeeklySummary?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "INSIGHT LEVEL",
                color = palette.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Kamu ada di tier ${palette.label.lowercase().replaceFirstChar { it.uppercase() }}.",
                color = TextColorPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = buildString {
                    append("Naik level berikutnya butuh ")
                    append((maxXp - xpProgress).coerceAtLeast(0))
                    append(" XP lagi. ")
                    if (weeklySummary != null) {
                        append("Minggu ini kamu menyelesaikan ${weeklySummary.completedThisWeek} dari ${weeklySummary.dueThisWeek} misi.")
                    } else {
                        append("Terus tambah misi dan selesaikan secara konsisten untuk membuka insight mingguan.")
                    }
                },
                color = TextColorSecondary,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatsMiniStrip(
                    modifier = Modifier.weight(1f),
                    title = "Tier",
                    value = "Lv $level",
                    accent = palette.primary
                )
                StatsMiniStrip(
                    modifier = Modifier.weight(1f),
                    title = "Prestasi",
                    value = "$unlockedAchievements/$totalAchievements",
                    accent = palette.secondary
                )
                StatsMiniStrip(
                    modifier = Modifier.weight(1f),
                    title = "Streak",
                    value = "$streak h",
                    accent = AccentYellow
                )
            }
        }
    }
}
// Component helpers

@Composable
fun BadgeSlotItem(
    badge: Achievement?,
    size: Dp = 40.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
                if (badge != null) AccentYellow.copy(alpha = 0.1f)
                else Color.Black.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                color = if (badge != null) AccentYellow else Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (badge != null) {
            Image(
                painter = painterResource(id = badge.imageResId),
                contentDescription = badge.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun GameStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    subText: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = if (isClickable) BorderStroke(1.dp, AccentYellow.copy(alpha = 0.3f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(enabled = isClickable) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, color = TextColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(value, color = TextColorPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subText, color = TextColorSecondary.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}

@Composable
fun ActiveBadgesCard(
    equippedBadges: Map<Int, Achievement>,
    onBadgeClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (slot in 0..2) {
                val badge = equippedBadges[slot]
                Column(
                    modifier = Modifier.width(98.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BadgeSlotItem(
                        badge = badge,
                        size = 72.dp
                    ) {
                        onBadgeClick(slot)
                    }
                    Text(
                        text = badge?.title ?: "Kosong",
                        color = if (badge != null) TextColorPrimary else TextColorSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(summary: WeeklySummary) {
    val progress = summary.completionRate.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "weeklyProgress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RINGKASAN MINGGUAN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow
                )
                Text(
                    text = summary.weekLabel,
                    fontSize = 11.sp,
                    color = TextColorSecondary
                )
            }

            Text(
                text = "Progress: ${summary.completedThisWeek}/${summary.dueThisWeek} habit selesai minggu ini",
                fontSize = 12.sp,
                color = TextColorPrimary
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = AccentYellow,
                trackColor = Color(0xFF3A3A3A)
            )

            Text(
                text = if (summary.mostMissedCount > 0) {
                    "Paling bolong: ${summary.mostMissedHabitName} (${summary.mostMissedCount}x terlewat)"
                } else {
                    "Paling bolong: Tidak ada, minggu ini aman"
                },
                fontSize = 12.sp,
                color = TextColorSecondary
            )

            Text(
                text = "Best streak aktivitas: ${summary.bestStreakDays} hari",
                fontSize = 12.sp,
                color = TextColorSecondary
            )

            Text(
                text = if (summary.nextWeekDueCount > 0) {
                    "Target minggu depan: ${summary.nextWeekTarget}/${summary.nextWeekDueCount} habit"
                } else {
                    "Target minggu depan: Belum ada habit terjadwal"
                },
                fontSize = 12.sp,
                color = TextColorPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class HabitCompletionSummary(
    val habitName: String,
    val completedCount: Int,
    val totalXp: Int
)

private data class DailyXpPoint(
    val date: LocalDate,
    val xp: Int
)

private fun buildXpTrend7Days(historyList: List<HabitHistoryItem>): List<DailyXpPoint> {
    val today = LocalDate.now()
    val xpByDate = historyList.groupBy {
        Instant.ofEpochMilli(it.timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }.mapValues { entry -> entry.value.sumOf { it.xpEarned } }

    return (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        DailyXpPoint(date = date, xp = xpByDate[date] ?: 0)
    }
}

@Composable
private fun XpTrendCard(
    data: List<DailyXpPoint>,
    accent: Color
) {
    val maxXp = data.maxOfOrNull { it.xp }?.coerceAtLeast(1) ?: 1
    val formatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("id-ID")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Grafik XP 7 Hari", color = TextColorPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("Lihat ritme progress minggu ini.", color = TextColorSecondary, fontSize = 12.sp)
                }
                Text(
                    text = "+${data.sumOf { it.xp }} XP",
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { point ->
                    val fraction = point.xp.toFloat() / maxXp.toFloat()
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(point.xp.toString(), color = TextColorSecondary, fontSize = 9.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((26 + 76 * fraction).dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(accent, accent.copy(alpha = 0.24f))
                                    )
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            point.date.format(formatter).take(3),
                            color = TextColorSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsDetailDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var animateIn by remember { mutableStateOf(false) }
    var dismissRequested by remember { mutableStateOf(false) }
    val dialogAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "statsDialogAlpha"
    )
    val dialogScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.92f,
        animationSpec = tween(durationMillis = 220),
        label = "statsDialogScale"
    )
    LaunchedEffect(Unit) { animateIn = true }

    val dismissWithAnimation = {
        if (!dismissRequested) {
            dismissRequested = true
            animateIn = false
            scope.launch {
                delay(190)
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = dismissWithAnimation) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .graphicsLayer {
                    alpha = dialogAlpha
                    scaleX = dialogScale
                    scaleY = dialogScale
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                    IconButton(onClick = dismissWithAnimation) {
                        Icon(Icons.Default.Close, null, tint = TextColorSecondary)
                    }
                }
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                content()
            }
        }
    }
}

@Composable
fun StreakDetailDialog(
    currentStreak: Int,
    bestStreak: Int,
    onDismiss: () -> Unit
) {
    val nextTarget = when {
        currentStreak < 3 -> 3
        currentStreak < 7 -> 7
        currentStreak < 30 -> 30
        else -> ((currentStreak / 10) + 1) * 10
    }
    val progress = if (nextTarget > 0) {
        (currentStreak.toFloat() / nextTarget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remaining = (nextTarget - currentStreak).coerceAtLeast(0)

    StatsDetailDialog(
        title = "DETAIL STREAK",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Streak Saat Ini", color = TextColorSecondary, fontSize = 11.sp)
                    Text("$currentStreak hari", color = TextColorPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Best streak", color = TextColorSecondary, fontSize = 12.sp)
                Text("$bestStreak hari", color = AccentYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Progress ke target $nextTarget hari",
                color = TextColorSecondary,
                fontSize = 12.sp
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFFFF5722),
                trackColor = Color(0xFF3A3A3A)
            )
            Text(
                text = if (remaining > 0) {
                    "Lanjut $remaining hari lagi untuk capai milestone berikutnya."
                } else {
                    "Milestone tercapai. Saatnya naik ke target berikutnya."
                },
                color = TextColorPrimary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CompletedMissionsDialog(
    historyList: List<HabitHistoryItem>,
    onDismiss: () -> Unit
) {
    val completionSummary = remember(historyList) {
        historyList
            .groupBy { it.habitName }
            .map { (habitName, entries) ->
                HabitCompletionSummary(
                    habitName = habitName,
                    completedCount = entries.size,
                    totalXp = entries.sumOf { it.xpEarned }
                )
            }
            .sortedWith(
                compareByDescending<HabitCompletionSummary> { it.completedCount }
                    .thenByDescending { it.totalXp }
            )
    }
    val totalXpFromHistory = remember(historyList) { historyList.sumOf { it.xpEarned } }

    StatsDetailDialog(
        title = "DETAIL MISI",
        onDismiss = onDismiss
    ) {
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada misi selesai.", color = TextColorSecondary, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Misi", color = TextColorSecondary, fontSize = 10.sp)
                                Text("${historyList.size}", color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total XP", color = TextColorSecondary, fontSize = 10.sp)
                                Text("+$totalXpFromHistory", color = AccentYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Habit paling sering selesai",
                        fontSize = 12.sp,
                        color = TextColorSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(completionSummary.take(3)) { summary ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF252525), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(summary.habitName, color = TextColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${summary.completedCount}x selesai", color = TextColorSecondary, fontSize = 10.sp)
                        }
                        Text("+${summary.totalXp} XP", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text(
                        text = "Riwayat terbaru",
                        fontSize = 12.sp,
                        color = TextColorSecondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                items(historyList.take(8)) { item ->
                    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID"))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF252525), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, null, tint = PrimaryColor, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.habitName, color = TextColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(dateFormat.format(Date(item.timestamp)), color = TextColorSecondary, fontSize = 9.sp)
                        }
                        Text("+${item.xpEarned}", color = AccentYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginDetailDialog(
    totalLoginDays: Int,
    loginStreakIndex: Int,
    lastLoginDate: String,
    onDismiss: () -> Unit
) {
    val loginCycleDay = (loginStreakIndex + 1).coerceIn(1, 7)
    val cycleProgress = loginCycleDay / 7f

    StatsDetailDialog(
        title = "DETAIL LOGIN",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Login", color = TextColorSecondary, fontSize = 10.sp)
                        Text("$totalLoginDays hari", color = TextColorPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Rantai 7 Hari", color = TextColorSecondary, fontSize = 10.sp)
                        Text("Hari ke-$loginCycleDay", color = AccentYellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Progress hadiah login",
                color = TextColorSecondary,
                fontSize = 12.sp
            )
            LinearProgressIndicator(
                progress = { cycleProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF2196F3),
                trackColor = Color(0xFF3A3A3A)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Login terakhir", color = TextColorSecondary, fontSize = 12.sp)
                Text(
                    text = formatIsoDateForUi(lastLoginDate),
                    color = TextColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun HistoryLogDialog(
    historyList: List<HabitHistoryItem>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var animateIn by remember { mutableStateOf(false) }
    var dismissRequested by remember { mutableStateOf(false) }
    val dialogAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "historyDialogAlpha"
    )
    val dialogScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.92f,
        animationSpec = tween(durationMillis = 220),
        label = "historyDialogScale"
    )
    LaunchedEffect(Unit) { animateIn = true }

    val dismissWithAnimation = {
        if (!dismissRequested) {
            dismissRequested = true
            animateIn = false
            scope.launch {
                delay(190)
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = dismissWithAnimation) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .graphicsLayer {
                    alpha = dialogAlpha
                    scaleX = dialogScale
                    scaleY = dialogScale
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("JURNAL PETUALANG", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
                    IconButton(onClick = dismissWithAnimation) { Icon(Icons.Default.Close, null, tint = TextColorSecondary) }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Jurnal masih kosong...", color = TextColorSecondary, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(historyList) { item ->
                            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID"))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF252525), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.habitName, color = TextColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(dateFormat.format(Date(item.timestamp)), color = TextColorSecondary, fontSize = 10.sp)
                                }
                                Text("+${item.xpEarned} XP", color = AccentYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatIsoDateForUi(dateValue: String): String {
    if (dateValue.isBlank()) return "Belum tercatat"
    return runCatching {
        LocalDate.parse(dateValue).format(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        )
    }.getOrElse { dateValue }
}

