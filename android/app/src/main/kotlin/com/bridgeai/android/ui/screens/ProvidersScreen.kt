package com.bridgeai.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bridgeai.android.data.Providers
import com.bridgeai.android.ui.components.RoundedCyberBox
import com.bridgeai.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = BgSurface,
        topBar = {
            TopAppBar(
                title = { Text("proveedores", color = NeonCyan, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { inner ->
        LazyColumn(
            Modifier.padding(inner).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(Providers.all) { p ->
                RoundedCyberBox(borderColor = (if (p.requiresKey) NeonBlue else NeonCyan).copy(alpha = 0.4f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (p.requiresKey) "requiere clave · gratis" else "sin clave · activo por defecto",
                                color = if (p.requiresKey) NeonBlue else NeonCyan,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (p.getKeyUrl != null) {
                            IconButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.getKeyUrl)))
                            }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "obtener clave", tint = NeonCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
