package com.bigong.oguri.feature.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.SuccessGreen
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.DisplayThemeMode
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.ic_terms_checked
import oguri.composeapp.generated.resources.ic_terms_unchecked
import org.jetbrains.compose.resources.painterResource

@Composable
fun DisplaySettingsScreen(
    titleText: String,
    followSystemText: String,
    lightModeText: String,
    darkModeText: String,
    currentDisplayThemeMode: DisplayThemeMode,
    onDisplayThemeModeChange: (DisplayThemeMode) -> Unit,
    onBackClick: () -> Unit,
) {
    PlatformBackHandler(
        enabled = true,
        onBack = onBackClick,
    )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral0),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.btn_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral100),
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .noRippleClickable(onClick = onBackClick),
            )

            Text(
                text = titleText,
                style = OguriTheme.typography.cardTitle,
                color = Neutral100,
                modifier = Modifier.align(Alignment.Center),
            )

            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
            )
        }

        Column(
            modifier = Modifier.padding(top = 8.dp),
        ) {
            DisplayThemeModeRow(
                text = followSystemText,
                isSelected = currentDisplayThemeMode == DisplayThemeMode.SYSTEM,
                onClick = { onDisplayThemeModeChange(DisplayThemeMode.SYSTEM) },
            )
            DisplayThemeModeRow(
                text = lightModeText,
                isSelected = currentDisplayThemeMode == DisplayThemeMode.LIGHT,
                onClick = { onDisplayThemeModeChange(DisplayThemeMode.LIGHT) },
            )
            DisplayThemeModeRow(
                text = darkModeText,
                isSelected = currentDisplayThemeMode == DisplayThemeMode.DARK,
                onClick = { onDisplayThemeModeChange(DisplayThemeMode.DARK) },
            )
        }
    }
}

@Composable
private fun DisplayThemeModeRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick)
                .padding(start = 24.dp, end = 24.dp)
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
        )

        Image(
            painter = painterResource(if (isSelected) Res.drawable.ic_terms_checked else Res.drawable.ic_terms_unchecked),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (isSelected) SuccessGreen else Neutral50),
            modifier = Modifier.size(30.dp),
        )
    }
}
