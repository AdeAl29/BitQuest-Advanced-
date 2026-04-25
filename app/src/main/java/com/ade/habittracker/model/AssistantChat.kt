package com.ade.habittracker.model

import kotlinx.serialization.Serializable

@Serializable
enum class AssistantMessageSender {
    USER,
    ASSISTANT
}

@Serializable
data class AssistantChatMessage(
    val id: String,
    val assistantId: String,
    val sender: AssistantMessageSender,
    val text: String,
    val timestamp: Long
)
