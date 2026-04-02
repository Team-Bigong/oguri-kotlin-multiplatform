package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.util.extension.noRippleClickable

private const val LOADING_BACKGROUND_ALPHA = 0.56f

@Composable
fun CenteredLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Neutral0.copy(alpha = LOADING_BACKGROUND_ALPHA))
                .noRippleClickable(onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = Mint70,
            trackColor = Neutral20,
            strokeWidth = 3.dp,
        )
    }
}
