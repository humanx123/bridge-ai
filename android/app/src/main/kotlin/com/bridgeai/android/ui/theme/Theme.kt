package com.bridgeai.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val BridgeDarkColors = darkColorScheme(
    primary            = NeonCyan,
    onPrimary          = BgDeep,
    primaryContainer   = NeonBlueDeep,
    onPrimaryContainer = NeonCyan,
    secondary          = NeonBlue,
    onSecondary        = BgDeep,
    tertiary           = AccentLime,
    onTertiary         = BgDeep,
    background         = BgSurface,
    onBackground       = TextPrimary,
    surface            = BgSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = BgElevated,
    onSurfaceVariant   = TextMuted,
    outline            = BorderLine,
    outlineVariant     = TextDim,
    error              = AccentPink,
    onError            = BgDeep
)

@Composable
fun BridgeAiTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dark-only — cyberpunk aesthetic is monochrome dark.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDeep.toArgb()
            window.navigationBarColor = BgDeep.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = BridgeDarkColors,
        typography  = BridgeTypography,
        content     = content
    )
}
