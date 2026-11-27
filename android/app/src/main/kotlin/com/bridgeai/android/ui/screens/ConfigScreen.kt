package com.bridgeai.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bridgeai.android.BridgeAiApp
import com.bridgeai.android.ui.components.CyberBox
import com.bridgeai.android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(app: BridgeAiApp, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val settings by app.liveSettings.collectAsState()
    var baseUrl by remember(settings) { mutableStateOf(settings.baseUrl) }
    var apiKey  by remember(settings) { mutableStateOf(settings.apiKey) }
    var model   by remember(settings) { mutableStateOf(settings.model) }
    var workspace by remember(settings) { mutableStateOf(settings.workspace) }

    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("configuración", color = NeonCyan, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CyberBox(title = "servidor") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("URL de FreeLLMAPI", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Text("Ej.: http://127.0.0.1:31415 (Termux) o http://127.0.0.1:3001 (app escritorio)",
                        color = TextDim, style = MaterialTheme.typography.bodySmall)
                }
            }

            CyberBox(title = "clave unificada") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("API key (freellmapi-…)", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                }
            }

            CyberBox(title = "modelo por defecto") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        singleLine = true,
                        placeholder = { Text("auto", color = TextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Text("«auto» = el gateway elige y cambia si uno falla.",
                        color = TextDim, style = MaterialTheme.typography.bodySmall)
                }
            }

            CyberBox(title = "workspace") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ruta de tu proyecto (ej.: /sdcard/mis-repos/app)", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = workspace,
                        onValueChange = { workspace = it },
                        singleLine = true,
                        placeholder = { Text("(vacío = sandbox de la app)", color = TextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Text("Se usa como directorio de trabajo en el chat/terminal y como cwd del CLI.",
                        color = TextDim, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        app.settings.update { copy(baseUrl = baseUrl.trim(), apiKey = apiKey.trim(), model = model.trim().ifBlank { "auto" }, workspace = workspace.trim()) }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BgDeep)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = BgDeep)
                Spacer(Modifier.width(8.dp))
                Text("GUARDAR", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = BorderLine,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonCyan
)
