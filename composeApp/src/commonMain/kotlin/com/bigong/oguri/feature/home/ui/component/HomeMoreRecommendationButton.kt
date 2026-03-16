package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

@Composable
fun HomeMoreRecommendationButton(
    subtitleText: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Text(
            text = subtitleText,
            style = OguriTheme.typography.caption,
            color = Neutral50,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(height = 6.dp))
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .noRippleClickable(onClick = onClick)
                    .background(color = Mint5, shape = RoundedCornerShape(size = 8.dp))
                    .border(width = 1.dp, color = Mint70, shape = RoundedCornerShape(size = 8.dp))
                    .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = OguriTheme.typography.cardTitle,
                color = Mint70,
            )
        }
    }
}
