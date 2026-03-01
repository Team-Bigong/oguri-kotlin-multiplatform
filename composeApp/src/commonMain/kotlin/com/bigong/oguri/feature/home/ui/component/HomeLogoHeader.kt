package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_app_text
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeLogoHeader() {
    Spacer(modifier = Modifier.height(height = 10.dp))
    Image(
        painter = painterResource(resource = Res.drawable.ic_app_text),
        contentDescription = null,
        modifier = Modifier.height(height = 34.dp),
    )
}
