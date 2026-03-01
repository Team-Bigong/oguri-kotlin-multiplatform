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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.util.extension.getStyledText

private val HOME_PRODUCT_CARD_CORNER_RADIUS = 8.dp

@Composable
fun HomeProductCard(
    imageUrl: String,
    titleText: String,
    highlightedText: String,
    highlightedColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = HOME_PRODUCT_CARD_CORNER_RADIUS))
                .background(
                    color = Mint5,
                    shape = RoundedCornerShape(size = HOME_PRODUCT_CARD_CORNER_RADIUS),
                ),
    ) {
        NetworkImage(
            imageUrl = imageUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height = 128.dp),
        )
        Text(
            text =
                titleText.getStyledText(
                    style =
                        TextStyle(
                            color = highlightedColor,
                            fontWeight = FontWeight.Bold,
                        ),
                    highlightedText,
                ),
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        )
    }
}
