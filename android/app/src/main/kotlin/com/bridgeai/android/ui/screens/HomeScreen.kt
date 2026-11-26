package com.bridgeai.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bridgeai.android.ui.Route
import com.bridgeai.android.ui.Selection
import com.bridgeai.android.ui.components.StatusDot
import com.bridgeai.android.ui.theme.BgDeep
import com.bridgeai.android.ui.theme.BgSurface
import com.bridgeai.android.ui.theme.BorderLine
import com.bridgeai.android.ui.theme.NeonCyan
import com.bridgeai.android.ui.theme.TextMuted
import com.bridgeai.android.ui.theme.TextPrimary

private val ClaudeOrange = Color(0xFFD97757)
private val CodexGreen   = Color(0xFF10A37F)

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
            Text("bridge-ai", style = MaterialTheme.typography.displayLarge, color = NeonCyan)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("//", color = NeonCyan.copy(alpha = 0.55f), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(6.dp))
                Text(
                    "elige agente → elige modo → chatea",
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

            Spacer(Modifier.height(12.dp))
            Text("» AGENTE", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AgentCard(
                    glyph = "Ⓒ",
                    glyphColor = ClaudeOrange,
                    name = "Claude",
                    sub = "Claude Code · Anthropic",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Selection.agent = Selection.Agent.Claude
                        onNavigate(Route.Mode)
                    }
                )
                AgentCard(
                    glyph = "⎔",
                    glyphColor = CodexGreen,
                    name = "Codex",
                    sub = "Codex CLI · OpenAI",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Selection.agent = Selection.Agent.Codex
                        onNavigate(Route.Mode)
                    }
                )
            }

            Spacer(Modifier.height(18.dp))
            Text("» ATAJOS", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShortcutButton("proveedores", Modifier.weight(1f)) { onNavigate(Route.Providers) }
                ShortcutButton("estado",      Modifier.weight(1f)) { onNavigate(Route.Status) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShortcutButton("configurar",  Modifier.weight(1f)) { onNavigate(Route.Config) }
                ShortcutButton("acerca",      Modifier.weight(1f)) { onNavigate(Route.About) }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "FreeLLMAPI corre en tu dispositivo (127.0.0.1). Modo clásico usa tu cuenta Anthropic u OpenAI.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AgentCard(
    glyph: String,
    glyphColor: Color,
    name: String,
    sub: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .background(BgDeep)
            .border(BorderStroke(1.dp, BorderLine), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = glyphColor, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(name, color = TextPrimary, fontSize = 16.sp, letterSpacing = 1.sp)
        Text(sub, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ShortcutButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, NeonCyan),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
    ) {
        Text(label, fontSize = 12.sp, letterSpacing = 1.sp)
    }
}
