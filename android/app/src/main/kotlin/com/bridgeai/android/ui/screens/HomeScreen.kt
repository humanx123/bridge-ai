package com.bridgeai.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bridgeai.android.ui.Route
import com.bridgeai.android.ui.components.CyberMenuButton
import com.bridgeai.android.ui.components.StatusDot
import com.bridgeai.android.ui.theme.BgSurface
import com.bridgeai.android.ui.theme.NeonCyan
import com.bridgeai.android.ui.theme.TextMuted

@Composable
fun HomeScreen(onNavigate: (Route) -> Unit) {
    Scaffold(containerColor = BgSurface) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "bridge-ai",
                style = MaterialTheme.typography.displayLarge,
                color = NeonCyan
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("//", color = NeonCyan.copy(alpha = 0.55f), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Claude · Codex · modelos gratis · F-Droid",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusDot(ok = true,  label = "FreeLLMAPI")
                StatusDot(ok = false, label = "proveedores")
                StatusDot(ok = true,  label = "red local")
            }
            Spacer(Modifier.height(10.dp))

            CyberMenuButton("1", "Chat",         "conversación con modelos gratuitos",            { onNavigate(Route.Chat) })
            CyberMenuButton("2", "Modelos",      "ver y probar modelos del servidor",             { onNavigate(Route.Status) })
            CyberMenuButton("3", "Configuración","URL, clave, modelo por defecto",                { onNavigate(Route.Config) })
            CyberMenuButton("4", "Proveedores",  "activa gratuitos, añade claves",                { onNavigate(Route.Providers) })
            CyberMenuButton("5", "Estado",       "ping, uptime, modelos disponibles",             { onNavigate(Route.Status) })
            CyberMenuButton("6", "Acerca · Donar","créditos, enlaces, apoyo al proyecto",          { onNavigate(Route.About) })

            Spacer(Modifier.height(16.dp))
            Text(
                "> conectado a FreeLLMAPI local  ·  sin cuentas en la nube  ·  tus claves cifradas en tu dispositivo",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
