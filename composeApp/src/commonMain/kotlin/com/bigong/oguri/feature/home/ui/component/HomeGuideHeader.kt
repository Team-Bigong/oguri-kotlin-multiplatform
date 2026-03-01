package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.getStyledText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val HOME_GUIDE_HIGHLIGHT_COLOR = Color(0xFF43B9A8)

@Composable
fun HomeGuideHeader(
    iconResource: DrawableResource,
    titleText: String,
    highlightedText: String,
    subtitleText: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(resource = iconResource),
            contentDescription = null,
            modifier = Modifier.size(size = 20.dp),
        )
        Column {
            Text(
                text = titleText.getStyledText(
                    style = TextStyle(
                        color = HOME_GUIDE_HIGHLIGHT_COLOR,
                        fontWeight = FontWeight.Bold,
                    ),
                    highlightedText,
                ),
                style = OguriTheme.typography.cardTitle,
                color = Color(0xFF14181F),
            )
            Text(
                text = subtitleText,
                style = OguriTheme.typography.bodyMedium,
                color = Color(0xFF94A0AC),
            )
        }
    }
}
