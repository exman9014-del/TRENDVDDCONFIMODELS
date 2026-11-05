package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = PrimaryGreenDark,
    secondary = PrimaryGreenDark,
    onSecondary = Color.White,
    secondaryContainer = MintSoft,
    onSecondaryContainer = PrimaryGreenDark,
    tertiary = AuctionGold,
    onTertiary = Color.White,
    tertiaryContainer = AuctionGoldLight,
    onTertiaryContainer = AuctionGold,
    error = CoralRed,
    onError = Color.White,
    errorContainer = CoralRedLight,
    onErrorContainer = CoralRedDark,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = MintBorder
)

@Composable
fun NaVerdadeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
