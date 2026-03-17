package com.bigong.oguri.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.pretendard_bold
import oguri.composeapp.generated.resources.pretendard_medium
import oguri.composeapp.generated.resources.pretendard_regular
import oguri.composeapp.generated.resources.pretendard_semi_bold
import org.jetbrains.compose.resources.Font
import androidx.compose.material3.Typography as MaterialTypography

@Immutable
data class OguriTypographySystem(
    val heroTitle: TextStyle,
    val sectionTitle: TextStyle,
    val cardTitle: TextStyle,
    val cardSubtitle: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

@Composable
fun rememberOguriTypographySystem(): OguriTypographySystem {
    val pretendardFontFamily = rememberPretendardFontFamily()

    fun createTextStyle(
        fontWeight: FontWeight,
        fontSize: Int,
        lineHeight: Int,
        letterSpacing: Float = 0f,
    ): TextStyle =
        TextStyle(
            fontFamily = pretendardFontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = letterSpacing.em,
        )

    return OguriTypographySystem(
        heroTitle =
            createTextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28,
                lineHeight = 36,
                letterSpacing = -0.01f,
            ),
        sectionTitle =
            createTextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22,
                lineHeight = 30,
                letterSpacing = -0.01f,
            ),
        cardTitle =
            createTextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18,
                lineHeight = 25,
            ),
        cardSubtitle =
            createTextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 15,
                lineHeight = 22,
                letterSpacing = -0.005f,
            ),
        bodyLarge =
            createTextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16,
                lineHeight = 24,
            ),
        bodyMedium =
            createTextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14,
                lineHeight = 21,
            ),
        bodySmall =
            createTextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 13,
                lineHeight = 19,
            ),
        caption =
            createTextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12,
                lineHeight = 16,
                letterSpacing = 0.01f,
            ),
        labelLarge =
            createTextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14,
                lineHeight = 20,
            ),
        labelMedium =
            createTextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 12,
                lineHeight = 16,
                letterSpacing = 0.01f,
            ),
        labelSmall =
            createTextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 11,
                lineHeight = 14,
                letterSpacing = 0.02f,
            ),
    )
}

@Composable
fun rememberOguriMaterialTypography(typographySystem: OguriTypographySystem): MaterialTypography =
    MaterialTypography(
        displayLarge = typographySystem.heroTitle,
        headlineMedium = typographySystem.sectionTitle,
        titleLarge = typographySystem.cardTitle,
        titleMedium = typographySystem.cardSubtitle,
        bodyLarge = typographySystem.bodyLarge,
        bodyMedium = typographySystem.bodyMedium,
        bodySmall = typographySystem.bodySmall,
        labelLarge = typographySystem.labelLarge,
        labelMedium = typographySystem.labelMedium,
        labelSmall = typographySystem.labelSmall,
    )

@Composable
private fun rememberPretendardFontFamily(): FontFamily {
    val regularFont = Font(resource = Res.font.pretendard_regular, weight = FontWeight.Normal)
    val mediumFont = Font(resource = Res.font.pretendard_medium, weight = FontWeight.Medium)
    val semiBoldFont = Font(resource = Res.font.pretendard_semi_bold, weight = FontWeight.SemiBold)
    val boldFont = Font(resource = Res.font.pretendard_bold, weight = FontWeight.Bold)

    return remember {
        FontFamily(
            regularFont,
            mediumFont,
            semiBoldFont,
            boldFont,
        )
    }
}
