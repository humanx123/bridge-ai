package com.bridgeai.android.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bridge_ai_settings")

object SettingsKeys {
    val BASE_URL      = stringPreferencesKey("base_url")
    val API_KEY       = stringPreferencesKey("api_key")
    val DEFAULT_MODEL = stringPreferencesKey("default_model")
    val TEMPERATURE   = stringPreferencesKey("temperature")
}

data class BridgeSettings(
    val baseUrl: String      = "http://127.0.0.1:31415",
    val apiKey:  String      = "",
    val model:   String      = "auto",
    val temperature: Double? = null
)

class SettingsRepository(private val context: Context) {

    val flow: Flow<BridgeSettings> = context.dataStore.data.map { p ->
        BridgeSettings(
            baseUrl = p[SettingsKeys.BASE_URL] ?: "http://127.0.0.1:31415",
            apiKey  = p[SettingsKeys.API_KEY]  ?: "",
            model   = p[SettingsKeys.DEFAULT_MODEL] ?: "auto",
            temperature = p[SettingsKeys.TEMPERATURE]?.toDoubleOrNull()
        )
    }

    suspend fun update(block: BridgeSettings.() -> BridgeSettings) {
        context.dataStore.edit { prefs ->
            val current = BridgeSettings(
                baseUrl = prefs[SettingsKeys.BASE_URL] ?: "http://127.0.0.1:31415",
                apiKey  = prefs[SettingsKeys.API_KEY]  ?: "",
                model   = prefs[SettingsKeys.DEFAULT_MODEL] ?: "auto",
                temperature = prefs[SettingsKeys.TEMPERATURE]?.toDoubleOrNull()
            ).block()
            prefs[SettingsKeys.BASE_URL] = current.baseUrl
            prefs[SettingsKeys.API_KEY]  = current.apiKey
            prefs[SettingsKeys.DEFAULT_MODEL] = current.model
            current.temperature?.let { prefs[SettingsKeys.TEMPERATURE] = it.toString() }
                ?: prefs.remove(SettingsKeys.TEMPERATURE)
        }
    }
}
