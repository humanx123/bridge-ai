package com.bridgeai.android

import android.app.Application
import com.bridgeai.android.data.SettingsRepository
import com.bridgeai.android.net.FreeLlmApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BridgeAiApp : Application() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    lateinit var settings: SettingsRepository
        private set

    val liveSettings = MutableStateFlow(com.bridgeai.android.data.BridgeSettings())

    val client by lazy {
        FreeLlmApiClient(
            baseUrlProvider = { liveSettings.value.baseUrl },
            apiKeyProvider  = { liveSettings.value.apiKey.takeIf { it.isNotBlank() } }
        )
    }

    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        scope.launch {
            liveSettings.value = settings.flow.first()
            settings.flow.collect { liveSettings.value = it }
        }
    }
}
