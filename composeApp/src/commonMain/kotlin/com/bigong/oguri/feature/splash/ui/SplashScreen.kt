package com.bigong.oguri.feature.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.splash_start
import oguri.composeapp.generated.resources.splash_subtitle
import oguri.composeapp.generated.resources.splash_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashRoute(
    onStartClick: () -> Unit,
) {
    SplashScreen(onStartClick = onStartClick)
}

@Composable
fun SplashScreen(
    onStartClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFDFF6EC),
                        Color(0xFFFFF1D6),
                        Color(0xFFFFFFFF),
                    ),
                ),
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        ) {
            PlaceholderHeader(
                screenTitleText = "🦆 ${stringResource(Res.string.splash_title)}",
                screenSubtitleText = stringResource(Res.string.splash_subtitle),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium),
        ) {
            PlaceholderActionButton(
                labelText = stringResource(Res.string.splash_start),
                onClick = onStartClick,
            )
        }
    }
}
