package com.example.sqlite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AzulPetroleoClaro,
    onPrimary = GrisOscuro,
    primaryContainer = Color(0xFF004A66),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFB74D),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPetroleo,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = GrisOscuro,
    secondary = Ambar,
    background = GrisFondo,
    surface = Color.White,
    onBackground = GrisOscuro,
    onSurface = GrisOscuro,
    error = RojoAlerta
)

@Composable
fun SqliteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Se usa una paleta propia (sin color dinámico) para que se vea igual en todos los equipos
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
