package com.bigong.oguri.feature.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_app_text
import oguri.composeapp.generated.resources.img_oguri_walking
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_oguri_walking),
            contentDescription = null,
            modifier = Modifier.size(190.dp),
        )
        Image(
            painter = painterResource(Res.drawable.ic_app_text),
            contentDescription = null,
            modifier = Modifier.size(width = 110.dp, height = 60.dp),
        )
    }
}
