package com.bridgeai.android.net

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class FreeLlmApiClient(
    private val baseUrlProvider: () -> String,
    private val apiKeyProvider: () -> String?
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis  = 120_000
        }
        install(Logging) { level = LogLevel.INFO }
        expectSuccess = false
    }

    private fun url(path: String): String {
        val base = baseUrlProvider().trimEnd('/')
        return "$base$path"
    }

    private fun HttpRequestBuilder.auth() {
        apiKeyProvider()?.takeIf { it.isNotBlank() }?.let {
            headers.append(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    suspend fun ping(): Result<PingResponse> = runCatching {
        val r = http.get(url("/api/ping")) { auth() }
        if (r.status.isSuccess()) r.body() else throw IllegalStateException("HTTP ${r.status.value}")
    }

    suspend fun models(): Result<List<ModelInfo>> = runCatching {
        val r = http.get(url("/v1/models")) { auth() }
        if (!r.status.isSuccess()) throw IllegalStateException("HTTP ${r.status.value}: ${r.bodyAsText()}")
        r.body<ModelsResponse>().data
    }

    suspend fun chat(model: String, messages: List<ChatMessage>, temperature: Double? = null): Result<String> = runCatching {
        val r = http.post(url("/v1/chat/completions")) {
            auth()
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(model, messages, stream = false, temperature = temperature))
        }
        if (!r.status.isSuccess()) throw IllegalStateException("HTTP ${r.status.value}: ${r.bodyAsText()}")
        val body = r.body<ChatResponse>()
        body.choices.firstOrNull()?.message?.content ?: ""
    }

    fun chatStream(model: String, messages: List<ChatMessage>, temperature: Double? = null): Flow<String> = flow {
        http.preparePost(url("/v1/chat/completions")) {
            auth()
            contentType(ContentType.Application.Json)
            accept(ContentType.Text.EventStream)
            setBody(ChatRequest(model, messages, stream = true, temperature = temperature))
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw IllegalStateException("HTTP ${response.status.value}: ${response.bodyAsText()}")
            }
            val channel: ByteReadChannel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (!line.startsWith("data:")) continue
                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]" || payload.isEmpty()) continue
                val chunk = runCatching { json.decodeFromString(ChatResponse.serializer(), payload) }.getOrNull() ?: continue
                val content = chunk.choices.firstOrNull()?.delta?.content ?: continue
                emit(content)
            }
        }
    }

    fun close() = http.close()
}
