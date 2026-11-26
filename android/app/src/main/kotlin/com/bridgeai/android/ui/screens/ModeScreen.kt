package com.bridgeai.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bridgeai.android.ui.Route
import com.bridgeai.android.ui.Selection
import com.bridgeai.android.ui.theme.BgDeep
import com.bridgeai.android.ui.theme.BgSurface
import com.bridgeai.android.ui.theme.BorderLine
import com.bridgeai.android.ui.theme.NeonBlue
import com.bridgeai.android.ui.theme.NeonCyan
import com.bridgeai.android.ui.theme.TextMuted
import com.bridgeai.android.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeScreen(onBack: () -> Unit, onNavigate: (Route) -> Unit) {
    val agentName = when (Selection.agent) {
        Selection.Agent.Claude -> "Claude"
        Selection.Agent.Codex  -> "Codex"
        null -> "—"
    }
    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("modo · $agentName", color = NeonCyan, fontSize = 14.sp, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "atrás", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                pin = "⚡",
                pinColor = NeonCyan,
                title = "FreeLLMAPI · modelos gratuitos",
                desc = "Enruta a decenas de proveedores (Kilo, Groq, Gemini, OVH…) con una sola clave unificada. Sin cuenta en la nube."
            ) {
                Selection.mode = Selection.Mode.Free
                onNavigate(Route.Chat)
            }
            ModeCard(
                pin = "◉",
                pinColor = NeonBlue,
                title = "Cuenta clásica",
                desc = "API directa del proveedor con tu propia clave (${if (Selection.agent == Selection.Agent.Claude) "Anthropic" else "OpenAI"})."
            ) {
                Selection.mode = Selection.Mode.Classic
                onNavigate(Route.Chat)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Configurar claves y modelos en [Atajos → configurar].",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ModeCard(
    pin: String,
    pinColor: Color,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BgDeep)
            .border(BorderStroke(1.dp, BorderLine), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .border(BorderStroke(1.dp, pinColor), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(pin, color = pinColor, fontSize = 20.sp) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(desc,  color = TextMuted,   fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text("›", color = NeonCyan, fontSize = 22.sp)
    }
}
