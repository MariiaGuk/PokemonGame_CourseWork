package com.example.chimeralis.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val ChimeralisColorScheme = darkColorScheme(
    primary = Gold,
    secondary = OrangeBright,
    tertiary = OrangeRed,
    background = DarkBrown,
    surface = DarkBurgundy,
    onPrimary = DarkBrown,
    onSecondary = DarkBrown,
    onBackground = Gold,
    onSurface = Gold,
)

/**
 * Renders the chimeralis theme UI.
 *
 * @param content Composable content rendered inside this component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
fun ChimeralisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChimeralisColorScheme,
        typography = ChimeralisTypography,
        content = content
    )
}