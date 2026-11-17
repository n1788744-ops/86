package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Ww1DarkColorScheme = darkColorScheme(
    primary = BrassGold,
    onPrimary = TrenchBlack,
    primaryContainer = BrassMuted,
    onPrimaryContainer = ParchmentIvory,
    secondary = HorizonBlue,
    onSecondary = ParchmentIvory,
    secondaryContainer = SlatePanel,
    onSecondaryContainer = ParchmentIvory,
    tertiary = OliveKhaki,
    onTertiary = ParchmentIvory,
    background = TrenchBlack,
    onBackground = ParchmentIvory,
    surface = GunmetalDark,
    onSurface = ParchmentIvory,
    surfaceVariant = SlatePanel,
    onSurfaceVariant = AshGrey,
    outline = SlateBorder,
    error = CrimsonAlert,
    onError = ParchmentIvory
)

@Composable
fun Ww1Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Ww1DarkColorScheme,
        typography = Ww1Typography,
        content = content
    )
}
