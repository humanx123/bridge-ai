package com.bridgeai.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bridgeai.android.BuildConfig
import com.bridgeai.android.ui.components.CyberBox
import com.bridgeai.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("acerca · donar", color = NeonCyan, style = MaterialTheme.typography.titleMedium) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberBox(title = "bridge-ai") {
                Column {
                    Text("v${BuildConfig.VERSION_NAME}", color = NeonCyan, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Puente en la terminal y en el móvil que conecta Claude Code y Codex " +
                        "con modelos gratuitos vía FreeLLMAPI. Neutral, objetivo, código abierto (MIT).",
                        color = TextPrimary, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            CyberBox(title = "donaciones") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CryptoRow(ctx, "BTC",  "bc1qd9j45f4t0jwyhjhqh2kvz2cr7k8xye460rr2y7")
                    CryptoRow(ctx, "XMR",  "447gTj6Hg6gaAEAUmjqfhqDZr1PziUTvbT4LYLpmLVnTNVFK6cqeqPfh6P4neMKLWX5jDXAr94fWHacJwDvjmCzBBH8wPBt")
                    CryptoRow(ctx, "USDC", "0xAC43f9b0E7F0ae6386cfC8ef6BFf62CE9c3006A1")
                    Text("USDC: solo por la red Arbitrum One. Otra red = fondos perdidos.",
                        color = AccentPink, style = MaterialTheme.typography.bodySmall)
                }
            }

            CyberBox(title = "créditos") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("· FreeLLMAPI — Tashfeen Ahmed (MIT)", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text("· Claude Code — Anthropic",           color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text("· Codex — OpenAI / comunidad",        color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text("· JetBrains Mono — Apache 2.0",       color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CryptoRow(ctx: Context, label: String, addr: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("[$label]", color = NeonCyan, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
        Text(addr, color = TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        IconButton(onClick = {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, addr))
            Toast.makeText(ctx, "$label copiada", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.Default.ContentCopy, contentDescription = "copy", tint = NeonCyan)
        }
    }
}
