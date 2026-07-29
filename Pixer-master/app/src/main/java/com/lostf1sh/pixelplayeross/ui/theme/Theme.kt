package com.lostf1sh.pixelplayeross.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lostf1sh.pixelplayeross.presentation.viewmodel.ColorSchemePair
import androidx.core.graphics.ColorUtils

val LocalPixelPlayerDarkTheme = staticCompositionLocalOf { false }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Suppress("DEPRECATION")
@Composable
fun PixelPlayerStatusBarStyle(
    color: Color,
    useDarkIcons: Boolean = ColorUtils.calculateLuminance(color.toArgb()) > 0.55,
    navigationColor: Color? = null,
    useDarkNavigationIcons: Boolean = navigationColor
        ?.let { ColorUtils.calculateLuminance(it.toArgb()) > 0.55 }
        ?: useDarkIcons
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val updateNavigationBar = navigationColor != null
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
        }

        WindowCompat.getInsetsController(window, view).run {
            isAppearanceLightStatusBars = useDarkIcons

            if (updateNavigationBar) {
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                isAppearanceLightNavigationBars = useDarkNavigationIcons
            }
        }
    }
}

val DarkColorScheme = darkColorScheme(
    primary = PixelPlayerPurplePrimary,
    secondary = PixelPlayerPink,
    tertiary = PixelPlayerOrange,
    background = PixelPlayerPurpleDark,
    surface = PixelPlayerSurface,
    onPrimary = PixelPlayerWhite,
    onSecondary = PixelPlayerWhite,
    onTertiary = PixelPlayerWhite,
    onBackground = PixelPlayerWhite,
    onSurface = PixelPlayerLightPurple, // Text on surfaces
    error = Color(0xFFFF5252),
    onError = PixelPlayerWhite
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = PixelPlayerWhite,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = PixelPlayerPink,
    onSecondary = PixelPlayerWhite,
    secondaryContainer = PixelPlayerPink.copy(alpha = 0.15f),
    onSecondaryContainer = PixelPlayerPink.copy(alpha = 0.85f),
    tertiary = PixelPlayerOrange,
    onTertiary = PixelPlayerBlack,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.6f),
    surfaceTint = LightPrimary,
    error = Color(0xFFD32F2F),
    onError = PixelPlayerWhite
)

// Deezer skin — dark-only, so it only ever needs one scheme (no light variant).
// Fixed-role and surfaceContainer* tiers are set explicitly: Compose Material3 does NOT derive
// them from primary/secondary/tertiary, and several live surfaces (FullPlayer's play/pause
// button, most Card/Surface backgrounds) read those roles directly.
val DeezerColorScheme = darkColorScheme(
    primary = DeezerViolet, onPrimary = DeezerWhite,
    primaryContainer = DeezerVioletContainer, onPrimaryContainer = DeezerWhite,
    secondary = DeezerViolet, onSecondary = DeezerWhite,
    secondaryContainer = DeezerSurfaceElevated, onSecondaryContainer = DeezerWhite,
    tertiary = DeezerWhite, onTertiary = DeezerBlack,
    background = DeezerBlack, onBackground = DeezerWhite,
    surface = DeezerBlack, onSurface = DeezerWhite,
    surfaceVariant = DeezerSurfaceElevated, onSurfaceVariant = DeezerGrey,
    outline = DeezerGrey.copy(alpha = 0.4f), outlineVariant = DeezerGrey.copy(alpha = 0.2f),
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DeezerBlack, surfaceContainerLow = DeezerSurfaceLow,
    surfaceContainer = DeezerSurfaceElevated, surfaceContainerHigh = DeezerSurfaceHigh,
    surfaceContainerHighest = DeezerSurfaceHighest,
    surfaceBright = DeezerSurfaceHigh, surfaceDim = DeezerBlack,
    primaryFixed = DeezerVioletContainer, primaryFixedDim = DeezerViolet,
    onPrimaryFixed = DeezerWhite, onPrimaryFixedVariant = DeezerWhite,
    secondaryFixed = DeezerSurfaceElevated, secondaryFixedDim = DeezerSurfaceHigh,
    onSecondaryFixed = DeezerWhite, onSecondaryFixedVariant = DeezerWhite,
    // White/black, not violet: this pair drives the FullPlayer's play/pause button
    // (expressivePlayPauseButtonColors), and real Deezer's play button is a plain white
    // circle with a black glyph, not an accent-colored one.
    tertiaryFixed = DeezerWhite, tertiaryFixedDim = DeezerWhite,
    onTertiaryFixed = DeezerBlack, onTertiaryFixedVariant = DeezerBlack,
    error = Color(0xFFFF5252), onError = DeezerWhite
)

@Composable
fun PixelPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    skin: AppSkin = AppSkin.DEFAULT,
    colorSchemePairOverride: ColorSchemePair? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val finalColorScheme = when {
        // Takes priority over dynamic color: without this branch first, Material You would
        // win on any Android 12+ device and the Deezer skin would silently never apply there.
        skin == AppSkin.DEEZER -> DeezerColorScheme
        colorSchemePairOverride == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // System dynamic theme as priority if there is no override
            try {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } catch (e: Exception) {
                // Fall back to the defaults if dynamic colors fail (rare, but possible on some devices)
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        colorSchemePairOverride != null -> {
            // Use the album scheme if one is provided
            if (darkTheme) colorSchemePairOverride.dark else colorSchemePairOverride.light
        }
        // Final fallback to the defaults if there is no override or applicable dynamic colors
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val finalTypography = if (skin == AppSkin.DEEZER) DeezerTypography else Typography

    PixelPlayerStatusBarStyle(
        color = finalColorScheme.background,
        navigationColor = finalColorScheme.background
    )

    CompositionLocalProvider(LocalPixelPlayerDarkTheme provides darkTheme, LocalAppSkin provides skin) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = finalTypography,
            shapes = Shapes,
            content = content
        )
    }
}
