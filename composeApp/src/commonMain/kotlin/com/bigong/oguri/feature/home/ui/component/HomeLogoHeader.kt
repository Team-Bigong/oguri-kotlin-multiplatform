package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.ui.component.OguriAppLogoImage

@Composable
fun HomeLogoHeader(modifier: Modifier = Modifier) {
    Spacer(modifier = Modifier.height(height = 10.dp))
    OguriAppLogoImage(
        modifier = modifier.height(height = 34.dp),
    )
}
