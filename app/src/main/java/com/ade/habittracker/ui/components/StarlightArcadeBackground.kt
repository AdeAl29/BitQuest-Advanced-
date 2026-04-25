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
fun StarlightArcadeBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "starlightArcadeTransition")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arcadePulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF110A24),
                    Color(0xFF0A091A),
                    Color(0xFF060610)
                )
            )
        )

        val wave = (pulse * 2f * PI).toFloat()
        repeat(10) { index ->
            val y = size.height * (0.18f + index * 0.08f)
            val alpha = (0.03f + 0.05f * (0.5f + 0.5f * sin(wave + index))).coerceIn(0.03f, 0.09f)
            drawLine(
                color = if (index % 2 == 0) Color(0xFF7C4DFF).copy(alpha = alpha) else Color(0xFF00E5FF).copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
        }

        repeat(24) { index ->
            val x = (((index * 23 + 11) % 100) / 100f) * size.width
            val y = (((index * 41 + 19) % 100) / 100f) * size.height
            drawCircle(
                color = if (index % 2 == 0) Color(0xFF9A8CFF).copy(alpha = 0.18f) else Color(0xFF67E8F9).copy(alpha = 0.18f),
                radius = if (index % 3 == 0) 4f else 2f,
                center = Offset(x, y)
            )
        }
    }
}
