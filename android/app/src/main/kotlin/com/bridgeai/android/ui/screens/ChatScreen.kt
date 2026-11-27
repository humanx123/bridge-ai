package com.bridgeai.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bridgeai.android.BridgeAiApp
import com.bridgeai.android.net.ChatMessage
import com.bridgeai.android.ui.Selection
import com.bridgeai.android.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class Line(val kind: Kind, val text: String) {
    enum class Kind { Prompt, Stdout, Stderr, UserChat, AssistantChat, Info, Error }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(app: BridgeAiApp, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by app.liveSettings.collectAsState()
    var input by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<Line>() }
    val listState = rememberLazyListState()
    var sending by remember { mutableStateOf(false) }
    var terminalMode by remember { mutableStateOf(true) }
    var cwd by remember { mutableStateOf(ctx.filesDir.absolutePath) }

    val launchCmd = remember(settings, Selection.agent, Selection.mode) {
        buildLaunchCmd(settings.baseUrl, settings.apiKey, settings.model)
    }

    LaunchedEffect(Unit) {
        if (lines.isEmpty()) {
            lines += Line(Line.Kind.Info, "bridge-ai terminal · sandbox: ${ctx.filesDir.absolutePath}")
            lines += Line(Line.Kind.Info, "═══════════════════════════════════════════")
            lines += Line(Line.Kind.Info, "agente: ${Selection.agent} · modo: ${Selection.mode}")
            lines += Line(Line.Kind.Info, "para abrir el CLI real en Termux → botón [▶ CLI]")
            lines += Line(Line.Kind.Info, "comando que se lanzaría:")
            lines += Line(Line.Kind.Stdout, "  " + launchCmd)
            lines += Line(Line.Kind.Info, "───────────────────────────────────────────")
            lines += Line(Line.Kind.Info, "comandos: ls, pwd  ·  /chat ...  ·  /termux <cmd>  ·  /cli (abre en Termux)  ·  /clear")
        }
    }

    val agentLabel = when (Selection.agent) {
        Selection.Agent.Claude -> "claude"
        Selection.Agent.Codex  -> "codex"
        null -> "—"
    }
    val modeLabel = when (Selection.mode) {
        Selection.Mode.Free    -> "free"
        Selection.Mode.Classic -> "clásico"
        null -> "—"
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "▸ bridge-ai · $agentLabel/$modeLabel",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back", tint = NeonCyan)
                    }
                },
                actions = {
                    TextButton(onClick = { openCli(ctx, launchCmd) { msg -> lines += Line(Line.Kind.Info, msg) } }) {
                        Text("[▶ CLI]", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    TextButton(onClick = { terminalMode = !terminalMode }) {
                        Text(
                            if (terminalMode) "[term]" else "[chat]",
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(BgDeep)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(lines) { l ->
                    val color = when (l.kind) {
                        Line.Kind.Prompt        -> NeonCyan
                        Line.Kind.Stdout        -> TextPrimary
                        Line.Kind.Stderr        -> AccentPink
                        Line.Kind.UserChat      -> NeonCyan
                        Line.Kind.AssistantChat -> NeonBlue
                        Line.Kind.Info          -> TextMuted
                        Line.Kind.Error         -> AccentPink
                    }
                    val prefix = when (l.kind) {
                        Line.Kind.UserChat      -> "? "
                        Line.Kind.AssistantChat -> "~ "
                        Line.Kind.Info          -> "# "
                        Line.Kind.Error         -> "! "
                        else -> ""
                    }
                    Text(
                        "$prefix${l.text}",
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().background(BgSurface).padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (terminalMode) "$ " else "? ",
                    color = if (terminalMode) NeonCyan else NeonBlue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (terminalMode) "ls -la  ·  /chat hola  ·  /termux pkg update"
                            else "pregunta al modelo…",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderLine,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    )
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    enabled = !sending,
                    onClick = {
                        val raw = input.trim()
                        if (raw.isBlank() || sending) return@IconButton
                        input = ""

                        val (action, payload) = when {
                            raw.startsWith("/chat ")   -> "chat"   to raw.removePrefix("/chat ").trim()
                            raw.startsWith("/termux ") -> "termux" to raw.removePrefix("/termux ").trim()
                            raw == "/clear"            -> "clear"  to ""
                            raw == "/cli"              -> "cli"    to ""
                            terminalMode               -> "shell"  to raw
                            else                       -> "chat"   to raw
                        }

                        when (action) {
                            "clear" -> { lines.clear() }
                            "cli" -> { openCli(ctx, launchCmd) { msg -> lines += Line(Line.Kind.Info, msg) } }
                            "termux" -> {
                                lines += Line(Line.Kind.Prompt, "termux$ $payload")
                                try {
                                    val i = Intent("com.termux.RUN_COMMAND").apply {
                                        setClassName("com.termux", "com.termux.app.RunCommandService")
                                        putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh")
                                        putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", payload))
                                        putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
                                    }
                                    ctx.startService(i)
                                    lines += Line(Line.Kind.Info, "enviado a Termux (requiere permiso RUN_COMMAND en Termux:Settings)")
                                } catch (e: Exception) {
                                    lines += Line(Line.Kind.Error, "Termux no disponible: ${e.message}")
                                }
                            }
                            "shell" -> {
                                lines += Line(Line.Kind.Prompt, "$cwd$ $payload")
                                sending = true
                                scope.launch {
                                    runShell(payload, cwd) { newCwd, kind, text ->
                                        if (newCwd != null) cwd = newCwd
                                        if (text != null) lines += Line(kind, text)
                                    }
                                    sending = false
                                    if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
                                }
                            }
                            "chat" -> {
                                lines += Line(Line.Kind.UserChat, payload)
                                val assistantIdx = lines.size
                                lines += Line(Line.Kind.AssistantChat, "")
                                sending = true
                                scope.launch {
                                    val history = mutableListOf<ChatMessage>()
                                    lines.forEach { l ->
                                        when (l.kind) {
                                            Line.Kind.UserChat      -> history += ChatMessage("user", l.text)
                                            Line.Kind.AssistantChat -> if (l.text.isNotEmpty()) history += ChatMessage("assistant", l.text)
                                            else -> {}
                                        }
                                    }
                                    app.client.chatStream(
                                        model = settings.model.takeIf { it != "auto" && it.isNotBlank() } ?: "auto",
                                        messages = history
                                    ).catch { e ->
                                        lines[assistantIdx] = Line(Line.Kind.Error, "error: ${e.message}")
                                    }.collect { chunk ->
                                        val prev = lines[assistantIdx]
                                        lines[assistantIdx] = prev.copy(text = prev.text + chunk)
                                        listState.animateScrollToItem(lines.lastIndex)
                                    }
                                    sending = false
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "send", tint = if (sending) TextDim else NeonCyan)
                }
            }
        }
    }
}

private fun buildLaunchCmd(baseUrl: String, fkey: String, model: String): String {
    val agent = Selection.agent
    val mode  = Selection.mode
    val modelArg = if (model.isNotBlank() && model != "auto") model else "auto"
    return when {
        agent == Selection.Agent.Claude && mode == Selection.Mode.Free ->
            "ANTHROPIC_BASE_URL='$baseUrl' ANTHROPIC_AUTH_TOKEN='${fkey.ifBlank{"<tu-clave-freellmapi>"}}' claude --model '$modelArg'"
        agent == Selection.Agent.Claude && mode == Selection.Mode.Classic ->
            "ANTHROPIC_API_KEY='<tu-clave-anthropic>' claude"
        agent == Selection.Agent.Codex && mode == Selection.Mode.Free ->
            "OPENAI_BASE_URL='${baseUrl.trimEnd('/')}/v1' OPENAI_API_KEY='${fkey.ifBlank{"<tu-clave-freellmapi>"}}' codex"
        agent == Selection.Agent.Codex && mode == Selection.Mode.Classic ->
            "OPENAI_API_KEY='<tu-clave-openai>' codex"
        else -> "bridge-ai"
    }
}

private fun openCli(ctx: Context, cmd: String, log: (String) -> Unit) {
    val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cb.setPrimaryClip(ClipData.newPlainText("bridge-ai cli", cmd))
    log("comando copiado al portapapeles")
    val termux = ctx.packageManager.getLaunchIntentForPackage("com.termux")
    if (termux != null) {
        termux.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(termux)
        log("abriendo Termux → pega (long-press) y pulsa enter")
    } else {
        log("Termux no está instalado. Install: F-Droid → Termux")
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.termux/"))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        } catch (_: Exception) {}
    }
}

private suspend fun runShell(
    cmd: String,
    cwd: String,
    emit: (newCwd: String?, kind: Line.Kind, text: String?) -> Unit
) {
    if (cmd.startsWith("cd ") || cmd == "cd") {
        val target = cmd.removePrefix("cd").trim().ifEmpty { System.getProperty("user.home") ?: cwd }
        val next = if (target.startsWith("/")) File(target) else File(cwd, target)
        if (next.isDirectory) emit(next.canonicalPath, Line.Kind.Stdout, null)
        else emit(null, Line.Kind.Stderr, "cd: no such directory: $target")
        return
    }
    withContext(Dispatchers.IO) {
        try {
            val pb = ProcessBuilder("sh", "-c", cmd)
                .directory(File(cwd))
                .redirectErrorStream(false)
            val p = pb.start()
            p.inputStream.bufferedReader().useLines { seq ->
                seq.forEach { emit(null, Line.Kind.Stdout, it) }
            }
            p.errorStream.bufferedReader().useLines { seq ->
                seq.forEach { emit(null, Line.Kind.Stderr, it) }
            }
            val code = p.waitFor()
            if (code != 0) emit(null, Line.Kind.Info, "[exit $code]")
        } catch (e: Exception) {
            emit(null, Line.Kind.Error, "exec: ${e.message}")
        }
    }
}
