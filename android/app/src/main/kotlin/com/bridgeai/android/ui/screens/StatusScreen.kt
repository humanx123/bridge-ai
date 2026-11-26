package com.bridgeai.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bridgeai.android.BridgeAiApp
import com.bridgeai.android.net.ModelInfo
import com.bridgeai.android.ui.components.CyberBox
import com.bridgeai.android.ui.components.StatusDot
import com.bridgeai.android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(app: BridgeAiApp, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val settings by app.liveSettings.collectAsState()
    var online by remember { mutableStateOf<Boolean?>(null) }
    var models by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun refresh() {
        loading = true
        error = null
        scope.launch {
            app.client.ping().onSuccess { online = true }.onFailure { online = false; error = it.message }
            app.client.models().onSuccess { models = it }.onFailure { error = it.message }
            loading = false
        }
    }
    LaunchedEffect(settings.baseUrl, settings.apiKey) { refresh() }

    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("estado", color = NeonCyan, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = NeonCyan)
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "refresh", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberBox(title = "servidor") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("URL  ${settings.baseUrl}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text("key  ${if (settings.apiKey.isBlank()) "(no definida)" else settings.apiKey.take(14) + "…"}",
                        color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    StatusDot(ok = online == true, label = when (online) {
                        null  -> if (loading) "comprobando…" else "sin probar"
                        true  -> "en línea"
                        false -> "sin conexión"
                    })
                    error?.let { Text("⚠︎ $it", color = AccentPink, style = MaterialTheme.typography.bodySmall) }
                }
            }

            CyberBox(title = "modelos (${models.size})") {
                if (models.isEmpty()) {
                    Text(if (loading) "cargando…" else "sin modelos todavía", color = TextMuted,
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(
                        Modifier.heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(models) { m ->
                            Text("• ${m.id}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            m.owned_by?.let {
                                Text("    $it", color = TextDim, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
