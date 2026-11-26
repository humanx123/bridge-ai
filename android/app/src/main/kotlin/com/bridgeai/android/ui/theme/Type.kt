package com.bridgeai.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * System monospace is used so we ship no font blobs (F-Droid friendly, smaller APK).
 * On most Android devices this maps to Roboto Mono or Droid Sans Mono.
 */
private val Mono: FontFamily = FontFamily.Monospace

val BridgeTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold,   fontSize = 32.sp, letterSpacing = 2.sp),
    displayMedium = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold,   fontSize = 24.sp, letterSpacing = 1.5.sp),
    titleLarge    = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = 1.sp),
    titleMedium   = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 15.sp, letterSpacing = 0.8.sp),
    bodyLarge     = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Normal, fontSize = 15.sp, letterSpacing = 0.3.sp),
    bodyMedium    = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Normal, fontSize = 13.sp, letterSpacing = 0.3.sp),
    bodySmall     = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelLarge    = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 1.sp),
    labelMedium   = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.sp),
    labelSmall    = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 1.2.sp)
)
