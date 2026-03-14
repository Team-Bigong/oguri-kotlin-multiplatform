package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_share
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlaceDetailShareButton(
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
) {
    Box(
        modifier =
            modifier
                .size(48.dp)
                .noRippleClickable(onClick = onShareClick)
                .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.btn_share),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
