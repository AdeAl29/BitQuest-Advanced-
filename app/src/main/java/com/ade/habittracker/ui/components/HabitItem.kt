package com.ade.habittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Habit
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(
    habit: Habit,
    isDueToday: Boolean,
    extraScheduleInfo: String?,
    missionCardSkinId: String = "mission_card_default",
    checklistEffectId: String = "checklist_effect_default",
    isPinned: Boolean = false,
    isCompactMode: Boolean = false,
    onTogglePinned: () -> Unit = {},
    onCheckedChanged: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dismissState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!habit.isCompleted) onEditClick()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteClick()
                    false
                }
                else -> false
            }
        }
    )

    val statusColor = when {
        habit.isCompleted -> PrimaryColor
        isDueToday -> Color(0xFF00E5FF)
        else -> Color(0xFF607D8B)
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50).copy(alpha = 0.8f)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935).copy(alpha = 0.8f)
                    else -> Color.Transparent
                },
                label = "swipeColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(color)
                    .padding(horizontal = 18.dp),
                contentAlignment = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                val swipeIcon = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                    else -> null
                }
                swipeIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        content = {
            HabitCardContent(
                habit = habit,
                statusColor = statusColor,
                isDueToday = isDueToday,
                extraScheduleInfo = extraScheduleInfo,
                missionCardSkinId = missionCardSkinId,
                checklistEffectId = checklistEffectId,
                isPinned = isPinned,
                isCompactMode = isCompactMode,
                onTogglePinned = onTogglePinned,
                onCheckedChanged = onCheckedChanged,
                onReminderToggle = onReminderToggle
            )
        }
    )
}

@Composable
private fun HabitCardContent(
    habit: Habit,
    statusColor: Color,
    isDueToday: Boolean,
    extraScheduleInfo: String?,
    missionCardSkinId: String,
    checklistEffectId: String,
    isPinned: Boolean,
    isCompactMode: Boolean,
    onTogglePinned: () -> Unit,
    onCheckedChanged: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    val missionCardSkin = ShopRepository.getMissionCardSkinById(missionCardSkinId)
    val checklistEffect = ShopRepository.getChecklistEffectById(checklistEffectId)
    val effectiveStatusColor = when {
        habit.isCompleted -> checklistEffect.primaryColor
        missionCardSkinId != "mission_card_default" -> missionCardSkin.primaryColor
        else -> statusColor
    }
    val cardContainer = if (missionCardSkinId == "mission_card_default") {
        CardBackground
    } else {
        missionCardSkin.secondaryColor.copy(alpha = 0.92f)
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (habit.isCompleted) 0.6f else 1f,
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (habit.isCompleted) checklistEffect.primaryColor.copy(alpha = 0.28f)
            else effectiveStatusColor.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(effectiveStatusColor, effectiveStatusColor.copy(alpha = 0.45f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = if (isCompactMode) 10.dp else 14.dp)
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(if (isCompactMode) 3.dp else 6.dp)
            ) {
                Text(
                    text = habit.name,
                    fontSize = if (isCompactMode) 15.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextColorPrimary,
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null
                )

                if (!isCompactMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HabitMetaChip(
                            text = habit.schedule,
                            accent = Color.White.copy(alpha = 0.16f),
                            icon = null
                        )
                        HabitMetaChip(
                            text = "+${habit.weight} XP",
                            accent = if (habit.isCompleted) checklistEffect.primaryColor else Color(0xFFFFC107),
                            icon = Icons.Default.CheckCircle
                        )
                        if (isPinned) {
                            HabitMetaChip(
                                text = "Pinned",
                                accent = AccentYellow,
                                icon = Icons.Default.Star
                            )
                        }
                        if (!isDueToday) {
                            HabitMetaChip(
                                text = "Tidak aktif",
                                accent = Color(0xFF90A4AE),
                                icon = null
                            )
                        }
                    }

                    if (!extraScheduleInfo.isNullOrBlank()) {
                        Text(
                            text = extraScheduleInfo,
                            fontSize = 11.sp,
                            color = if (isDueToday) TextColorSecondary else Color(0xFFB0BEC5)
                        )
                    }
                } else {
                    Text(
                        text = "${habit.schedule} • +${habit.weight} XP",
                        fontSize = 11.sp,
                        color = TextColorSecondary,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onTogglePinned,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (isPinned) "Lepas pin" else "Pin misi",
                        tint = if (isPinned) AccentYellow else TextColorSecondary.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onReminderToggle(!habit.reminderEnabled) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (habit.reminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = "Toggle Reminder",
                        tint = if (habit.reminderEnabled) AccentYellow else TextColorSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Checkbox(
                    checked = habit.isCompleted,
                    enabled = isDueToday && !habit.isCompleted,
                    onCheckedChange = onCheckedChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = checklistEffect.primaryColor,
                        uncheckedColor = TextColorSecondary,
                        checkmarkColor = Color.Black,
                        disabledCheckedColor = checklistEffect.primaryColor.copy(alpha = 0.7f),
                        disabledUncheckedColor = TextColorSecondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(30.dp)
                )
                if (habit.isCompleted && checklistEffectId != "checklist_effect_default") {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(checklistEffect.primaryColor.copy(alpha = 0.18f))
                            .border(
                                width = 1.dp,
                                color = checklistEffect.primaryColor.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = checklistEffect.emoji,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = checklistEffect.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitMetaChip(
    text: String,
    accent: Color,
    icon: ImageVector?
) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = accent.copy(alpha = 0.16f),
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                color = if (accent == Color.White.copy(alpha = 0.16f)) TextColorSecondary else accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
