package it.livasodv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette ricavata direttamente da ProfessionalTheme.swift / RootView.swift (Build Apple 31).
val LivasBackground = Color(0xFF090C0F)       // Color(0.035, 0.045, 0.060)
val LivasBackgroundDeep = Color(0xFF05070D)   // WowGlowBackground top
val LivasSurface = Color(0xFF17191D)          // white ~5.5% sopra il fondo scuro
val LivasSurfaceStrong = Color(0xFF202329)    // white ~8.5%
val LivasLine = Color(0xFF2A2C31)             // white ~10%
val LivasAccent = Color(0xFFD11C14)           // Color(0.82, 0.11, 0.08)
val LivasWarmAccent = Color(0xFFF25C1F)       // Color(0.95, 0.36, 0.12)

// Alias mantenuti per compatibilità con le schermate esistenti.
val LivasNavy = LivasAccent
val LivasNavyDark = Color(0xFF7A0004)
val LivasRed = LivasAccent
val LivasOrange = LivasWarmAccent
val LivasGreen = Color(0xFF30D158)
val LivasBlue = Color(0xFF0A84FF)
val LivasPurple = Color(0xFFBF5AF2)
val LivasText = Color(0xFFF5F5F7)
val LivasMuted = Color(0xFF91949B)

private val Scheme = darkColorScheme(
    primary = LivasAccent,
    onPrimary = Color.White,
    secondary = LivasWarmAccent,
    onSecondary = Color.White,
    tertiary = LivasGreen,
    background = LivasBackground,
    onBackground = LivasText,
    surface = LivasSurface,
    onSurface = LivasText,
    surfaceVariant = LivasSurfaceStrong,
    onSurfaceVariant = Color(0xFFC7C8CC),
    outline = LivasLine,
    error = Color(0xFFFF453A),
    onError = Color.White
)

@Composable
fun LivasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
