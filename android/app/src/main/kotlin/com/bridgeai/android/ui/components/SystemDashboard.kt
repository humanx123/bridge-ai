package com.bridgeai.android.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.StatFs
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bridgeai.android.ui.theme.*
import kotlinx.coroutines.delay

private data class SysStats(
    val storageFreeGb: Double,
    val storageTotalGb: Double,
    val ramFreeGb: Double,
    val ramTotalGb: Double,
    val batteryPct: Int,
    val network: String
)

@Composable
fun SystemDashboard() {
    val ctx = LocalContext.current
    var stats by remember { mutableStateOf(readStats(ctx)) }
    LaunchedEffect(Unit) {
        while (true) { stats = readStats(ctx); delay(5000) }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(BgDeep)
            .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)), RoundedCornerShape(3.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCell("STORAGE", "${"%.1f".format(stats.storageFreeGb)} GB", "de ${"%.1f".format(stats.storageTotalGb)} GB", Modifier.weight(1f))
            StatCell("RAM",     "${"%.1f".format(stats.ramFreeGb)} GB",     "de ${"%.1f".format(stats.ramTotalGb)} GB",    Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCell("BATERÍA", "${stats.batteryPct}%", "nivel", Modifier.weight(1f))
            StatCell("RED",     stats.network,          "conexión", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = TextMuted,  fontSize = 9.sp,  letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = NeonCyan,   fontSize = 18.sp, fontFamily = FontFamily.Monospace)
        Text(sub,   color = TextMuted,  fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

private fun readStats(ctx: Context): SysStats {
    val dataPath = Environment.getDataDirectory()
    val stat = StatFs(dataPath.absolutePath)
    val totalBytes = stat.blockCountLong * stat.blockSizeLong
    val freeBytes  = stat.availableBlocksLong * stat.blockSizeLong

    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

    val bm = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val batt = if (bm != null) {
        val level = bm.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = bm.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    } else -1

    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
    val netLabel = when {
        network == null -> "sin conexión"
        network.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> "Wi-Fi"
        network.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "móvil"
        network.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
        else -> "activa"
    }

    return SysStats(
        storageFreeGb = freeBytes / 1_073_741_824.0,
        storageTotalGb = totalBytes / 1_073_741_824.0,
        ramFreeGb = mi.availMem / 1_073_741_824.0,
        ramTotalGb = mi.totalMem / 1_073_741_824.0,
        batteryPct = batt,
        network = netLabel
    )
}
