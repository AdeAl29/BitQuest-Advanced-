package com.ade.habittracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.R
import com.ade.habittracker.data.AssistantPersona
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.AssistantChatMessage
import com.ade.habittracker.model.AssistantMessageSender
import com.ade.habittracker.model.isCompletedOn
import com.ade.habittracker.model.isDueOn
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun AssistantInboxScreen(
    appData: AppData,
    assistants: List<AssistantPersona>,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextColorPrimary
                    )
                }
                Column {
                    Text(
                        text = "Chat Asisten",
                        color = AccentYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Pilih partner waifu yang mau kamu ajak ngobrol dulu.",
                        color = TextColorSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kotak masuk BitQuest",
                    color = TextColorPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Setiap assistant punya vibe, cara ngomong, dan gaya support yang beda. Tinggal pilih yang paling cocok sama mood kamu sekarang.",
                    color = TextColorSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assistants, key = { it.id }) { assistant ->
                val latestMessage = appData.assistantChatHistories[assistant.id]
                    ?.maxByOrNull { it.timestamp }
                val preview = latestMessage?.text ?: assistant.greeting
                val isActive = appData.selectedAssistantId == assistant.id
                AssistantInboxItem(
                    assistant = assistant,
                    preview = preview,
                    lastMessageTimestamp = latestMessage?.timestamp,
                    isActive = isActive,
                    onClick = { onOpenChat(assistant.id) }
                )
            }
        }
    }
}

@Composable
fun AssistantChatScreen(
    appData: AppData,
    assistants: List<AssistantPersona>,
    selectedAssistantId: String,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClearConversation: (String) -> Unit
) {
    val selectedAssistant = assistants.firstOrNull { it.id == selectedAssistantId } ?: assistants.first()
    val palette = remember(selectedAssistant.id) { assistantPaletteFor(selectedAssistant.id) }
    val currentMessages = appData.assistantChatHistories[selectedAssistant.id]
        ?.sortedBy { it.timestamp }
        .orEmpty()
        .ifEmpty {
            listOf(
                AssistantChatMessage(
                    id = "${selectedAssistant.id}_greeting",
                    assistantId = selectedAssistant.id,
                    sender = AssistantMessageSender.ASSISTANT,
                    text = selectedAssistant.greeting,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    val listState = rememberLazyListState()
    var input by rememberSaveable(selectedAssistant.id) { mutableStateOf("") }
    var pendingSendText by remember(selectedAssistant.id) { mutableStateOf<String?>(null) }
    val isTyping = pendingSendText != null

    LaunchedEffect(pendingSendText) {
        val text = pendingSendText ?: return@LaunchedEffect
        delay(650)
        onSendMessage(text)
        pendingSendText = null
    }

    LaunchedEffect(selectedAssistant.id, currentMessages.size, isTyping) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex + if (isTyping) 1 else 0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(top = 8.dp, bottom = 10.dp)
    ) {
        AssistantTopBar(
            assistant = selectedAssistant,
            palette = palette,
            onBack = onBack,
            onClearConversation = { onClearConversation(selectedAssistant.id) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(CardBackground.copy(alpha = 0.98f))
                .border(1.dp, palette.primary.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentMessages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        assistant = selectedAssistant,
                        palette = palette
                    )
                }
                if (isTyping) {
                    item(key = "typing_indicator") {
                        TypingIndicatorBubble(
                            assistant = selectedAssistant,
                            palette = palette
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(22.dp),
            color = CardBackground.copy(alpha = 0.98f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    minLines = 2,
                    maxLines = 4,
                    placeholder = {
                        Text(
                            text = "Ketik pesan...",
                            color = TextColorSecondary
                        )
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val text = input.trim()
                            if (text.isNotBlank() && pendingSendText == null) {
                                pendingSendText = text
                                input = ""
                            }
                        },
                        enabled = pendingSendText == null,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantInboxItem(
    assistant: AssistantPersona,
    preview: String,
    lastMessageTimestamp: Long?,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val palette = remember(assistant.id) { assistantPaletteFor(assistant.id) }
    val lastSeenText = remember(lastMessageTimestamp) {
        lastMessageTimestamp?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        } ?: ""
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) palette.primary.copy(alpha = 0.14f) else CardBackground.copy(alpha = 0.98f)
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) palette.primary.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistantAvatar(
                assistant = assistant,
                modifier = Modifier.size(70.dp),
                badgeText = null
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assistant.name,
                        color = TextColorPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        if (lastSeenText.isNotBlank()) {
                            Text(
                                text = lastSeenText,
                                color = TextColorSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isActive) {
                            Text(
                                text = "Aktif",
                                color = palette.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Text(
                    text = assistant.archetype,
                    color = palette.secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = preview,
                    color = TextColorSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(palette.secondary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = palette.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AssistantTopBar(
    assistant: AssistantPersona,
    palette: AssistantPalette,
    onBack: () -> Unit,
    onClearConversation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextColorPrimary
                )
            }
            AssistantAvatar(
                assistant = assistant,
                modifier = Modifier.size(44.dp),
                badgeText = null
            )
            Column {
                Text(
                    text = assistant.name,
                    color = TextColorPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(palette.secondary)
                    )
                    Text(
                        text = "Online - Siap bantu",
                        color = TextColorSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
        IconButton(
            onClick = onClearConversation,
            modifier = Modifier
                .clip(CircleShape)
                .background(palette.secondary.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Reset chat",
                tint = palette.secondary
            )
        }
    }
}
@Composable
private fun AssistantHeroCard(
    assistant: AssistantPersona,
    palette: AssistantPalette,
    level: Int,
    streak: Int,
    dueToday: Int,
    completedToday: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = 0.22f),
                            palette.secondary.copy(alpha = 0.14f),
                            CardBackground
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = assistant.name,
                        color = TextColorPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = assistant.archetype,
                        color = palette.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = assistant.tagline,
                        color = TextColorSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                AssistantAvatar(
                    assistant = assistant,
                    modifier = Modifier.size(126.dp),
                    badgeText = assistant.moodLabel
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroMiniMetric(
                    label = "Level",
                    value = level.toString(),
                    accent = palette.primary,
                    modifier = Modifier.weight(1f)
                )
                HeroMiniMetric(
                    label = "Streak",
                    value = "$streak h",
                    accent = palette.secondary,
                    modifier = Modifier.weight(1f)
                )
                HeroMiniMetric(
                    label = "Hari ini",
                    value = "$completedToday/$dueToday",
                    accent = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
private fun QuickInsightStrip(
    palette: AssistantPalette,
    selectedAssistant: AssistantPersona,
    appData: AppData,
    completedToday: Int,
    dueToday: Int
) {
    val moodText = when {
        completedToday == 0 && dueToday > 0 -> "Hari ini masih bisa dibuka dengan satu langkah kecil."
        completedToday in 1..2 -> "Kamu sudah mulai. Tinggal jaga ritmenya tetap hidup."
        completedToday >= 3 -> "Momentummu lagi bagus. Cocok buat dorong satu langkah lagi."
        else -> "Hari ini cukup tenang. Kita bisa susun langkah selanjutnya dengan rapi."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.secondary.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = palette.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Insight dari ${selectedAssistant.name}",
                    color = TextColorPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = moodText,
                color = TextColorSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Text(
                text = "Level ${appData.level} | ${appData.totalXp} XP total | ${appData.focusTotalSessions} sesi fokus terkumpul",
                color = TextColorSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: AssistantChatMessage,
    assistant: AssistantPersona,
    palette: AssistantPalette
) {
    val isUser = message.sender == AssistantMessageSender.USER
    val bubbleColor = if (isUser) palette.primary.copy(alpha = 0.18f) else CardBackground
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val borderColor = if (isUser) palette.primary.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)
    val timestamp = remember(message.timestamp) {
        Instant.ofEpochMilli(message.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    if (isUser) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalAlignment = alignment
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .widthIn(min = 260.dp),
                shape = RoundedCornerShape(
                    topStart = 22.dp,
                    topEnd = 22.dp,
                    bottomStart = 22.dp,
                    bottomEnd = 8.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message.text,
                        color = TextColorPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = timestamp,
                        color = TextColorSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AssistantAvatar(
                assistant = assistant,
                modifier = Modifier.size(34.dp),
                badgeText = null
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 240.dp),
                shape = RoundedCornerShape(
                    topStart = 22.dp,
                    topEnd = 22.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 22.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = assistant.name,
                        color = palette.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = message.text,
                        color = TextColorPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = timestamp,
                        color = TextColorSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble(
    assistant: AssistantPersona,
    palette: AssistantPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AssistantAvatar(
            assistant = assistant,
            modifier = Modifier.size(34.dp),
            badgeText = null
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.16f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${assistant.name} sedang mengetik", color = TextColorSecondary, fontSize = 12.sp)
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size((5 + index).dp)
                            .clip(CircleShape)
                            .background(palette.secondary.copy(alpha = 0.45f + index * 0.15f))
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistantAvatar(
    assistant: AssistantPersona,
    modifier: Modifier,
    badgeText: String?
) {
    val context = LocalContext.current
    val resId = remember(assistant.assetName) {
        context.resources.getIdentifier(assistant.assetName, "drawable", context.packageName)
            .takeIf { it != 0 }
    }

    Box(contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(2.dp, Color.White.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (resId != null) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = assistant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = assistant.name.take(1),
                        color = TextColorPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xCC111111),
                border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.24f)),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = AccentYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroMiniMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextColorSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
private data class AssistantPalette(
    val primary: Color,
    val secondary: Color
)

private fun assistantPaletteFor(id: String): AssistantPalette = when (id) {
    "aika_tsundere" -> AssistantPalette(Color(0xFFE56B87), Color(0xFFFFB347))
    "luna_kuudere" -> AssistantPalette(Color(0xFF7E8CE0), Color(0xFFA7C5FF))
    "sora_genki" -> AssistantPalette(Color(0xFFFF9F45), Color(0xFFFFD166))
    "elara_strategist" -> AssistantPalette(Color(0xFF8C7BFF), Color(0xFFC2B6FF))
    "yuna_healer" -> AssistantPalette(Color(0xFF4DB6AC), Color(0xFF9BE7C4))
    "reina_gamer" -> AssistantPalette(Color(0xFF6C63FF), Color(0xFF58E1FF))
    else -> AssistantPalette(Color(0xFF8C7BFF), Color(0xFFFFD166))
}


