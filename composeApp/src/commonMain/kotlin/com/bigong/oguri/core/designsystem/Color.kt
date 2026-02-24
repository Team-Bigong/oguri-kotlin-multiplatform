package com.bigong.oguri.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Reference direction: Triple's clean travel information density + Airbnb's warm editorial accents.
// Oguri adapts that feel with a mint-forward brand tone and orange secondary highlights.

// Mint (Primary)
val Mint5: Color = Color(0xFFF2FFFD)
val Mint10: Color = Color(0xFFE6FFFB)
val Mint20: Color = Color(0xFFC8FFF2)
val Mint30: Color = Color(0xFF9AF7E4)
val Mint40: Color = Color(0xFF63EBD1)
val Mint50: Color = Color(0xFF2FDAB9)
val Mint60: Color = Color(0xFF16C4A4)
val Mint70: Color = Color(0xFF0E9A84)
val Mint80: Color = Color(0xFF0C7565)
val Mint90: Color = Color(0xFF0A564C)

// Orange (Secondary emphasis)
val Orange5: Color = Color(0xFFFFF6EE)
val Orange10: Color = Color(0xFFFFECD9)
val Orange20: Color = Color(0xFFFFD6AE)
val Orange30: Color = Color(0xFFFFBB7A)
val Orange40: Color = Color(0xFFFF9854)
val Orange50: Color = Color(0xFFFF7A2F)
val Orange60: Color = Color(0xFFE55F17)
val Orange70: Color = Color(0xFFB84910)
val Orange80: Color = Color(0xFF8D360D)

// Neutral
val Neutral0: Color = Color(0xFFFFFFFF)
val Neutral5: Color = Color(0xFFF9FBFC)
val Neutral10: Color = Color(0xFFF2F5F7)
val Neutral20: Color = Color(0xFFE7ECEF)
val Neutral30: Color = Color(0xFFD8E0E5)
val Neutral40: Color = Color(0xFFBAC6CF)
val Neutral50: Color = Color(0xFF8D9BA7)
val Neutral60: Color = Color(0xFF677785)
val Neutral70: Color = Color(0xFF495867)
val Neutral80: Color = Color(0xFF2F3B47)
val Neutral90: Color = Color(0xFF1B252E)
val Neutral95: Color = Color(0xFF121A22)
val Neutral100: Color = Color(0xFF0B1116)

// Semantic support colors
val SuccessGreen: Color = Color(0xFF12B76A)
val WarningOrange: Color = Color(0xFFF79009)
val ErrorRed: Color = Color(0xFFD92D20)
val InfoBlue: Color = Color(0xFF2E90FA)

val KakaoYellow: Color = Color(0xFFFEE500)

@Immutable
data class OguriColorPalette(
    val brandMint: Color,
    val brandMintContainer: Color,
    val accentOrange: Color,
    val accentOrangeContainer: Color,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val surfaceCard: Color,
    val surfaceRaised: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnBrand: Color,
    val textOnAccent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
)

val LightOguriColorPalette: OguriColorPalette =
    OguriColorPalette(
        brandMint = Mint60,
        brandMintContainer = Mint10,
        accentOrange = Orange50,
        accentOrangeContainer = Orange10,
        backgroundPrimary = Neutral0,
        backgroundSecondary = Neutral5,
        backgroundTertiary = Neutral10,
        surfaceCard = Neutral0,
        surfaceRaised = Color(0xFFFDFEFE),
        borderSubtle = Neutral20,
        borderStrong = Neutral30,
        textPrimary = Neutral90,
        textSecondary = Neutral70,
        textTertiary = Neutral50,
        textOnBrand = Neutral0,
        textOnAccent = Neutral0,
        success = SuccessGreen,
        warning = WarningOrange,
        error = ErrorRed,
        info = InfoBlue,
    )

val DarkOguriColorPalette: OguriColorPalette =
    OguriColorPalette(
        brandMint = Mint40,
        brandMintContainer = Color(0xFF123A34),
        accentOrange = Orange40,
        accentOrangeContainer = Color(0xFF47250E),
        backgroundPrimary = Neutral100,
        backgroundSecondary = Neutral95,
        backgroundTertiary = Neutral90,
        surfaceCard = Color(0xFF131D25),
        surfaceRaised = Color(0xFF19242C),
        borderSubtle = Color(0xFF22313D),
        borderStrong = Color(0xFF314250),
        textPrimary = Neutral5,
        textSecondary = Neutral30,
        textTertiary = Neutral40,
        textOnBrand = Neutral100,
        textOnAccent = Neutral100,
        success = Color(0xFF33D17A),
        warning = Color(0xFFFFB547),
        error = Color(0xFFFF6B5E),
        info = Color(0xFF73B7FF),
    )
