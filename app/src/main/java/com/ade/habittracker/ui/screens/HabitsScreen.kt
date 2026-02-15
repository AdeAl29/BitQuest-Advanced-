package com.ade.habittracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.R
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitScheduleType
import com.ade.habittracker.model.completedCountInWeek
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.ui.components.ChibiMessage
import com.ade.habittracker.ui.components.DraggableChibiWithBubble
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.components.HabitItem
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import java.time.LocalDate
import java.time.LocalTime

enum class HabitTab(val title: String) {
    ALL("Semua"),
    PENDING("Belum"),
    COMPLETED("Selesai")
}

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    userName: String = "Petualang",
    chibiRes: Int = R.drawable.chibi_helper,
    isChibiEnabled: Boolean = true,
    isChibiVoiceEnabled: Boolean = true,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onReminderToggle: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    var selectedTab by remember { mutableStateOf(HabitTab.ALL) }
    var showGuide by remember { mutableStateOf(false) }
    var chibiNotification by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val dueHabits = remember(habits, today) { habits.filter { it.isDueOn(today) } }

    val totalHabits = dueHabits.size
    val completedHabits = dueHabits.count { it.isCompleted }
    val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    val filteredHabits = remember(habits, selectedTab, today) {
        when (selectedTab) {
            HabitTab.ALL -> habits.sortedWith(
                compareBy<Habit>({ !it.isDueOn(today) }, { it.isCompleted })
            )
            HabitTab.PENDING -> habits.filter { it.isDueOn(today) && !it.isCompleted }
            HabitTab.COMPLETED -> habits.filter { it.isCompleted }
        }
    }

    val currentHour = remember { LocalTime.now().hour }
    val chibiMessages = remember(totalHabits, completedHabits, selectedTab, currentHour) {
        val base = mutableListOf(
            ChibiMessage("Ayo satu misi lagi, $userName!", R.raw.chibi_ayo_satu_misi),
            ChibiMessage("Jangan bolos ya!", R.raw.chibi_jangan_bolos),
            ChibiMessage("Konsisten itu keren banget!", R.raw.chibi_konsisten_keren)
        )

        when {
            totalHabits == 0 -> base += ChibiMessage("Belum ada misi. Tambah dulu yuk.", R.raw.chibi_aku_liatin)
            completedHabits == totalHabits && totalHabits > 0 -> base += ChibiMessage("Semua misi hari ini beres. Keren!", R.raw.chibi_misi_selesai)
            progress >= 0.75f -> base += ChibiMessage("Sedikit lagi, hampir selesai semua!", R.raw.chibi_sedikit_lagi)
        }

        if (currentHour >= 20 && completedHabits < totalHabits) {
            base += ChibiMessage("Malam ini jangan sampai ada misi bolong.", R.raw.chibi_jangan_bolos)
        }

        if (selectedTab == HabitTab.PENDING && completedHabits == 0 && totalHabits > 0) {
            base += ChibiMessage("Mulai dari satu misi paling ringan dulu.", R.raw.chibi_ayo_satu_misi)
        }

        base.distinctBy { it.text }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FallingSnowEffect(modifier = Modifier.fillMaxSize().zIndex(0f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MISI HARIAN",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextColorPrimary
                    )
                    Text(
                        text = if (totalHabits > 0) {
                            "$completedHabits dari $totalHabits misi terjadwal hari ini"
                        } else {
                            "Tidak ada misi terjadwal hari ini"
                        },
                        fontSize = 12.sp,
                        color = TextColorSecondary
                    )
                }
                IconButton(onClick = { showGuide = true }) {
                    Icon(Icons.AutoMirrored.Filled.Help, "Panduan", tint = AccentYellow)
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progress Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${(progress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = AccentYellow,
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = tab }
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.White else TextColorSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (filteredHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TextColorSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = when (selectedTab) {
                                HabitTab.ALL -> "Belum ada misi. Tekan + untuk tambah."
                                HabitTab.PENDING -> "Semua misi terjadwal selesai."
                                HabitTab.COMPLETED -> "Belum ada misi yang selesai."
                            },
                            color = TextColorSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredHabits, key = { it.id }) { habit ->
                        val isDueToday = habit.isDueOn(today)
                        val extraInfo = when {
                            !isDueToday -> "Tidak terjadwal hari ini"
                            habit.scheduleType == HabitScheduleType.TIMES_PER_WEEK -> {
                                "${habit.completedCountInWeek(today)}/${habit.targetPerWeek.coerceIn(1, 7)} selesai minggu ini"
                            }
                            else -> null
                        }

                        HabitItem(
                            habit = habit,
                            isDueToday = isDueToday,
                            extraScheduleInfo = extraInfo,
                            onCheckedChanged = { checked ->
                                if (!isDueToday) return@HabitItem
                                if (!habit.isCompleted && checked) {
                                    chibiNotification = "Mantap! +${habit.weight} XP"
                                }
                                onHabitCheckedChanged(habit, checked)
                            },
                            onReminderToggle = { enabled ->
                                onReminderToggle(habit, enabled)
                            },
                            onEditClick = { onEditClick(habit) },
                            onDeleteClick = { onDeleteClick(habit) }
                        )
                    }
                }
            }
        }

        if (isChibiEnabled) {
            DraggableChibiWithBubble(
                chibiRes = chibiRes,
                messages = chibiMessages,
                modifier = Modifier.fillMaxSize().zIndex(50f),
                voiceEnabled = isChibiVoiceEnabled,
                externalMessage = chibiNotification,
                onExternalMessageDismiss = { chibiNotification = null }
            )
        }

        if (showGuide) {
            AlertDialog(
                onDismissRequest = { showGuide = false },
                confirmButton = {
                    TextButton(onClick = { showGuide = false }) {
                        Text("Siap!", color = AccentYellow)
                    }
                },
                title = { Text("Panduan", color = TextColorPrimary) },
                text = {
                    Text(
                        "Geser kanan untuk edit, geser kiri untuk hapus. Misi yang tidak terjadwal hari ini akan otomatis nonaktif.",
                        color = TextColorSecondary
                    )
                },
                containerColor = CardBackground
            )
        }
    }
}
