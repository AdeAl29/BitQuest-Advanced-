package com.ade.habittracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun AchievementItem(achievement: Achievement) {
    // Animasi Progress Bar
    val progressPercent = (achievement.progress.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressPercent, label = "progress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        // Memberikan border emas jika achievement sudah terbuka
        border = if (achievement.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, AccentYellow.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- BAGIAN ICON ---
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) AccentYellow.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, if (achievement.isUnlocked) AccentYellow else Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    // 🔥 SUDAH DIPERBAIKI: Menggunakan imageResId
                    Image(
                        painter = painterResource(id = achievement.imageResId),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- BAGIAN TEKS & PROGRESS ---
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (achievement.isUnlocked) TextColorPrimary else Color.Gray
                )

                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = TextColorSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentYellow,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // 🔥 SUDAH DIPERBAIKI: Menggunakan target (bukan goal)
                    Text(
                        text = "${achievement.progress}/${achievement.target}",
                        fontSize = 10.sp,
                        color = if (achievement.isUnlocked) AccentYellow else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}