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
fun RamadhanCalmBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ramadhanStarsTransition")
    val motion by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ramadhanStarsMotion"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A251C),
                    Color(0xFF071A14),
                    Color(0xFF05120E)
                )
            )
        )

        val twinkle = (motion * 2f * PI).toFloat()
        repeat(52) { index ->
            val xSeed = ((index * 37 + 11) % 100) / 100f
            val ySeed = ((index * 61 + 17) % 100) / 100f
            val phase = index * 0.43f
            val pulse = (0.55f + 0.45f * sin(twinkle + phase)).coerceIn(0.1f, 1f)
            val drift = (sin(twinkle * 0.65f + phase) * 0.004f).toFloat()

            val center = Offset(
                x = (xSeed + drift).coerceIn(0.03f, 0.97f) * size.width,
                y = (ySeed - drift).coerceIn(0.05f, 0.95f) * size.height
            )

            val radius = when {
                index % 11 == 0 -> 3.0f
                index % 5 == 0 -> 2.1f
                else -> 1.4f
            }

            drawCircle(
                color = Color(0xFFFFE8A3).copy(alpha = 0.06f + (pulse * 0.16f)),
                radius = radius,
                center = center
            )
        }

        val moonCenter = Offset(size.width * 0.84f, size.height * 0.14f)
        val moonOuter = size.minDimension * 0.068f
        val moonInner = size.minDimension * 0.058f

        drawCircle(
            color = Color(0x26FFE9A6),
            radius = moonOuter,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xAA081A14),
            radius = moonInner,
            center = moonCenter + Offset(size.minDimension * 0.028f, 0f)
        )
    }
}
