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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun RamadhanFestiveBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ramadhanFestiveTransition")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "festivePulse"
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "festiveDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F2E22),
                    Color(0xFF0A2118),
                    Color(0xFF07140F)
                )
            )
        )

        val pulseValue = (pulse * 2f * PI).toFloat()
        repeat(86) { index ->
            val xSeed = ((index * 43 + 7) % 100) / 100f
            val ySeed = ((index * 67 + 19) % 100) / 100f
            val phase = index * 0.41f
            val shimmer = (0.45f + 0.55f * sin(pulseValue + phase)).coerceIn(0.08f, 1f)
            val sway = (sin((drift * 2f * PI).toFloat() + phase) * 0.006f).toFloat()

            val center = Offset(
                x = (xSeed + sway).coerceIn(0.03f, 0.97f) * size.width,
                y = (ySeed - sway).coerceIn(0.04f, 0.96f) * size.height
            )

            val radius = when {
                index % 13 == 0 -> 3.6f
                index % 5 == 0 -> 2.5f
                else -> 1.6f
            }

            drawCircle(
                color = Color(0xFFFFE08A).copy(alpha = 0.05f + 0.22f * shimmer),
                radius = radius,
                center = center
            )
        }

        val moonCenter = Offset(size.width * 0.84f, size.height * 0.16f)
        drawCircle(
            color = Color(0x40FFE7A8),
            radius = size.minDimension * 0.078f,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xCC0C1E16),
            radius = size.minDimension * 0.066f,
            center = moonCenter + Offset(size.minDimension * 0.032f, 0f)
        )

        val lampGlow = 0.35f + 0.65f * (0.5f + 0.5f * sin(pulseValue))
        drawLantern(
            anchorX = size.width * 0.16f,
            anchorY = size.height * 0.05f,
            drop = size.height * 0.20f,
            glowAlpha = (0.10f * lampGlow).coerceIn(0.06f, 0.16f)
        )
        drawLantern(
            anchorX = size.width * 0.72f,
            anchorY = size.height * 0.07f,
            drop = size.height * 0.16f,
            glowAlpha = (0.09f * lampGlow).coerceIn(0.05f, 0.14f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLantern(
    anchorX: Float,
    anchorY: Float,
    drop: Float,
    glowAlpha: Float
) {
    val bodyTop = anchorY + drop
    val bodySize = size.minDimension * 0.05f

    drawLine(
        color = Color(0x55EFD99A),
        start = Offset(anchorX, anchorY),
        end = Offset(anchorX, bodyTop),
        strokeWidth = 1.8f
    )

    drawCircle(
        color = Color(0xFFFFD773).copy(alpha = glowAlpha),
        radius = bodySize * 1.5f,
        center = Offset(anchorX, bodyTop + bodySize * 0.45f)
    )

    drawRoundRect(
        color = Color(0xCCF6D47A),
        topLeft = Offset(anchorX - bodySize * 0.55f, bodyTop),
        size = Size(bodySize * 1.1f, bodySize * 1.25f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodySize * 0.28f, bodySize * 0.28f)
    )
}
