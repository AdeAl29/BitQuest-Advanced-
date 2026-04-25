package com.ade.habittracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ade.habittracker.data.DailyReward
import com.ade.habittracker.data.DailyRewardsConfig
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun DailyLoginDialog(
    currentDayIndex: Int,
    rewardToday: DailyReward,
    onClaim: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackground)
                .border(1.dp, PrimaryColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .wrapContentHeight()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryColor.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Login Harian",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColorPrimary
                )
                Text(
                    "Kumpulkan koin untuk jajan di Shop!",
                    fontSize = 12.sp,
                    color = TextColorSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    itemsIndexed(DailyRewardsConfig.REWARDS) { index, reward ->
                        val isToday = index == currentDayIndex
                        val isPast = index < currentDayIndex
                        val isJackpot = index == 6

                        DayRewardItem(
                            reward = reward,
                            isToday = isToday,
                            isPast = isPast,
                            isJackpot = isJackpot
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Hari ke-${currentDayIndex + 1}: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextColorSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${rewardToday.coins} Coins",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(
                        "KLAIM HADIAH",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DayRewardItem(
    reward: DailyReward,
    isToday: Boolean,
    isPast: Boolean,
    isJackpot: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isToday) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bgColor = when {
        isToday -> PrimaryColor
        isPast -> PrimaryColor.copy(alpha = 0.2f)
        isJackpot -> AccentYellow.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val borderColor = when {
        isToday -> Color.White
        isJackpot -> AccentYellow.copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val contentColor = when {
        isToday -> Color.White
        isPast -> TextColorSecondary.copy(alpha = 0.5f)
        isJackpot -> AccentYellow
        else -> TextColorSecondary
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isToday) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .aspectRatio(1f)
            .scale(if (isToday) scale else 1f)
            .alpha(if (isPast) 0.6f else 1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isToday) {
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(6.dp)
                        .background(Color(0xFFFF5252), CircleShape)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HARI ${reward.day}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coin",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${reward.coins}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
            }
        }
    }
}
