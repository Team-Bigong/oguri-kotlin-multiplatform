package com.bigong.oguri.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object OguriTheme {
    val typography: OguriTypographySystem
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
    val colors: OguriColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalColorPalette.current
}

private val LocalTypography =
    staticCompositionLocalOf<OguriTypographySystem> { error("No typography provided") }
private val LocalColorPalette =
    staticCompositionLocalOf<OguriColorPalette> { error("No colors provided") }

val LightOguriMaterialColorScheme =
    lightColorScheme(
        primary = LightOguriColorPalette.mint,
        onPrimary = LightOguriColorPalette.textOnMint,
        primaryContainer = LightOguriColorPalette.mintContainer,
        onPrimaryContainer = Mint90,
        secondary = LightOguriColorPalette.orange,
        onSecondary = LightOguriColorPalette.textOnOrange,
        secondaryContainer = LightOguriColorPalette.orangeContainer,
        onSecondaryContainer = Orange80,
        tertiary = Mint80,
        onTertiary = Neutral0,
        tertiaryContainer = Mint20,
        onTertiaryContainer = Mint90,
        background = LightOguriColorPalette.backgroundPrimary,
        onBackground = LightOguriColorPalette.textPrimary,
        surface = LightOguriColorPalette.surfaceCard,
        onSurface = LightOguriColorPalette.textPrimary,
        surfaceVariant = LightOguriColorPalette.backgroundTertiary,
        onSurfaceVariant = LightOguriColorPalette.textSecondary,
        surfaceTint = LightOguriColorPalette.mint,
        outline = LightOguriColorPalette.borderStrong,
        outlineVariant = LightOguriColorPalette.borderSubtle,
        error = LightOguriColorPalette.error,
        onError = Neutral0,
        errorContainer = Color(0xFFFEECEB),
        onErrorContainer = Color(0xFF601410),
    )

val DarkOguriMaterialColorScheme =
    darkColorScheme(
        primary = DarkOguriColorPalette.mint,
        onPrimary = DarkOguriColorPalette.textOnMint,
        primaryContainer = DarkOguriColorPalette.mintContainer,
        onPrimaryContainer = Mint20,
        secondary = DarkOguriColorPalette.orange,
        onSecondary = DarkOguriColorPalette.textOnOrange,
        secondaryContainer = DarkOguriColorPalette.orangeContainer,
        onSecondaryContainer = Orange20,
        tertiary = Mint30,
        onTertiary = Neutral100,
        tertiaryContainer = Color(0xFF14352F),
        onTertiaryContainer = Mint20,
        background = DarkOguriColorPalette.backgroundPrimary,
        onBackground = DarkOguriColorPalette.textPrimary,
        surface = DarkOguriColorPalette.surfaceCard,
        onSurface = DarkOguriColorPalette.textPrimary,
        surfaceVariant = DarkOguriColorPalette.backgroundTertiary,
        onSurfaceVariant = DarkOguriColorPalette.textSecondary,
        surfaceTint = DarkOguriColorPalette.mint,
        outline = DarkOguriColorPalette.borderStrong,
        outlineVariant = DarkOguriColorPalette.borderSubtle,
        error = DarkOguriColorPalette.error,
        onError = Neutral100,
        errorContainer = Color(0xFF5E1F1A),
        onErrorContainer = Color(0xFFFFDAD6),
    )

@Composable
fun OguriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    @Suppress("UNUSED_VARIABLE")
    val ignoredDynamicColor = dynamicColor
    val typographySystem = rememberOguriTypographySystem()
    val materialTypography = rememberOguriMaterialTypography(typographySystem)
    val colorPalette = if (darkTheme) DarkOguriColorPalette else LightOguriColorPalette
    val colorScheme = if (darkTheme) DarkOguriMaterialColorScheme else LightOguriMaterialColorScheme
    CompositionLocalProvider(
        LocalTypography provides typographySystem,
        LocalColorPalette provides colorPalette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = materialTypography,
            content = content,
        )
    }
}
