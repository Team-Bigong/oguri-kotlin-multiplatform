package com.bigong.oguri.feature.search.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SearchSectionHeader(
    titleText: String,
    iconResource: DrawableResource,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText,
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
        )
        Image(
            painter = painterResource(resource = iconResource),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Neutral70),
            modifier = Modifier.size(size = 24.dp),
        )
    }
}
