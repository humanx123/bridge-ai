package com.bridgeai.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bridgeai.android.ui.theme.BgDeep
import com.bridgeai.android.ui.theme.NeonCyan
import com.bridgeai.android.ui.theme.TextPrimary

@Composable
fun CyberMenuButton(
    index: String,
    label: String,
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = NeonCyan
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BgDeep,
            contentColor   = TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = "[$index]",
                color = accent,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(hint,  style = MaterialTheme.typography.bodySmall,   color = accent.copy(alpha = 0.6f))
            }
            Text("›", color = accent, style = MaterialTheme.typography.titleLarge)
        }
    }
}
