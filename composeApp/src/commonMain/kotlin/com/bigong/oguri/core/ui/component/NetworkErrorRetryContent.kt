package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_network_error_message
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.img_oguri_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val RETRY_CONTENT_HORIZONTAL_PADDING = 24.dp
private val RETRY_CONTENT_IMAGE_HEIGHT = 220.dp
private val RETRY_CONTENT_MESSAGE_TOP_SPACING = 12.dp
private val RETRY_CONTENT_BUTTON_TOP_SPACING = 24.dp
private val RETRY_CONTENT_BUTTON_HEIGHT = 48.dp
private val RETRY_CONTENT_BUTTON_CORNER_RADIUS = 8.dp

@Composable
fun NetworkErrorRetryContent(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    messageText: String = stringResource(Res.string.common_network_error_message),
    retryButtonText: String = stringResource(Res.string.common_retry_button),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = RETRY_CONTENT_HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_oguri_empty),
            contentDescription = null,
            modifier = Modifier.height(RETRY_CONTENT_IMAGE_HEIGHT),
        )

        SpacerHeight(height = RETRY_CONTENT_MESSAGE_TOP_SPACING)
        Text(
            text = messageText,
            style = OguriTheme.typography.cardTitle,
            color = Neutral90,
        )

        SpacerHeight(height = RETRY_CONTENT_BUTTON_TOP_SPACING)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(RETRY_CONTENT_BUTTON_HEIGHT)
                    .background(
                        color = Mint70,
                        shape = RoundedCornerShape(RETRY_CONTENT_BUTTON_CORNER_RADIUS),
                    )
                    .noRippleClickable(onClick = onRetryClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = retryButtonText,
                style = OguriTheme.typography.cardTitle,
                color = Neutral0,
            )
        }
    }
}

@Composable
private fun SpacerHeight(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}
