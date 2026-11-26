package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ArrowLightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = PureWhite,
    primaryContainer = IceBlueContainer,
    onPrimaryContainer = CobaltDark,
    secondary = SkyCyan,
    onSecondary = PureWhite,
    secondaryContainer = SkyCyanContainer,
    onSecondaryContainer = CobaltDark,
    tertiary = GoldAccent,
    onTertiary = InkNavy,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = InkNavy,
    background = PureWhite,
    onBackground = InkNavy,
    surface = IceSurface,
    onSurface = InkNavy,
    surfaceVariant = IceBlueContainer,
    onSurfaceVariant = SlateText,
    outline = SoftBorderBlue,
    error = HeartCrimson,
    onError = PureWhite,
    errorContainer = HeartCrimsonLight,
    onErrorContainer = HeartCrimson,
)

private val ArrowDarkColorScheme = darkColorScheme(
    primary = AzureLight,
    onPrimary = DarkBg,
    primaryContainer = CobaltDark,
    onPrimaryContainer = IceBlueContainer,
    secondary = SkyCyan,
    onSecondary = PureWhite,
    tertiary = GoldAccent,
    onTertiary = InkNavy,
    background = DarkBg,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = AzureLight,
    outline = DarkBorder,
    error = HeartCrimson,
    onError = PureWhite,
)

val ArrowShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ArrowPuzzleTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ArrowDarkColorScheme else ArrowLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArrowTypography,
        shapes = ArrowShapes,
        content = content
    )
}
