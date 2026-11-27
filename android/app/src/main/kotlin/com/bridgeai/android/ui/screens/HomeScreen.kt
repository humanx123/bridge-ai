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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bridgeai.android.ui.Route
import com.bridgeai.android.ui.Selection
import com.bridgeai.android.ui.components.StatusDot
import com.bridgeai.android.ui.components.SystemDashboard
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
    val ctx = LocalContext.current
    val app = (ctx.applicationContext as com.bridgeai.android.BridgeAiApp)
    val settings by app.liveSettings.collectAsState()
    val termuxInstalled = remember { isPackageInstalled(ctx, "com.termux") }
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
                StatusDot(ok = true,            label = "FreeLLMAPI")
                StatusDot(ok = termuxInstalled, label = "Termux")
                StatusDot(ok = true,            label = "red")
            }

            Spacer(Modifier.height(6.dp))
            SystemDashboard()

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("» WORKSPACE", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                Text(
                    if (settings.workspace.isBlank()) "sandbox (app)" else settings.workspace,
                    color = NeonCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onNavigate(Route.Config) }
                )
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

            Spacer(Modifier.height(18.dp))
            Text("» LABORATORIO (requiere Termux)", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)
            if (!termuxInstalled) {
                Text(
                    "Termux no está instalado. Instálalo desde F-Droid para usar los CLIs reales y distros.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShortcutButton("instalar Termux (F-Droid)", Modifier.weight(1f)) { openUrl(ctx, "https://f-droid.org/packages/com.termux/") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShortcutButton("Debian",  Modifier.weight(1f)) { runInTermux(ctx, "pkg install -y proot-distro && proot-distro install debian && proot-distro login debian") }
                    ShortcutButton("Ubuntu",  Modifier.weight(1f)) { runInTermux(ctx, "pkg install -y proot-distro && proot-distro install ubuntu && proot-distro login ubuntu") }
                    ShortcutButton("Alpine",  Modifier.weight(1f)) { runInTermux(ctx, "pkg install -y proot-distro && proot-distro install alpine && proot-distro login alpine") }
                }
                Text(
                    "Alpine es la más liviana (~50 MB); Debian/Ubuntu dan entorno completo (~500 MB).",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))
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

private fun isPackageInstalled(ctx: Context, pkg: String): Boolean = try {
    ctx.packageManager.getPackageInfo(pkg, 0); true
} catch (_: Exception) { false }

private fun openUrl(ctx: Context, url: String) {
    try {
        val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    } catch (_: Exception) {}
}

private fun runInTermux(ctx: Context, cmd: String) {
    val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cb.setPrimaryClip(ClipData.newPlainText("bridge-ai cmd", cmd))
    Toast.makeText(ctx, "comando copiado · pégalo en Termux", Toast.LENGTH_LONG).show()
    val i = ctx.packageManager.getLaunchIntentForPackage("com.termux")
    i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try { i?.let { ctx.startActivity(it) } } catch (_: Exception) {}
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
