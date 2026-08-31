package com.fati_market.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app theme.
 *
 * Both schemes are filled in completely - container and "on" roles included -
 * so components that reach for `surfaceVariant` or `primaryContainer` get a
 * considered colour rather than a Material default that clashes with the brand.
 */

private val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Neutral0,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,

    secondary = Amber600,
    onSecondary = Neutral0,
    secondaryContainer = Amber50,
    onSecondaryContainer = Amber700,

    tertiary = Info,
    onTertiary = Neutral0,
    tertiaryContainer = InfoContainer,
    onTertiaryContainer = Color(0xFF0B3C77),

    background = Neutral50,
    onBackground = Neutral900,

    surface = Neutral0,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,

    // Material 3 tonal surfaces, stepped so stacked cards stay distinguishable.
    surfaceContainerLowest = Neutral0,
    surfaceContainerLow = Neutral50,
    surfaceContainer = Neutral100,
    surfaceContainerHigh = Neutral200,
    surfaceContainerHighest = Neutral300,

    outline = Neutral300,
    outlineVariant = Neutral200,

    error = Danger,
    onError = Neutral0,
    errorContainer = DangerContainer,
    onErrorContainer = Color(0xFF6E1610),

    scrim = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    // The deep brand green is unreadable on a dark ground, so dark mode steps
    // up the ramp rather than reusing the light primary.
    primary = Green300,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,

    secondary = Amber300,
    onSecondary = Color(0xFF3D2900),
    secondaryContainer = Amber700,
    onSecondaryContainer = Amber50,

    tertiary = InfoDark,
    onTertiary = Color(0xFF06305F),
    tertiaryContainer = Color(0xFF11447E),
    onTertiaryContainer = InfoContainer,

    background = Neutral950,
    onBackground = Neutral100,

    surface = Neutral900,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral400,

    surfaceContainerLowest = Neutral950,
    surfaceContainerLow = Neutral900,
    surfaceContainer = Neutral800,
    surfaceContainerHigh = Neutral700,
    surfaceContainerHighest = Neutral600,

    outline = Neutral600,
    outlineVariant = Neutral700,

    error = DangerDark,
    onError = Color(0xFF5C1410),
    errorContainer = Color(0xFF7A241E),
    onErrorContainer = DangerContainer,

    scrim = Color(0xFF000000),
)

/**
 * Corner radii.
 *
 * Generous but not pill-shaped: cards at 18dp read as modern without the
 * content feeling like it is falling off the rounded edges.
 */
val MarketShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

/**
 * Semantic colours that Material's scheme has no slot for.
 *
 * Provided through a CompositionLocal so a status pill can ask for "warning"
 * and get the right value in either theme, instead of every call site
 * hard-coding two hex values and an isSystemInDarkTheme() check.
 */
data class MarketAccents(
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarningContainer: Color,
    val warningContainer: Color,
    val info: Color,
    val onInfoContainer: Color,
    val infoContainer: Color,
    /** Points and rewards are always amber, in both themes. */
    val reward: Color,
    val rewardContainer: Color,
    val onRewardContainer: Color,
)

private val LightAccents = MarketAccents(
    success = Success,
    onSuccessContainer = Color(0xFF14421A),
    successContainer = SuccessContainer,
    warning = Warning,
    onWarningContainer = Color(0xFF6B3A00),
    warningContainer = WarningContainer,
    info = Info,
    onInfoContainer = Color(0xFF0B3C77),
    infoContainer = InfoContainer,
    reward = Amber600,
    rewardContainer = Amber50,
    onRewardContainer = Amber700,
)

private val DarkAccents = MarketAccents(
    success = SuccessDark,
    onSuccessContainer = Color(0xFFBFEFC6),
    successContainer = Color(0xFF1D4522),
    warning = WarningDark,
    onWarningContainer = Color(0xFFFFDFB8),
    warningContainer = Color(0xFF5C3703),
    info = InfoDark,
    onInfoContainer = InfoContainer,
    infoContainer = Color(0xFF11447E),
    reward = Amber300,
    rewardContainer = Color(0xFF4A3403),
    onRewardContainer = Amber100,
)

val LocalMarketAccents = staticCompositionLocalOf { LightAccents }

/** Spacing scale, in multiples of 4dp. */
object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 28.dp
    val xxxl: Dp = 40.dp

    /** The standard horizontal inset for screen content. */
    val screen: Dp = 16.dp
}

@Composable
fun FatiMarketFrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val accents = if (darkTheme) DarkAccents else LightAccents

    CompositionLocalProvider(LocalMarketAccents provides accents) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = MarketShapes,
            content = content,
        )
    }
}

/** Shorthand for the semantic accents inside a composable. */
val MaterialThemeAccents: MarketAccents
    @Composable get() = LocalMarketAccents.current
