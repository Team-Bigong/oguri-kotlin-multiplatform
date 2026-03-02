package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.getStyledText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun GuideHeader(
    iconResource: DrawableResource,
    titleText: String,
    highlightedText: String,
    subtitleText: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(resource = iconResource),
            contentDescription = null,
            modifier = Modifier.size(size = 24.dp),
        )
        Column {
            Text(
                text =
                    titleText.getStyledText(
                        style = TextStyle(color = Mint70, fontWeight = FontWeight.Bold),
                        highlightedText,
                    ),
                modifier = Modifier.padding(top = 2.dp),
                style = OguriTheme.typography.cardTitle,
                color = Neutral100,
            )
            if (!subtitleText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    style = OguriTheme.typography.labelMedium,
                    color = Neutral50,
                )
            }
        }
    }
}
