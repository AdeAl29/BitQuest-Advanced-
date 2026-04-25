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
fun ForestCampBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "forestCampTransition")
    val ember by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "forestCampEmber"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A1A12),
                    Color(0xFF08130D),
                    Color(0xFF050A07)
                )
            )
        )

        drawRect(
            color = Color(0x22000000),
            topLeft = Offset(0f, size.height * 0.64f),
            size = Size(size.width, size.height * 0.36f)
        )

        val pulse = (ember * 2f * PI).toFloat()
        val fireCenter = Offset(size.width * 0.22f, size.height * 0.78f)
        drawCircle(
            color = Color(0x55FFB74D),
            radius = size.minDimension * (0.1f + 0.01f * sin(pulse)),
            center = fireCenter
        )
        drawCircle(
            color = Color(0xFFFF8A50),
            radius = size.minDimension * 0.045f,
            center = fireCenter
        )
        drawCircle(
            color = Color(0xFFFFD180),
            radius = size.minDimension * 0.022f,
            center = fireCenter
        )
    }
}
