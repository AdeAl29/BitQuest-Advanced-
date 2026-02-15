package com.ade.habittracker.ui.components.sheets

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitScheduleType
import com.ade.habittracker.model.HabitTemplate
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import java.util.Locale

private data class BobotOption(val label: String, val xp: Int)

private val bobotOptions = listOf(
    BobotOption("Ringan (1x)", 10),
    BobotOption("Sedang (3x)", 30),
    BobotOption("Berat (5x)", 50)
)

private val scheduleOptions = listOf(
    HabitScheduleType.DAILY to "Setiap Hari",
    HabitScheduleType.WEEKDAYS to "Senin-Jumat",
    HabitScheduleType.TIMES_PER_WEEK to "x / Minggu",
    HabitScheduleType.CUSTOM_DAYS to "Custom Hari"
)

private val customDayOptions = listOf(
    1 to "Sen",
    2 to "Sel",
    3 to "Rab",
    4 to "Kam",
    5 to "Jum",
    6 to "Sab",
    7 to "Min"
)

@Composable
fun AddOptionsSheet(
    onManualAddClick: () -> Unit,
    onTemplateAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tambah Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onTemplateAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Pilih dari Template", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onManualAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Buat Misi Sendiri (Manual)", color = PrimaryColor, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TemplateHabitSheet(
    templates: List<Pair<String, List<HabitTemplate>>>,
    onTemplateClick: (HabitTemplate) -> Unit
) {
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Pilih Misi dari Template",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        templates.forEach { (category, habitList) ->
            item(key = "template_category_$category") {
                TemplateCategoryAccordion(
                    category = category,
                    habitList = habitList,
                    isExpanded = expandedCategory == category,
                    onToggle = {
                        expandedCategory = if (expandedCategory == category) null else category
                    },
                    onTemplateClick = onTemplateClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TemplateCategoryAccordion(
    category: String,
    habitList: List<HabitTemplate>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onTemplateClick: (HabitTemplate) -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "templateArrowRotation"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .animateContentSize()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.uppercase(),
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isExpanded) "Tap untuk tutup" else "Tap untuk lihat daftar",
                        color = TextColorSecondary,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Tutup kategori" else "Buka kategori",
                    tint = AccentYellow,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 260)) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 220)) + fadeOut(animationSpec = tween(durationMillis = 160))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    habitList.forEach { template ->
                        HabitTemplateItem(
                            template = template,
                            onClick = { onTemplateClick(template) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTemplateItem(
    template: HabitTemplate,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, color = TextColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(template.schedule, color = TextColorSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "+${template.weight} XP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddHabitSheet(
    habitToEdit: Habit?,
    defaultReminderTime: String,
    onConfirm: (
        name: String,
        weight: Int,
        scheduleType: HabitScheduleType,
        customDays: List<Int>,
        targetPerWeek: Int,
        reminderEnabled: Boolean,
        reminderTime: String?
    ) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var scheduleType by remember { mutableStateOf(HabitScheduleType.DAILY) }
    var selectedCustomDays by remember { mutableStateOf(listOf<Int>()) }
    var timesPerWeek by remember { mutableIntStateOf(3) }

    var selectedBobot by remember { mutableStateOf(bobotOptions[0]) }
    var isWeightDropdownExpanded by remember { mutableStateOf(false) }

    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf(defaultReminderTime) }

    LaunchedEffect(habitToEdit, defaultReminderTime) {
        if (habitToEdit != null) {
            name = habitToEdit.name
            selectedBobot = bobotOptions.find { it.xp == habitToEdit.weight } ?: bobotOptions[0]
            scheduleType = habitToEdit.scheduleType
            selectedCustomDays = habitToEdit.customDays
            timesPerWeek = habitToEdit.targetPerWeek.coerceIn(1, 7)
            reminderEnabled = habitToEdit.reminderEnabled
            reminderTime = habitToEdit.reminderTime ?: defaultReminderTime
        } else {
            name = ""
            selectedBobot = bobotOptions[0]
            scheduleType = HabitScheduleType.DAILY
            selectedCustomDays = emptyList()
            timesPerWeek = 3
            reminderEnabled = true
            reminderTime = defaultReminderTime
        }
    }

    val validCustomDays = selectedCustomDays.distinct().sorted()
    val isScheduleValid = scheduleType != HabitScheduleType.CUSTOM_DAYS || validCustomDays.isNotEmpty()
    val isFormValid = name.isNotBlank() && isScheduleValid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (habitToEdit == null) "Tambah Misi Manual" else "Edit Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Misi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors()
        )

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "${selectedBobot.label} (+${selectedBobot.xp} XP)",
                onValueChange = { },
                label = { Text("Pilih Bobot") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih Bobot",
                        modifier = Modifier.clickable { isWeightDropdownExpanded = true }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isWeightDropdownExpanded = true },
                colors = textFieldColors()
            )

            DropdownMenu(
                expanded = isWeightDropdownExpanded,
                onDismissRequest = { isWeightDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CardBackground)
            ) {
                bobotOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${option.label} (+${option.xp} XP)",
                                color = if (option.xp == selectedBobot.xp) AccentYellow else TextColorPrimary
                            )
                        },
                        onClick = {
                            selectedBobot = option
                            isWeightDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Jadwal Misi",
            color = TextColorSecondary,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            scheduleOptions.forEach { (type, label) ->
                FilterChip(
                    selected = scheduleType == type,
                    onClick = { scheduleType = type },
                    label = { Text(label) }
                )
            }
        }

        if (scheduleType == HabitScheduleType.TIMES_PER_WEEK) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target per minggu", color = TextColorSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { timesPerWeek = (timesPerWeek - 1).coerceAtLeast(1) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("-") }
                    Text(
                        text = "$timesPerWeek x",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { timesPerWeek = (timesPerWeek + 1).coerceAtMost(7) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("+") }
                }
            }
        }

        if (scheduleType == HabitScheduleType.CUSTOM_DAYS) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pilih hari aktif",
                color = TextColorSecondary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                customDayOptions.forEach { (day, label) ->
                    val isSelected = day in validCustomDays
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCustomDays = if (isSelected) {
                                validCustomDays - day
                            } else {
                                (validCustomDays + day).distinct().sorted()
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = TextColorSecondary)
                Spacer(Modifier.width(6.dp))
                Text("Reminder Habit", color = TextColorSecondary)
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
            )
        }

        if (reminderEnabled) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = reminderTime,
                onValueChange = { },
                readOnly = true,
                label = { Text("Jam Reminder") },
                trailingIcon = {
                    Text(
                        text = "Pilih",
                        color = AccentYellow,
                        modifier = Modifier.clickable {
                            val currentHour = reminderTime.substringBefore(":").toIntOrNull() ?: 20
                            val currentMinute = reminderTime.substringAfter(":").toIntOrNull() ?: 0

                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    reminderTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                },
                                currentHour,
                                currentMinute,
                                true
                            ).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = TextColorSecondary)
            ) {
                Text("Batal")
            }
            Button(
                onClick = {
                    onConfirm(
                        name.trim(),
                        selectedBobot.xp,
                        scheduleType,
                        validCustomDays,
                        timesPerWeek,
                        reminderEnabled,
                        reminderTime
                    )
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(if (habitToEdit == null) "Tambah Misi" else "Simpan")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = PrimaryColor,
    unfocusedIndicatorColor = TextColorSecondary,
    cursorColor = PrimaryColor,
    focusedTextColor = TextColorPrimary,
    unfocusedTextColor = TextColorPrimary,
    focusedLabelColor = PrimaryColor,
    unfocusedLabelColor = TextColorSecondary
)
