package com.bridgeai.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bridgeai.android.BridgeAiApp
import com.bridgeai.android.net.ChatMessage
import com.bridgeai.android.ui.components.RoundedCyberBox
import com.bridgeai.android.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private data class UiMsg(val role: String, var text: String, val streaming: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(app: BridgeAiApp, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val settings by app.liveSettings.collectAsState()
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<UiMsg>() }
    val listState = rememberLazyListState()
    var sending by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("chat ░ ${settings.model}", color = NeonCyan, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { m ->
                    val color = if (m.role == "user") NeonCyan.copy(alpha = 0.5f) else NeonBlue.copy(alpha = 0.4f)
                    RoundedCyberBox(borderColor = color) {
                        Text(
                            text = if (m.role == "user") "> you" else "~ assistant",
                            color = color,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(m.text, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("mensaje…", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    singleLine = false,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderLine,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isBlank() || sending) return@IconButton
                        val userText = input.trim()
                        input = ""
                        messages += UiMsg("user", userText)
                        val assistantIdx = messages.size
                        messages += UiMsg("assistant", "", streaming = true)
                        sending = true
                        scope.launch {
                            val history = messages.filter { it.text.isNotEmpty() }.map { ChatMessage(it.role, it.text) }
                            app.client.chatStream(
                                model = settings.model.takeIf { it != "auto" && it.isNotBlank() } ?: "auto",
                                messages = history
                            ).catch { e ->
                                messages[assistantIdx] = UiMsg("assistant", "⚠︎ error: ${e.message}")
                            }.collect { chunk ->
                                val prev = messages[assistantIdx]
                                messages[assistantIdx] = prev.copy(text = prev.text + chunk)
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                            sending = false
                        }
                    },
                    enabled = !sending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "send", tint = if (sending) TextDim else NeonCyan)
                }
            }
        }
    }
}
