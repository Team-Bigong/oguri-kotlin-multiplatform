package com.bigong.oguri.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ======================================================
// Mint (Primary Axis)
// Anchor color: #ABEBDD
// ======================================================

val Mint5 = Color(0xFFF3FBFA)
val Mint10 = Color(0xFFE6F7F4)
val Mint20 = Color(0xFFD2F1EB)
val Mint30 = Color(0xFFBDEAE2)
val Mint40 = Color(0xFFAFEDE2)
val Mint50 = Color(0xFFABEBDD)
val Mint60 = Color(0xFF7FDACB)
val Mint70 = Color(0xFF4FC3AF)
val Mint80 = Color(0xFF239C89)
val Mint90 = Color(0xFF137363)

// ======================================================
// Orange (Accent / CTA Axis)
// ======================================================

val Orange5 = Color(0xFFFFF4EE)
val Orange10 = Color(0xFFFFE7DA)
val Orange20 = Color(0xFFFFCFB7)
val Orange30 = Color(0xFFFFB18A)
val Orange40 = Color(0xFFFF8C5C)
val Orange50 = Color(0xFFFF6F3D)
val Orange60 = Color(0xFFE95727)
val Orange70 = Color(0xFFC5421C)
val Orange80 = Color(0xFF9B3314)
val Orange90 = Color(0xFF6E240E)

// ======================================================
// Neutral (True grayscale only)
// ======================================================

val Neutral0 = Color(0xFFFFFFFF)
val Neutral5 = Color(0xFFF8FAFC)
val Neutral10 = Color(0xFFF1F5F9)
val Neutral20 = Color(0xFFE2E8F0)
val Neutral30 = Color(0xFFCBD5E1)
val Neutral40 = Color(0xFF94A3B8)
val Neutral50 = Color(0xFF64748B)
val Neutral60 = Color(0xFF475569)
val Neutral70 = Color(0xFF334155)
val Neutral80 = Color(0xFF1F2937)
val Neutral90 = Color(0xFF0F172A)
val Neutral95 = Color(0xFF0B1220)
val Neutral100 = Color(0xFF060B14)

// ======================================================
// Semantic
// ======================================================

val SuccessGreen = Color(0xFF12B76A)
val WarningAmber = Color(0xFFF79009)
val ErrorRed = Color(0xFFD92D20)
val InfoBlue = Color(0xFF2E90FA)
val KakaoYellow = Color(0xFFFEE500)

@Immutable
data class OguriColorPalette(
    val mint: Color,
    val mintContainer: Color,
    val orange: Color,
    val orangeContainer: Color,
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
    val textOnMint: Color,
    val textOnOrange: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
)

val LightOguriColorPalette =
    OguriColorPalette(
        mint = Mint70,
        mintContainer = Mint10,
        orange = Orange50,
        orangeContainer = Orange10,
        backgroundPrimary = Neutral0,
        backgroundSecondary = Neutral5,
        backgroundTertiary = Neutral10,
        surfaceCard = Neutral0,
        surfaceRaised = Color(0xFFFBFDFE),
        borderSubtle = Neutral20,
        borderStrong = Neutral30,
        textPrimary = Neutral90,
        textSecondary = Neutral70,
        textTertiary = Neutral50,
        textOnMint = Neutral0,
        textOnOrange = Neutral0,
        success = SuccessGreen,
        warning = WarningAmber,
        error = ErrorRed,
        info = InfoBlue,
    )

val DarkOguriColorPalette =
    OguriColorPalette(
        mint = Mint50,
        mintContainer = Color(0xFF0E2B28),
        orange = Orange40,
        orangeContainer = Color(0xFF3A1F14),
        backgroundPrimary = Neutral100,
        backgroundSecondary = Neutral95,
        backgroundTertiary = Neutral90,
        surfaceCard = Color(0xFF0B1422),
        surfaceRaised = Color(0xFF0F1B2C),
        borderSubtle = Color(0xFF1B2A3A),
        borderStrong = Color(0xFF2A3B4D),
        textPrimary = Neutral5,
        textSecondary = Neutral20,
        textTertiary = Neutral40,
        textOnMint = Neutral100,
        textOnOrange = Neutral100,
        success = Color(0xFF33D17A),
        warning = Color(0xFFFFB547),
        error = Color(0xFFFF6B5E),
        info = Color(0xFF73B7FF),
    )
