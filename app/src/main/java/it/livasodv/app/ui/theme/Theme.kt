package it.livasodv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LivasNavy = Color(0xFF073A88)
val LivasNavyDark = Color(0xFF052A63)
val LivasBackground = Color(0xFFF6F7FA)
val LivasSurface = Color(0xFFFFFFFF)
val LivasSurfaceStrong = Color(0xFFF0F3F8)
val LivasLine = Color(0xFFD9DEE8)
val LivasRed = Color(0xFFD71920)
val LivasOrange = Color(0xFFF28C18)
val LivasGreen = Color(0xFF159447)
val LivasBlue = Color(0xFF0B63CE)
val LivasPurple = Color(0xFF7247C8)
val LivasText = Color(0xFF111827)
val LivasMuted = Color(0xFF6B7280)

private val Scheme = lightColorScheme(
    primary = LivasNavy,
    onPrimary = Color.White,
    secondary = LivasBlue,
    onSecondary = Color.White,
    tertiary = LivasGreen,
    background = LivasBackground,
    onBackground = LivasText,
    surface = LivasSurface,
    onSurface = LivasText,
    surfaceVariant = LivasSurfaceStrong,
    onSurfaceVariant = Color(0xFF4B5563),
    outline = LivasLine,
    error = LivasRed,
    onError = Color.White
)

@Composable
fun LivasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
