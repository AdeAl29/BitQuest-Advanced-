package com.ade.habittracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.ade.habittracker.model.isCompletedOn
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    activeMissionCardSkinId: String = "mission_card_default",
    activeChecklistEffectId: String = "checklist_effect_default",
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onReminderToggle: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    var selectedTab by remember { mutableStateOf(HabitTab.ALL) }
    var showGuide by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    var chibiNotification by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val activeDate = selectedDate
    val localeId = remember { Locale.forLanguageTag("id-ID") }
    val shortDateFormatter = remember(localeId) { DateTimeFormatter.ofPattern("dd MMM", localeId) }
    val fullDateFormatter = remember(localeId) { DateTimeFormatter.ofPattern("EEEE, dd MMM", localeId) }
    val dueHabits = remember(habits, activeDate) { habits.filter { it.isDueOn(activeDate) } }

    val totalHabits = dueHabits.size
    val completedHabits = dueHabits.count { it.isCompletedOn(activeDate) || (activeDate == today && it.isCompleted) }
    val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    val filteredHabits = remember(habits, selectedTab, activeDate, today) {
        when (selectedTab) {
            HabitTab.ALL -> habits.sortedWith(
                compareBy<Habit>({ !it.isDueOn(activeDate) }, { !(it.isCompletedOn(activeDate) || (activeDate == today && it.isCompleted)) })
            )
            HabitTab.PENDING -> habits.filter { it.isDueOn(activeDate) && !(it.isCompletedOn(activeDate) || (activeDate == today && it.isCompleted)) }
            HabitTab.COMPLETED -> habits.filter { it.isCompletedOn(activeDate) || (activeDate == today && it.isCompleted) }
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentYellow
                    )
                    Text(
                        text = activeDate.format(fullDateFormatter).replaceFirstChar { it.titlecase(localeId) },
                        fontSize = 11.sp,
                        color = AccentYellow.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (totalHabits > 0) {
                            if (activeDate == today) {
                                "$completedHabits dari $totalHabits misi terjadwal hari ini"
                            } else {
                                "$completedHabits dari $totalHabits misi untuk ${activeDate.format(shortDateFormatter)}"
                            }
                        } else {
                            if (activeDate == today) "Tidak ada misi terjadwal hari ini" else "Tidak ada misi untuk ${activeDate.format(shortDateFormatter)}"
                        },
                        fontSize = 12.sp,
                        color = TextColorSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = {
                        displayedMonth = YearMonth.from(activeDate)
                        showCalendarSheet = true
                    }) {
                        Icon(Icons.Default.CalendarMonth, "Kalender", tint = AccentYellow)
                    }
                    IconButton(onClick = { showGuide = true }) {
                        Icon(Icons.AutoMirrored.Filled.Help, "Panduan", tint = AccentYellow)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progress Hari Ini", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${(progress * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = AccentYellow,
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HabitStatChip(
                    modifier = Modifier.weight(1f),
                    title = "Belum",
                    value = (totalHabits - completedHabits).coerceAtLeast(0).toString(),
                    accent = Color(0xFFFFB74D)
                )
                HabitStatChip(
                    modifier = Modifier.weight(1f),
                    title = "Selesai",
                    value = completedHabits.toString(),
                    accent = Color(0xFF66BB6A)
                )
                HabitStatChip(
                    modifier = Modifier.weight(1f),
                    title = "Kalender",
                    value = activeDate.dayOfMonth.toString(),
                    accent = AccentYellow,
                    onClick = {
                        displayedMonth = YearMonth.from(activeDate)
                        showCalendarSheet = true
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            if (activeDate != today) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Menampilkan ${activeDate.format(shortDateFormatter)}",
                                color = TextColorPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daftar misi sedang difilter mengikuti tanggal yang dipilih dari kalender.",
                                color = TextColorSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Hari Ini",
                            color = AccentYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { selectedDate = today }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

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
                        val isDueForSelectedDate = habit.isDueOn(activeDate)
                        val isCompletedForSelectedDate = habit.isCompletedOn(activeDate) || (activeDate == today && habit.isCompleted)
                        val extraInfo = when {
                            activeDate != today && isDueForSelectedDate -> "Terjadwal untuk ${activeDate.format(shortDateFormatter)}"
                            !isDueForSelectedDate -> if (activeDate == today) "Tidak terjadwal hari ini" else "Tidak terjadwal di ${activeDate.format(shortDateFormatter)}"
                            habit.scheduleType == HabitScheduleType.TIMES_PER_WEEK -> {
                                "${habit.completedCountInWeek(activeDate)}/${habit.targetPerWeek.coerceIn(1, 7)} selesai minggu ini"
                            }
                            else -> null
                        }

                        HabitItem(
                            habit = habit.copy(isCompleted = isCompletedForSelectedDate),
                            isDueToday = isDueForSelectedDate,
                            extraScheduleInfo = extraInfo,
                            missionCardSkinId = activeMissionCardSkinId,
                            checklistEffectId = activeChecklistEffectId,
                            onCheckedChanged = { checked ->
                                if (!isDueForSelectedDate || activeDate != today) return@HabitItem
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

        if (showCalendarSheet) {
            MissionCalendarSheet(
                habits = habits,
                selectedDate = selectedDate,
                displayedMonth = displayedMonth,
                onSelectedDateChange = {
                    selectedDate = it
                    displayedMonth = YearMonth.from(it)
                    showCalendarSheet = false
                },
                onDisplayedMonthChange = { displayedMonth = it },
                onDismiss = { showCalendarSheet = false }
            )
        }
    }
}

@Composable
private fun HabitStatChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, color = TextColorSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = TextColorPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionCalendarSheet(
    habits: List<Habit>,
    selectedDate: LocalDate,
    displayedMonth: YearMonth,
    onSelectedDateChange: (LocalDate) -> Unit,
    onDisplayedMonthChange: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val localeId = remember { Locale.forLanguageTag("id-ID") }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", localeId) }
    val selectedDateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM", localeId) }
    val daysInMonth = remember(displayedMonth) { displayedMonth.lengthOfMonth() }
    val firstDayOffset = remember(displayedMonth) { displayedMonth.atDay(1).dayOfWeek.value - 1 }
    val calendarDays = remember(displayedMonth) {
        buildList {
            repeat(firstDayOffset) { add(null) }
            for (day in 1..daysInMonth) {
                add(displayedMonth.atDay(day))
            }
            while (size % 7 != 0) add(null)
        }
    }
    val dueForSelectedDate = remember(habits, selectedDate) { habits.filter { it.isDueOn(selectedDate) } }
    val completedForSelectedDate = remember(habits, selectedDate) { habits.filter { it.isCompletedOn(selectedDate) } }
    val xpForSelectedDate = remember(completedForSelectedDate) { completedForSelectedDate.sumOf { it.weight } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDisplayedMonthChange(displayedMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya", tint = AccentYellow)
                }
                Text(
                    text = displayedMonth.format(monthFormatter).replaceFirstChar { it.titlecase(localeId) },
                    color = TextColorPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                IconButton(onClick = { onDisplayedMonthChange(displayedMonth.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya", tint = AccentYellow)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { day ->
                    Text(
                        text = day,
                        color = TextColorSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                calendarDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        week.forEach { date ->
                            CalendarDayCell(
                                modifier = Modifier.weight(1f),
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                dueCount = date?.let { day -> habits.count { it.isDueOn(day) } } ?: 0,
                                completedCount = date?.let { day -> habits.count { it.isCompletedOn(day) } } ?: 0,
                                onClick = { if (date != null) onSelectedDateChange(date) }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedDate.format(selectedDateFormatter).replaceFirstChar { it.titlecase(localeId) },
                            color = TextColorPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (dueForSelectedDate.isEmpty()) {
                                "Belum ada misi terjadwal di tanggal ini."
                            } else {
                                "${completedForSelectedDate.size} dari ${dueForSelectedDate.size} misi selesai • $xpForSelectedDate XP"
                            },
                            color = TextColorSecondary,
                            fontSize = 12.sp
                        )
                        if (completedForSelectedDate.isNotEmpty()) {
                            Text(
                                text = completedForSelectedDate.take(2).joinToString("  •  ") { it.name },
                                color = TextColorSecondary,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniCalendarStat(value = dueForSelectedDate.size.toString(), label = "Misi", accent = AccentYellow)
                        MiniCalendarStat(value = completedForSelectedDate.size.toString(), label = "Done", accent = Color(0xFF66BB6A))
                        MiniCalendarStat(value = xpForSelectedDate.toString(), label = "XP", accent = Color(0xFF29B6F6))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MiniCalendarStat(
    value: String,
    label: String,
    accent: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = TextColorPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    dueCount: Int,
    completedCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .then(if (date != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = when {
                date == null -> Color.Transparent
                isSelected -> AccentYellow.copy(alpha = 0.22f)
                isToday -> AccentYellow.copy(alpha = 0.12f)
                else -> Color.White.copy(alpha = 0.03f)
            }
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            when {
                date == null -> Color.Transparent
                isSelected -> AccentYellow.copy(alpha = 0.75f)
                isToday -> AccentYellow.copy(alpha = 0.32f)
                else -> Color.Transparent
            }
        )
    ) {
        if (date != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = when {
                        isSelected -> AccentYellow
                        isToday -> AccentYellow.copy(alpha = 0.95f)
                        else -> TextColorPrimary
                    },
                    fontSize = 13.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (dueCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentYellow.copy(alpha = 0.9f))
                        )
                    }
                    if (completedCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF66BB6A))
                        )
                    }
                }
            }
        }
    }
}
