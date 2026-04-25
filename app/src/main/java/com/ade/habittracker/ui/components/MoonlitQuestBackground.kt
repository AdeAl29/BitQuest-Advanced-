package com.ade.habittracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MoonlitQuestBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "moonlitQuestTransition")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "moonlitDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0B1730),
                    Color(0xFF081121),
                    Color(0xFF040814)
                )
            )
        )

        val phase = (drift * 2f * PI).toFloat()
        repeat(64) { index ->
            val xSeed = ((index * 43 + 9) % 100) / 100f
            val ySeed = ((index * 59 + 17) % 100) / 100f
            val twinkle = (0.45f + 0.55f * sin(phase + index)).coerceIn(0.08f, 1f)
            drawCircle(
                color = Color(0xFFCAE7FF).copy(alpha = 0.05f + (0.16f * twinkle)),
                radius = if (index % 8 == 0) 2.8f else 1.6f,
                center = Offset(xSeed * size.width, ySeed * size.height)
            )
        }

        val moonCenter = Offset(size.width * 0.82f, size.height * 0.16f)
        drawCircle(
            color = Color(0x35D9F1FF),
            radius = size.minDimension * 0.09f,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xFFFFF6D6),
            radius = size.minDimension * 0.065f,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xFF0B1730),
            radius = size.minDimension * 0.055f,
            center = moonCenter + Offset(size.minDimension * 0.025f, 0f)
        )
    }
}
