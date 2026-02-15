package com.ade.habittracker.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.R
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

data class ChibiMessage(
    val text: String,
    val voiceRes: Int
)

@Composable
fun DraggableChibiWithBubble(
    chibiRes: Int,
    messages: List<ChibiMessage>,
    modifier: Modifier = Modifier,
    voiceEnabled: Boolean = true,
    externalMessage: String? = null,
    onExternalMessageDismiss: () -> Unit = {},
    autoBehaviorEnabled: Boolean = true,
    autoTalkMinDelayMs: Long = 10_000L,
    autoTalkMaxDelayMs: Long = 10_000L,
    autoMoveMinDelayMs: Long = 4_500L,
    autoMoveMaxDelayMs: Long = 8_500L,
    autoHideMinVisibleMs: Long = 10_000L,
    autoHideMaxVisibleMs: Long = 18_000L,
    autoHideMinDurationMs: Long = 1_800L,
    autoHideMaxDurationMs: Long = 3_200L
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var offset by remember { mutableStateOf(Offset.Zero) }
    var internalMessage by remember { mutableStateOf<ChibiMessage?>(null) }
    var isChibiVisible by remember { mutableStateOf(true) }

    val activeText = externalMessage ?: internalMessage?.text
    val isBubbleVisible = activeText != null
    val isExternalActive = externalMessage != null
    val effectiveChibiVisible = isChibiVisible || isExternalActive

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun playVoice(resId: Int) {
        if (!voiceEnabled) return
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.start()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val chibiSizePx = with(density) { 70.dp.toPx() }
        val marginPx = with(density) { 24.dp.toPx() }
        val minX = marginPx
        val minY = marginPx
        val maxX = (maxWidthPx - chibiSizePx - marginPx).coerceAtLeast(minX)
        val maxY = (maxHeightPx - chibiSizePx - marginPx).coerceAtLeast(minY)

        fun clampOffset(raw: Offset): Offset {
            return Offset(
                x = raw.x.coerceIn(minX, maxX),
                y = raw.y.coerceIn(minY, maxY)
            )
        }

        fun snapToEdge(raw: Offset): Offset {
            val clamped = clampOffset(raw)
            val middleX = (minX + maxX) / 2f
            val edgeX = if (clamped.x < middleX) minX else maxX
            return Offset(edgeX, clamped.y)
        }

        fun randomEdgeOffset(): Offset {
            val targetY = if (maxY > minY) {
                Random.nextFloat() * (maxY - minY) + minY
            } else {
                minY
            }
            val edgeX = if (Random.nextBoolean()) minX else maxX
            return Offset(edgeX, targetY)
        }

        LaunchedEffect(maxWidthPx, maxHeightPx) {
            if (maxWidthPx > 0f && maxHeightPx > 0f && offset == Offset.Zero) {
                val start = snapToEdge(
                    Offset(
                        x = maxWidthPx - chibiSizePx - marginPx,
                        y = (maxHeightPx * 0.55f)
                    )
                )
                offset = start
            }
        }

        LaunchedEffect(externalMessage) {
            if (externalMessage != null) {
                internalMessage = null
                isChibiVisible = true
                playVoice(R.raw.chibi_misi_selesai)

                delay(3000)
                onExternalMessageDismiss()
            }
        }

        LaunchedEffect(internalMessage) {
            if (internalMessage != null) {
                delay(2500)
                internalMessage = null
            }
        }

        LaunchedEffect(autoBehaviorEnabled, messages, effectiveChibiVisible, isExternalActive) {
            if (!autoBehaviorEnabled || messages.isEmpty()) return@LaunchedEffect

            while (true) {
                val safeMin = minOf(autoTalkMinDelayMs, autoTalkMaxDelayMs)
                val safeMax = maxOf(autoTalkMinDelayMs, autoTalkMaxDelayMs)
                val waitMs = if (safeMin == safeMax) safeMin else Random.nextLong(safeMin, safeMax)
                delay(waitMs)

                if (!isExternalActive && internalMessage == null && effectiveChibiVisible) {
                    val picked = messages.random()
                    internalMessage = picked
                    playVoice(picked.voiceRes)
                }
            }
        }

        LaunchedEffect(autoBehaviorEnabled, maxWidthPx, maxHeightPx, isExternalActive, internalMessage, effectiveChibiVisible) {
            if (!autoBehaviorEnabled || maxWidthPx <= 0f || maxHeightPx <= 0f) return@LaunchedEffect

            while (true) {
                val safeMin = minOf(autoMoveMinDelayMs, autoMoveMaxDelayMs)
                val safeMax = maxOf(autoMoveMinDelayMs, autoMoveMaxDelayMs)
                val waitMs = if (safeMin == safeMax) safeMin else Random.nextLong(safeMin, safeMax)
                delay(waitMs)

                if (!isExternalActive && internalMessage == null && effectiveChibiVisible) {
                    offset = randomEdgeOffset()
                }
            }
        }

        LaunchedEffect(autoBehaviorEnabled, isExternalActive) {
            if (!autoBehaviorEnabled) return@LaunchedEffect

            while (true) {
                val visibleMin = minOf(autoHideMinVisibleMs, autoHideMaxVisibleMs)
                val visibleMax = maxOf(autoHideMinVisibleMs, autoHideMaxVisibleMs)
                val visibleDuration = if (visibleMin == visibleMax) visibleMin else Random.nextLong(visibleMin, visibleMax)
                delay(visibleDuration)

                if (!isExternalActive) {
                    isChibiVisible = false

                    val hiddenMin = minOf(autoHideMinDurationMs, autoHideMaxDurationMs)
                    val hiddenMax = maxOf(autoHideMinDurationMs, autoHideMaxDurationMs)
                    val hiddenDuration = if (hiddenMin == hiddenMax) hiddenMin else Random.nextLong(hiddenMin, hiddenMax)
                    delay(hiddenDuration)

                    isChibiVisible = true
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = effectiveChibiVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 350)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .pointerInput(maxWidthPx, maxHeightPx) {
                        detectDragGestures(
                            onDragEnd = {
                                offset = snapToEdge(offset)
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val next = snapToEdge(clampOffset(offset + dragAmount))
                            offset = next
                        }
                    }
            ) {
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = isBubbleVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-58).dp)
                    ) {
                        Surface(
                            color = CardBackground,
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            border = if (isExternalActive) BorderStroke(2.dp, AccentYellow) else null
                        ) {
                            Text(
                                text = activeText.orEmpty(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isExternalActive) AccentYellow else TextColorPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = chibiRes),
                        contentDescription = "Chibi Helper",
                        modifier = Modifier
                            .size(70.dp)
                            .pointerInput(messages, isExternalActive) {
                                detectTapGestures(
                                    onTap = {
                                        if (!isExternalActive && messages.isNotEmpty()) {
                                            val picked = messages.random()
                                            internalMessage = picked
                                            playVoice(picked.voiceRes)
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}
