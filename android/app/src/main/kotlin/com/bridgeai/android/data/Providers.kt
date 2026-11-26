package com.bridgeai.android.data

/** Static catalog of FreeLLMAPI providers, mirrored from the CLI. */
data class Provider(
    val id: String,
    val name: String,
    val requiresKey: Boolean,
    val free: Boolean,
    val getKeyUrl: String?
)

object Providers {
    val all: List<Provider> = listOf(
        Provider("kilo",       "Kilo",                requiresKey = false, free = true, getKeyUrl = null),
        Provider("ovh",        "OVH AI Endpoints",    requiresKey = false, free = true, getKeyUrl = null),
        Provider("llm7",       "LLM7",                requiresKey = false, free = true, getKeyUrl = null),
        Provider("aihorde",    "AI Horde",            requiresKey = false, free = true, getKeyUrl = null),
        Provider("groq",       "Groq",                requiresKey = true,  free = true, getKeyUrl = "https://console.groq.com/keys"),
        Provider("gemini",     "Google AI Studio",    requiresKey = true,  free = true, getKeyUrl = "https://aistudio.google.com/apikey"),
        Provider("openrouter", "OpenRouter",          requiresKey = true,  free = true, getKeyUrl = "https://openrouter.ai/keys"),
        Provider("cerebras",   "Cerebras",            requiresKey = true,  free = true, getKeyUrl = "https://cloud.cerebras.ai"),
        Provider("nvidia",     "NVIDIA NIM",          requiresKey = true,  free = true, getKeyUrl = "https://build.nvidia.com")
    )
}
