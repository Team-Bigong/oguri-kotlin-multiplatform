package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

private val HOME_ERROR_VERTICAL_SPACING = 12.dp

@Composable
fun HomeErrorContent(
    message: String,
    retryText: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = HOME_ERROR_VERTICAL_SPACING,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = message,
            style = OguriTheme.typography.bodyLarge,
            color = Color(0xFF5D6670),
        )

        Box(
            modifier = Modifier
                .background(color = Color(0xFFDDE7E7), shape = RoundedCornerShape(size = 10.dp))
                .noRippleClickable(onClick = onRetryClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = retryText,
                style = OguriTheme.typography.labelLarge,
                color = Color(0xFF43B9A8),
            )
        }
    }
}
