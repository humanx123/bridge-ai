package com.bridgeai.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bridgeai.android.ui.theme.BgElevated
import com.bridgeai.android.ui.theme.NeonCyan
import com.bridgeai.android.ui.theme.TextMuted

/**
 * ASCII-box framing like:
 *   ╭─ title ─────────╮
 *   │  content        │
 *   ╰─────────────────╯
 */
@Composable
fun CyberBox(
    title: String?,
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = "╭─ $title ".padEnd(48, '─') + "╮",
                color = borderColor,
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            Text("╭" + "─".repeat(47) + "╮", color = borderColor, style = MaterialTheme.typography.labelMedium)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(BgElevated)
                .border(BorderStroke(1.dp, borderColor.copy(alpha = 0.35f)))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            content()
        }
        Text("╰" + "─".repeat(47) + "╯", color = borderColor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StatusDot(ok: Boolean, label: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(
            text = if (ok) "●" else "○",
            color = if (ok) NeonCyan else TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RoundedCyberBox(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan.copy(alpha = 0.4f),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(BgElevated, RoundedCornerShape(2.dp))
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .padding(14.dp),
        content = content
    )
}
