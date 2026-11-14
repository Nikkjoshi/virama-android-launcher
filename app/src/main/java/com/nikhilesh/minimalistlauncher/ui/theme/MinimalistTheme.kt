package com.nikhilesh.minimalistlauncher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherSettings

private val ViramaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val InkColors = darkColorScheme(
    primary = Color(0xFFF3F0E8),
    onPrimary = Color(0xFF0A0A0A),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF3F0E8),
    surface = Color(0xFF171717),
    onSurface = Color(0xFFF3F0E8),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFBDB9B0),
    outline = Color(0xFF6D6962),
)

private val PaperColors = lightColorScheme(
    primary = Color(0xFF24211D),
    onPrimary = Color(0xFFF5F0E7),
    background = Color(0xFFF5F0E7),
    onBackground = Color(0xFF24211D),
    surface = Color(0xFFECE5DA),
    onSurface = Color(0xFF24211D),
    surfaceVariant = Color(0xFFE2DACD),
    onSurfaceVariant = Color(0xFF666057),
    outline = Color(0xFF8D857A),
)

@Composable
fun MinimalistTheme(
    settings: LauncherSettings,
    content: @Composable () -> Unit,
) {
    val scale = settings.textScale.multiplier
    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            fontSize = 68.sp * scale,
            lineHeight = 72.sp * scale,
            letterSpacing = (-2).sp * scale,
            fontFeatureSettings = "tnum",
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            fontSize = 30.sp * scale,
            lineHeight = 36.sp * scale,
            letterSpacing = (-0.4f).sp * scale,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp * scale,
            lineHeight = 26.sp * scale,
            letterSpacing = (-0.1f).sp * scale,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp * scale,
            lineHeight = 25.sp * scale,
            letterSpacing = 0.1f.sp * scale,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp * scale,
            lineHeight = 20.sp * scale,
            letterSpacing = 0.15f.sp * scale,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp * scale,
            lineHeight = 20.sp * scale,
            letterSpacing = 0.2f.sp * scale,
        ),
    )

    MaterialTheme(
        colorScheme = if (settings.palette == LauncherPalette.INK) InkColors else PaperColors,
        typography = typography,
        shapes = ViramaShapes,
        content = content,
    )
}

