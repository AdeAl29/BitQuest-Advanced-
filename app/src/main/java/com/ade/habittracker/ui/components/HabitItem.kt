package com.ade.habittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Habit
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
            )
        },
        content = {
            HabitCardContent(
                habit = habit,
                statusColor = statusColor,
                isDueToday = isDueToday,
                extraScheduleInfo = extraScheduleInfo,
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
    onCheckedChanged: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (habit.isCompleted) 0.6f else 1f,
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .width(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(statusColor, statusColor.copy(alpha = 0.5f))
                        )
                    )
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(0.dp),
                        spotColor = statusColor
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = habit.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColorPrimary,
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = habit.schedule,
                            fontSize = 11.sp,
                            color = TextColorSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "+${habit.weight} XP",
                        fontSize = 12.sp,
                        color = if (habit.isCompleted) PrimaryColor else Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!extraScheduleInfo.isNullOrBlank()) {
                    Text(
                        text = extraScheduleInfo,
                        fontSize = 11.sp,
                        color = if (isDueToday) TextColorSecondary else Color(0xFFB0BEC5)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
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
                        checkedColor = PrimaryColor,
                        uncheckedColor = TextColorSecondary,
                        checkmarkColor = Color.Black,
                        disabledCheckedColor = PrimaryColor.copy(alpha = 0.7f),
                        disabledUncheckedColor = TextColorSecondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
