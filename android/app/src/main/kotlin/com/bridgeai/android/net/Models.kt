package com.bridgeai.android.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(val status: String? = null, val uptime: Double? = null)

@Serializable
data class ModelsResponse(val data: List<ModelInfo> = emptyList())

@Serializable
data class ModelInfo(
    val id: String,
    val `object`: String? = null,
    val owned_by: String? = null
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double? = null
)

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<ChatChoice> = emptyList()
)

@Serializable
data class ChatChoice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    val delta: ChatDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ChatDelta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class ProviderInfo(
    val id: String,
    val name: String,
    val requiresKey: Boolean,
    val getKeyUrl: String?,
    val configured: Boolean = false
)
