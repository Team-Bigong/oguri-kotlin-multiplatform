package com.bigong.oguri.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

private val MINT_5_LIGHT: Color = Color(0xFFF3FBFA)
private val MINT_10_LIGHT: Color = Color(0xFFE6F7F4)
private val MINT_20_LIGHT: Color = Color(0xFFD2F1EB)
private val MINT_30_LIGHT: Color = Color(0xFFBDEAE2)
private val MINT_40_LIGHT: Color = Color(0xFFAFEDE2)
private val MINT_50_LIGHT: Color = Color(0xFFABEBDD)
private val MINT_60_LIGHT: Color = Color(0xFF7FDACB)
private val MINT_70_LIGHT: Color = Color(0xFF4FC3AF)
private val MINT_80_LIGHT: Color = Color(0xFF239C89)
private val MINT_90_LIGHT: Color = Color(0xFF137363)
private val MINT_5_DARK: Color = Color(0xFF071311)
private val MINT_10_DARK: Color = Color(0xFF0C1F1C)
private val MINT_20_DARK: Color = Color(0xFF14322D)
private val MINT_30_DARK: Color = Color(0xFF1D463F)
private val MINT_40_DARK: Color = Color(0xFF275A51)
private val MINT_50_DARK: Color = Color(0xFF3A8277)
private val MINT_60_DARK: Color = Color(0xFF4B9C90)
private val MINT_70_DARK: Color = Color(0xFF5FB8A8)
private val MINT_80_DARK: Color = Color(0xFF78CCBC)
private val MINT_90_DARK: Color = Color(0xFF96DFCF)

private val ORANGE_5_LIGHT: Color = Color(0xFFFFF4EE)
private val ORANGE_10_LIGHT: Color = Color(0xFFFFE7DA)
private val ORANGE_20_LIGHT: Color = Color(0xFFFFCFB7)
private val ORANGE_30_LIGHT: Color = Color(0xFFFFB18A)
private val ORANGE_40_LIGHT: Color = Color(0xFFFF8C5C)
private val ORANGE_50_LIGHT: Color = Color(0xFFFF6F3D)
private val ORANGE_60_LIGHT: Color = Color(0xFFE95727)
private val ORANGE_70_LIGHT: Color = Color(0xFFC5421C)
private val ORANGE_80_LIGHT: Color = Color(0xFF9B3314)
private val ORANGE_90_LIGHT: Color = Color(0xFF6E240E)
private val ORANGE_5_DARK: Color = Color(0xFF1A100B)
private val ORANGE_10_DARK: Color = Color(0xFF2A1911)
private val ORANGE_20_DARK: Color = Color(0xFF4A2C19)
private val ORANGE_30_DARK: Color = Color(0xFF653C22)
private val ORANGE_40_DARK: Color = Color(0xFF834D2A)
private val ORANGE_50_DARK: Color = Color(0xFFA96335)
private val ORANGE_60_DARK: Color = Color(0xFFC8793E)
private val ORANGE_70_DARK: Color = Color(0xFFE1924C)
private val ORANGE_80_DARK: Color = Color(0xFFF0AE67)
private val ORANGE_90_DARK: Color = Color(0xFFFFCCA0)

private val NEUTRAL_0_LIGHT: Color = Color(0xFFFFFFFF)
private val NEUTRAL_5_LIGHT: Color = Color(0xFFF8FAFC)
private val NEUTRAL_10_LIGHT: Color = Color(0xFFF1F5F9)
private val NEUTRAL_20_LIGHT: Color = Color(0xFFE2E8F0)
private val NEUTRAL_30_LIGHT: Color = Color(0xFFCBD5E1)
private val NEUTRAL_40_LIGHT: Color = Color(0xFF94A3B8)
private val NEUTRAL_50_LIGHT: Color = Color(0xFF64748B)
private val NEUTRAL_60_LIGHT: Color = Color(0xFF475569)
private val NEUTRAL_70_LIGHT: Color = Color(0xFF334155)
private val NEUTRAL_80_LIGHT: Color = Color(0xFF1F2937)
private val NEUTRAL_90_LIGHT: Color = Color(0xFF0F172A)
private val NEUTRAL_95_LIGHT: Color = Color(0xFF0B1220)
private val NEUTRAL_100_LIGHT: Color = Color(0xFF060B14)
private val NEUTRAL_0_DARK: Color = NEUTRAL_100_LIGHT
private val NEUTRAL_5_DARK: Color = NEUTRAL_95_LIGHT
private val NEUTRAL_10_DARK: Color = NEUTRAL_90_LIGHT
private val NEUTRAL_20_DARK: Color = Color(0xFF172335)
private val NEUTRAL_30_DARK: Color = Color(0xFF223246)
private val NEUTRAL_40_DARK: Color = Color(0xFF5B7390)
private val NEUTRAL_50_DARK: Color = Color(0xFF7390AC)
private val NEUTRAL_60_DARK: Color = Color(0xFF8EA7BF)
private val NEUTRAL_70_DARK: Color = Color(0xFFAFC3D7)
private val NEUTRAL_80_DARK: Color = Color(0xFFC8D8E7)
private val NEUTRAL_90_DARK: Color = Color(0xFFE0EAF2)
private val NEUTRAL_95_DARK: Color = Color(0xFFEDF3F8)
private val NEUTRAL_100_DARK: Color = NEUTRAL_0_LIGHT
private val SURFACE_RAISED_LIGHT: Color = Color(0xFFFBFDFE)
private val MINT_CONTAINER_DARK: Color = MINT_20_DARK
private val ORANGE_CONTAINER_DARK: Color = ORANGE_20_DARK
private val SURFACE_CARD_DARK: Color = Color(0xFF0D1626)
private val SURFACE_RAISED_DARK: Color = Color(0xFF121E31)
private val BORDER_SUBTLE_DARK: Color = NEUTRAL_20_DARK
private val BORDER_STRONG_DARK: Color = NEUTRAL_30_DARK

private val SUCCESS_LIGHT: Color = Color(0xFF12B76A)
private val WARNING_LIGHT: Color = Color(0xFFF79009)
private val ERROR_LIGHT: Color = Color(0xFFD92D20)
private val INFO_LIGHT: Color = Color(0xFF2E90FA)
private val KAKAO_YELLOW_LIGHT: Color = Color(0xFFFEE500)
private val SUCCESS_DARK: Color = Color(0xFF2ECC80)
private val WARNING_DARK: Color = Color(0xFFFFBE5C)
private val ERROR_DARK: Color = Color(0xFFFF7B72)
private val INFO_DARK: Color = Color(0xFF82BEFF)
private val KAKAO_YELLOW_DARK: Color = Color(0xFFE6CC00)

private val mint5Token = mutableStateOf(MINT_5_LIGHT)
private val mint10Token = mutableStateOf(MINT_10_LIGHT)
private val mint20Token = mutableStateOf(MINT_20_LIGHT)
private val mint30Token = mutableStateOf(MINT_30_LIGHT)
private val mint40Token = mutableStateOf(MINT_40_LIGHT)
private val mint50Token = mutableStateOf(MINT_50_LIGHT)
private val mint60Token = mutableStateOf(MINT_60_LIGHT)
private val mint70Token = mutableStateOf(MINT_70_LIGHT)
private val mint80Token = mutableStateOf(MINT_80_LIGHT)
private val mint90Token = mutableStateOf(MINT_90_LIGHT)

private val orange5Token = mutableStateOf(ORANGE_5_LIGHT)
private val orange10Token = mutableStateOf(ORANGE_10_LIGHT)
private val orange20Token = mutableStateOf(ORANGE_20_LIGHT)
private val orange30Token = mutableStateOf(ORANGE_30_LIGHT)
private val orange40Token = mutableStateOf(ORANGE_40_LIGHT)
private val orange50Token = mutableStateOf(ORANGE_50_LIGHT)
private val orange60Token = mutableStateOf(ORANGE_60_LIGHT)
private val orange70Token = mutableStateOf(ORANGE_70_LIGHT)
private val orange80Token = mutableStateOf(ORANGE_80_LIGHT)
private val orange90Token = mutableStateOf(ORANGE_90_LIGHT)

private val neutral0Token = mutableStateOf(NEUTRAL_0_LIGHT)
private val neutral5Token = mutableStateOf(NEUTRAL_5_LIGHT)
private val neutral10Token = mutableStateOf(NEUTRAL_10_LIGHT)
private val neutral20Token = mutableStateOf(NEUTRAL_20_LIGHT)
private val neutral30Token = mutableStateOf(NEUTRAL_30_LIGHT)
private val neutral40Token = mutableStateOf(NEUTRAL_40_LIGHT)
private val neutral50Token = mutableStateOf(NEUTRAL_50_LIGHT)
private val neutral60Token = mutableStateOf(NEUTRAL_60_LIGHT)
private val neutral70Token = mutableStateOf(NEUTRAL_70_LIGHT)
private val neutral80Token = mutableStateOf(NEUTRAL_80_LIGHT)
private val neutral90Token = mutableStateOf(NEUTRAL_90_LIGHT)
private val neutral95Token = mutableStateOf(NEUTRAL_95_LIGHT)
private val neutral100Token = mutableStateOf(NEUTRAL_100_LIGHT)

private val successToken = mutableStateOf(SUCCESS_LIGHT)
private val warningToken = mutableStateOf(WARNING_LIGHT)
private val errorToken = mutableStateOf(ERROR_LIGHT)
private val infoToken = mutableStateOf(INFO_LIGHT)
private val kakaoYellowToken = mutableStateOf(KAKAO_YELLOW_LIGHT)

val Mint5: Color
    get() = mint5Token.value
val Mint10: Color
    get() = mint10Token.value
val Mint20: Color
    get() = mint20Token.value
val Mint30: Color
    get() = mint30Token.value
val Mint40: Color
    get() = mint40Token.value
val Mint50: Color
    get() = mint50Token.value
val Mint60: Color
    get() = mint60Token.value
val Mint70: Color
    get() = mint70Token.value
val Mint80: Color
    get() = mint80Token.value
val Mint90: Color
    get() = mint90Token.value

val Orange5: Color
    get() = orange5Token.value
val Orange10: Color
    get() = orange10Token.value
val Orange20: Color
    get() = orange20Token.value
val Orange30: Color
    get() = orange30Token.value
val Orange40: Color
    get() = orange40Token.value
val Orange50: Color
    get() = orange50Token.value
val Orange60: Color
    get() = orange60Token.value
val Orange70: Color
    get() = orange70Token.value
val Orange80: Color
    get() = orange80Token.value
val Orange90: Color
    get() = orange90Token.value

val Neutral0: Color
    get() = neutral0Token.value
val Neutral5: Color
    get() = neutral5Token.value
val Neutral10: Color
    get() = neutral10Token.value
val Neutral20: Color
    get() = neutral20Token.value
val Neutral30: Color
    get() = neutral30Token.value
val Neutral40: Color
    get() = neutral40Token.value
val Neutral50: Color
    get() = neutral50Token.value
val Neutral60: Color
    get() = neutral60Token.value
val Neutral70: Color
    get() = neutral70Token.value
val Neutral80: Color
    get() = neutral80Token.value
val Neutral90: Color
    get() = neutral90Token.value
val Neutral95: Color
    get() = neutral95Token.value
val Neutral100: Color
    get() = neutral100Token.value

val SuccessGreen: Color
    get() = successToken.value
val WarningAmber: Color
    get() = warningToken.value
val ErrorRed: Color
    get() = errorToken.value
val InfoBlue: Color
    get() = infoToken.value
val KakaoYellow: Color
    get() = kakaoYellowToken.value

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
        mint = MINT_70_LIGHT,
        mintContainer = MINT_10_LIGHT,
        orange = ORANGE_50_LIGHT,
        orangeContainer = ORANGE_10_LIGHT,
        backgroundPrimary = NEUTRAL_0_LIGHT,
        backgroundSecondary = NEUTRAL_5_LIGHT,
        backgroundTertiary = NEUTRAL_10_LIGHT,
        surfaceCard = NEUTRAL_0_LIGHT,
        surfaceRaised = SURFACE_RAISED_LIGHT,
        borderSubtle = NEUTRAL_20_LIGHT,
        borderStrong = NEUTRAL_30_LIGHT,
        textPrimary = NEUTRAL_90_LIGHT,
        textSecondary = NEUTRAL_70_LIGHT,
        textTertiary = NEUTRAL_50_LIGHT,
        textOnMint = NEUTRAL_0_LIGHT,
        textOnOrange = NEUTRAL_0_LIGHT,
        success = SUCCESS_LIGHT,
        warning = WARNING_LIGHT,
        error = ERROR_LIGHT,
        info = INFO_LIGHT,
    )

val DarkOguriColorPalette =
    OguriColorPalette(
        mint = MINT_70_DARK,
        mintContainer = MINT_CONTAINER_DARK,
        orange = ORANGE_80_DARK,
        orangeContainer = ORANGE_CONTAINER_DARK,
        backgroundPrimary = NEUTRAL_0_DARK,
        backgroundSecondary = NEUTRAL_5_DARK,
        backgroundTertiary = NEUTRAL_10_DARK,
        surfaceCard = SURFACE_CARD_DARK,
        surfaceRaised = SURFACE_RAISED_DARK,
        borderSubtle = BORDER_SUBTLE_DARK,
        borderStrong = BORDER_STRONG_DARK,
        textPrimary = NEUTRAL_95_DARK,
        textSecondary = NEUTRAL_80_DARK,
        textTertiary = NEUTRAL_70_DARK,
        textOnMint = NEUTRAL_100_DARK,
        textOnOrange = NEUTRAL_100_DARK,
        success = SUCCESS_DARK,
        warning = WARNING_DARK,
        error = ERROR_DARK,
        info = INFO_DARK,
    )

private var isDarkTokenModeApplied: Boolean = false

internal fun applyLegacyColorTokenMode(isDarkTheme: Boolean) {
    if (isDarkTokenModeApplied == isDarkTheme) {
        return
    }
    isDarkTokenModeApplied = isDarkTheme

    if (!isDarkTheme) {
        mint5Token.value = MINT_5_LIGHT
        mint10Token.value = MINT_10_LIGHT
        mint20Token.value = MINT_20_LIGHT
        mint30Token.value = MINT_30_LIGHT
        mint40Token.value = MINT_40_LIGHT
        mint50Token.value = MINT_50_LIGHT
        mint60Token.value = MINT_60_LIGHT
        mint70Token.value = MINT_70_LIGHT
        mint80Token.value = MINT_80_LIGHT
        mint90Token.value = MINT_90_LIGHT

        orange5Token.value = ORANGE_5_LIGHT
        orange10Token.value = ORANGE_10_LIGHT
        orange20Token.value = ORANGE_20_LIGHT
        orange30Token.value = ORANGE_30_LIGHT
        orange40Token.value = ORANGE_40_LIGHT
        orange50Token.value = ORANGE_50_LIGHT
        orange60Token.value = ORANGE_60_LIGHT
        orange70Token.value = ORANGE_70_LIGHT
        orange80Token.value = ORANGE_80_LIGHT
        orange90Token.value = ORANGE_90_LIGHT

        neutral0Token.value = NEUTRAL_0_LIGHT
        neutral5Token.value = NEUTRAL_5_LIGHT
        neutral10Token.value = NEUTRAL_10_LIGHT
        neutral20Token.value = NEUTRAL_20_LIGHT
        neutral30Token.value = NEUTRAL_30_LIGHT
        neutral40Token.value = NEUTRAL_40_LIGHT
        neutral50Token.value = NEUTRAL_50_LIGHT
        neutral60Token.value = NEUTRAL_60_LIGHT
        neutral70Token.value = NEUTRAL_70_LIGHT
        neutral80Token.value = NEUTRAL_80_LIGHT
        neutral90Token.value = NEUTRAL_90_LIGHT
        neutral95Token.value = NEUTRAL_95_LIGHT
        neutral100Token.value = NEUTRAL_100_LIGHT

        successToken.value = SUCCESS_LIGHT
        warningToken.value = WARNING_LIGHT
        errorToken.value = ERROR_LIGHT
        infoToken.value = INFO_LIGHT
        kakaoYellowToken.value = KAKAO_YELLOW_LIGHT
        return
    }

    mint5Token.value = MINT_5_DARK
    mint10Token.value = MINT_10_DARK
    mint20Token.value = MINT_20_DARK
    mint30Token.value = MINT_30_DARK
    mint40Token.value = MINT_40_DARK
    mint50Token.value = MINT_50_DARK
    mint60Token.value = MINT_60_DARK
    mint70Token.value = MINT_70_DARK
    mint80Token.value = MINT_80_DARK
    mint90Token.value = MINT_90_DARK

    orange5Token.value = ORANGE_5_DARK
    orange10Token.value = ORANGE_10_DARK
    orange20Token.value = ORANGE_20_DARK
    orange30Token.value = ORANGE_30_DARK
    orange40Token.value = ORANGE_40_DARK
    orange50Token.value = ORANGE_50_DARK
    orange60Token.value = ORANGE_60_DARK
    orange70Token.value = ORANGE_70_DARK
    orange80Token.value = ORANGE_80_DARK
    orange90Token.value = ORANGE_90_DARK

    neutral0Token.value = NEUTRAL_0_DARK
    neutral5Token.value = NEUTRAL_5_DARK
    neutral10Token.value = NEUTRAL_10_DARK
    neutral20Token.value = NEUTRAL_20_DARK
    neutral30Token.value = NEUTRAL_30_DARK
    neutral40Token.value = NEUTRAL_40_DARK
    neutral50Token.value = NEUTRAL_50_DARK
    neutral60Token.value = NEUTRAL_60_DARK
    neutral70Token.value = NEUTRAL_70_DARK
    neutral80Token.value = NEUTRAL_80_DARK
    neutral90Token.value = NEUTRAL_90_DARK
    neutral95Token.value = NEUTRAL_95_DARK
    neutral100Token.value = NEUTRAL_100_DARK

    successToken.value = SUCCESS_DARK
    warningToken.value = WARNING_DARK
    errorToken.value = ERROR_DARK
    infoToken.value = INFO_DARK
    kakaoYellowToken.value = KAKAO_YELLOW_DARK
}
