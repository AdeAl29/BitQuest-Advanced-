package com.ade.habittracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.ui.viewmodel.FocusSummary
import com.ade.habittracker.ui.viewmodel.FocusTimerUiState

@Composable
fun FocusScreen(
    userName: String,
    focusState: FocusTimerUiState,
    focusSummary: FocusSummary,
    rewardXp: Int,
    rewardCoins: Int,
    habits: List<Habit> = emptyList(),
    linkedHabitId: Int? = null,
    onBack: () -> Unit,
    onSelectLinkedHabit: (Int?) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onUpdateDailyTarget: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onDismissCompletion: () -> Unit
) {
    val totalSeconds = (focusState.selectedDurationMinutes * 60).coerceAtLeast(1)
    val targetProgress = 1f - (focusState.remainingSeconds.toFloat() / totalSeconds.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "focusProgress"
    )
    val progressToTarget = if (focusSummary.dailyTargetSessions > 0) {
        focusSummary.sessionsCompletedToday.toFloat() / focusSummary.dailyTargetSessions.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)
    val remainingMinutes = focusState.remainingSeconds / 60
    val remainingSeconds = focusState.remainingSeconds % 60
    val timerText = String.format("%02d:%02d", remainingMinutes, remainingSeconds)
    var showCompletionDialog by remember { mutableStateOf(false) }
    val focusableHabits = remember(habits) {
        habits.filter { it.isDueOn(java.time.LocalDate.now()) && !it.isCompleted }.take(5)
    }
    val linkedHabit = remember(focusableHabits, linkedHabitId) {
        focusableHabits.firstOrNull { it.id == linkedHabitId }
    }

    LaunchedEffect(focusState.completionToken) {
        if (focusState.completionToken > 0L) {
            showCompletionDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        CardBackground.copy(alpha = 0.98f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextColorPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "MODE FOKUS",
                                color = AccentYellow,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Pomodoro untuk bantu $userName kerja lebih tenang.",
                                color = TextColorSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            item {
                FocusSectionCard(
                    title = "Fokus ke Misi",
                    subtitle = linkedHabit?.let { "Sesi ini dikaitkan ke: ${it.name}" }
                        ?: "Pilih satu misi supaya timer punya target yang jelas."
                ) {
                    if (focusableHabits.isEmpty()) {
                        Text("Tidak ada misi aktif untuk hari ini.", color = TextColorSecondary, fontSize = 12.sp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = linkedHabitId == null,
                                onClick = { onSelectLinkedHabit(null) },
                                label = { Text("Tanpa link") }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.height((focusableHabits.size.coerceAtMost(3) * 48).dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(focusableHabits, key = { it.id }) { habit ->
                                FilterChip(
                                    selected = linkedHabitId == habit.id,
                                    onClick = { onSelectLinkedHabit(habit.id) },
                                    label = {
                                        Text(
                                            text = "${habit.name} • +${habit.weight} XP",
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
                    border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.24f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryColor.copy(alpha = 0.20f),
                                        CardBackground,
                                        CardBackground
                                    )
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (focusState.isRunning) "SEDANG FOKUS" else "SIAP MULAI",
                            color = PrimaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier.size(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(220.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                strokeWidth = 16.dp
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(220.dp),
                                color = PrimaryColor,
                                strokeWidth = 16.dp,
                                trackColor = Color.Transparent,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = timerText,
                                    color = TextColorPrimary,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${focusState.selectedDurationMinutes} menit per sesi",
                                    color = TextColorSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onToggleTimer,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Icon(
                                    imageVector = if (focusState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (focusState.isRunning) "Pause" else "Mulai")
                            }
                            Button(
                                onClick = onResetTimer,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardBackground,
                                    contentColor = TextColorPrimary
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            item {
                FocusSectionCard(
                    title = "Preset Durasi",
                    subtitle = "Pilih ritme fokus yang paling cocok untukmu."
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 50, 90).forEach { minutes ->
                            FilterChip(
                                selected = focusState.selectedDurationMinutes == minutes,
                                onClick = { onSelectDuration(minutes) },
                                label = { Text("$minutes m") }
                            )
                        }
                    }
                }
            }

            item {
                FocusSectionCard(
                    title = "Target Harian",
                    subtitle = "Biar sesi fokusmu terasa terukur, tapi tidak terlalu berat."
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 4, 6, 8).forEach { target ->
                                FilterChip(
                                    selected = focusSummary.dailyTargetSessions == target,
                                    onClick = { onUpdateDailyTarget(target) },
                                    label = { Text("$target sesi") }
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Progress target", color = TextColorSecondary, fontSize = 12.sp)
                                Text(
                                    "${focusSummary.sessionsCompletedToday}/${focusSummary.dailyTargetSessions} sesi",
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progressToTarget },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = PrimaryColor,
                                trackColor = Color(0xFF303030)
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FocusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Hari Ini",
                        value = focusSummary.sessionsCompletedToday.toString(),
                        subtitle = "Sesi fokus",
                        accent = PrimaryColor
                    )
                    FocusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Menit",
                        value = focusSummary.totalMinutes.toString(),
                        subtitle = "Akumulasi",
                        accent = Color(0xFF64B5F6)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FocusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Streak Fokus",
                        value = "${focusSummary.currentStreakDays}",
                        subtitle = "Hari beruntun",
                        accent = AccentYellow
                    )
                    FocusStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Best Streak",
                        value = "${focusSummary.bestStreakDays}",
                        subtitle = "Rekor fokus",
                        accent = Color(0xFFFF8A65)
                    )
                }
            }

            item {
                FocusSectionCard(
                    title = "Reward Sesi",
                    subtitle = "Setiap fokus selesai akan langsung masuk ke progres akunmu."
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RewardPill(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "+$rewardXp XP",
                            accent = AccentYellow
                        )
                        RewardPill(
                            icon = Icons.Default.MonetizationOn,
                            label = "+$rewardCoins Koin",
                            accent = Color(0xFF81C784)
                        )
                    }
                }
            }

            item {
                FocusSectionCard(
                    title = "Tips Fokus",
                    subtitle = "Polish kecil yang bikin sesi pomodoro lebih kepakai beneran."
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FocusTip(text = "Taruh HP di mode senyap selama timer berjalan.")
                        FocusTip(text = "Pilih satu tugas saja untuk satu sesi.")
                        FocusTip(text = "Kalau sesi 25 menit terasa ringan, naikkan ke 50 menit.")
                    }
                }
            }
        }

        if (showCompletionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCompletionDialog = false
                    onDismissCompletion()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCompletionDialog = false
                            onDismissCompletion()
                        }
                    ) {
                        Text("Lanjut", color = AccentYellow)
                    }
                },
                title = {
                    Text(
                        text = "Sesi Fokus Selesai",
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = buildString {
                            append("${focusState.lastCompletedDurationMinutes} menit fokus selesai. Kamu dapat +${focusState.lastEarnedXp} XP dan +${focusState.lastEarnedCoins} koin.")
                            linkedHabit?.let { append("\n\nMisi terkait: ${it.name}. Kalau tugasnya sudah benar-benar beres, centang dari halaman Misi ya.") }
                        },
                        color = TextColorSecondary
                    )
                },
                containerColor = CardBackground
            )
        }
    }
}

@Composable
private fun FocusSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = TextColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextColorSecondary, fontSize = 12.sp)
            }
            content()
        }
    }
}

@Composable
private fun FocusStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, color = TextColorSecondary, fontSize = 12.sp)
            Text(text = value, color = TextColorPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = subtitle, color = accent, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RewardPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FocusTip(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(AccentYellow)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = TextColorSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
    }
}
