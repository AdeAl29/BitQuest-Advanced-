package com.ade.habittracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.R
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.components.AchievementDescriptionDialog
import com.ade.habittracker.ui.components.ChibiMessage
import com.ade.habittracker.ui.components.DraggableChibiWithBubble
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.theme.*
import java.time.LocalTime

private enum class AchievementFilter(val label: String) {
    ALL("Semua"),
    UNLOCKED("Terbuka"),
    NEAR("Hampir"),
    LOCKED("Terkunci")
}

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    userName: String = "Petualang",
    chibiRes: Int = R.drawable.chibi_helper,
    isChibiEnabled: Boolean = true,
    isChibiVoiceEnabled: Boolean = true
) {
    var achievementToShowDesc by remember { mutableStateOf<Achievement?>(null) }
    var selectedFilter by remember { mutableStateOf(AchievementFilter.ALL) }

    // Logic Sorting: Yang terbuka di atas, lalu urutkan berdasarkan progress
    val sortedAchievements = remember(achievements, selectedFilter) {
        achievements.filter { achievement ->
            val progressRatio = if (achievement.target > 0) achievement.progress.toFloat() / achievement.target.toFloat() else 0f
            when (selectedFilter) {
                AchievementFilter.ALL -> true
                AchievementFilter.UNLOCKED -> achievement.isUnlocked
                AchievementFilter.NEAR -> !achievement.isUnlocked && progressRatio >= 0.6f
                AchievementFilter.LOCKED -> !achievement.isUnlocked
            }
        }.sortedWith(
            compareByDescending<Achievement> { it.isUnlocked }
                .thenByDescending { if (it.target > 0) it.progress.toFloat() / it.target else 0f }
        )
    }

    // Hitung Statistik
    val totalCount = achievements.size
    val unlockedCount = achievements.count { it.isUnlocked }
    val progressPercentage = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
    val currentHour = remember { LocalTime.now().hour }
    val chibiMessages = remember(unlockedCount, totalCount, progressPercentage, currentHour) {
        val base = mutableListOf(
            ChibiMessage("Badge baru tinggal dikit lagi, $userName!", R.raw.chibi_ayo_satu_misi),
            ChibiMessage("Cek progres prestasimu!", R.raw.chibi_konsisten_keren),
            ChibiMessage("Jangan berhenti sekarang.", R.raw.chibi_jangan_bolos)
        )

        when {
            unlockedCount == 0 -> base += ChibiMessage("Satu pencapaian pertama dulu, gas!", R.raw.chibi_ayo_satu_misi)
            progressPercentage >= 0.8f -> base += ChibiMessage("Koleksi badge kamu hampir penuh!", R.raw.chibi_misi_selesai)
            progressPercentage >= 0.5f -> base += ChibiMessage("Setengah jalan, lanjut terus.", R.raw.chibi_sedikit_lagi)
        }

        if (currentHour >= 20 && unlockedCount < totalCount) {
            base += ChibiMessage("Malam ini bisa dapet satu badge lagi.", R.raw.chibi_jangan_bolos)
        }

        base.distinctBy { it.text }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Background Effect
        FallingSnowEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .alpha(0.6f) // Sedikit transparan agar konten lebih terbaca
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .zIndex(1f)
        ) {

            // 2. Judul Halaman
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PENCAPAIAN SAYA",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = AccentYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lihat badge yang sudah terbuka dan progres yang sedang berjalan.",
                    color = TextColorSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 3. Header Statistik (Span selebar 2 kolom)
                item(span = { GridItemSpan(2) }) {
                    AchievementStatsCard(
                        unlockedCount = unlockedCount,
                        totalCount = totalCount,
                        progress = progressPercentage
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AchievementFilter.values().forEach { filter ->
                            val selected = selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                // 4. Item List
                items(sortedAchievements, key = { it.id }) { achievement ->
                    AchievementCardItem(
                        achievement = achievement,
                        onClick = { achievementToShowDesc = achievement }
                    )
                }
            }
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

    // Dialog Detail
    achievementToShowDesc?.let {
        AchievementDescriptionDialog(
            achievement = it,
            onDismiss = { achievementToShowDesc = null }
        )
    }
}

// --- COMPONENT: Header Statistik ---
@Composable
fun AchievementStatsCard(
    unlockedCount: Int,
    totalCount: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500),
        label = "headerProgress"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(66.dp),
                        color = Color.Gray.copy(alpha = 0.2f),
                        strokeWidth = 7.dp,
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(66.dp),
                        color = AccentYellow,
                        strokeWidth = 7.dp,
                    )
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Total Koleksi",
                        fontSize = 14.sp,
                        color = TextColorSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$unlockedCount dari $totalCount Terbuka",
                        fontSize = 18.sp,
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% Selesai",
                        fontSize = 12.sp,
                        color = AccentYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AchievementMiniPill(
                    modifier = Modifier.weight(1f),
                    title = "Terbuka",
                    value = unlockedCount.toString(),
                    accent = AccentYellow
                )
                AchievementMiniPill(
                    modifier = Modifier.weight(1f),
                    title = "Sisa",
                    value = (totalCount - unlockedCount).coerceAtLeast(0).toString(),
                    accent = PrimaryColor
                )
            }
        }
    }
}

@Composable
private fun AchievementMiniPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.18f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextColorSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
// --- COMPONENT: Kartu Achievement Individual ---
@Composable
fun AchievementCardItem(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val isUnlocked = achievement.isUnlocked
    val isNearUnlock = !isUnlocked && achievement.target > 0 &&
        achievement.progress.toFloat() / achievement.target.toFloat() >= 0.6f

    // Setup Warna & Animasi
    val borderColor by animateColorAsState(
        targetValue = if (isUnlocked) AccentYellow else Color.Transparent,
        label = "borderColor"
    )

    // Efek Grayscale (Hitam Putih) jika terkunci
    val colorMatrix = remember(isUnlocked) {
        if (isUnlocked) null else ColorMatrix().apply { setToSaturation(0f) }
    }
    val colorFilter = if (colorMatrix != null) ColorFilter.colorMatrix(colorMatrix) else null

    // Animasi Progress Bar
    val targetProgress = if (achievement.target > 0)
        achievement.progress.toFloat() / achievement.target.toFloat()
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "itemProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, when {
            isUnlocked -> borderColor
            isNearUnlock -> AccentYellow.copy(alpha = 0.36f)
            else -> Color.White.copy(alpha = 0.06f)
        }),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- BAGIAN GAMBAR ---
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Gambar Utama
                Image(
                    painter = painterResource(id = achievement.imageResId),
                    contentDescription = achievement.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter, // Aplikasikan filter BW
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isUnlocked) 1f else 0.5f) // Redupkan jika terkunci
                )

                // Overlay Gembok jika Terkunci
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- BAGIAN TEKS ---
            Text(
                text = achievement.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUnlocked) TextColorPrimary else TextColorSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = achievement.description,
                fontSize = 11.sp,
                color = TextColorSecondary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            // --- BAGIAN PROGRESS / STATUS ---
            if (achievement.target > 1) {
                // Jika ada target angka (misal: 0/100)
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (isUnlocked) AccentYellow else Color.Gray,
                        trackColor = Color(0xFF404040)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${achievement.progress}/${achievement.target}",
                        fontSize = 10.sp,
                        color = if (isNearUnlock) AccentYellow else TextColorSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                if (isNearUnlock) {
                    Text(
                        text = "HAMPIR TERBUKA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentYellow,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            } else {
                // Jika hanya status (misal: Login Sekali)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isUnlocked) AccentYellow.copy(alpha = 0.15f) else Color(0xFF2C2C2E),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "TERBUKA" else "TERKUNCI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUnlocked) AccentYellow else TextColorSecondary
                    )
                }
            }
        }
    }
}


