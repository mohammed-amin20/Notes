package com.mohammed.notes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Color(0xff111111),
    onSecondary = Color(0xFFEEEDED),
    surface = Color(0xFF2D2D2D),
    onSurface = Color.White,
    tertiary = Color(0x2AFFFFFF),
    onTertiary = Color(0x6BFFFFFF),
    background  = Color.Black ,
    onBackground  = Color.White ,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Color(0xFFFAF7F1),
    onSecondary = Color(0xF8313131),
    surface = Color(0xffffffff),
    onSurface = Color(0xFF242424),
    tertiary = Color.White,
    onTertiary = Color(0x80000000),
    background = Color(0xFFF3F3F3),
    onBackground  = Color.Black

)

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}