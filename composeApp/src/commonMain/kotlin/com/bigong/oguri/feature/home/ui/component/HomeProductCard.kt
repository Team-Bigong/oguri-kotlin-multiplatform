package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.getStyledText

private val HOME_PRODUCT_CARD_CORNER_RADIUS = 14.dp
private val HOME_PRODUCT_HIGHLIGHT_COLOR = Color(0xFF43B9A8)

@Composable
fun HomeProductCard(
    imageUrl: String,
    titleText: String,
    highlightedText: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = HOME_PRODUCT_CARD_CORNER_RADIUS))
            .background(
                color = Color(0xFFDDE7E7),
                shape = RoundedCornerShape(size = HOME_PRODUCT_CARD_CORNER_RADIUS),
            ),
    ) {
        NetworkImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 142.dp),
        )
        Text(
            text = titleText.getStyledText(
                style = TextStyle(
                    color = HOME_PRODUCT_HIGHLIGHT_COLOR,
                    fontWeight = FontWeight.Bold,
                ),
                highlightedText,
            ),
            style = OguriTheme.typography.cardTitle,
            color = Color(0xFF1A2229),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}
