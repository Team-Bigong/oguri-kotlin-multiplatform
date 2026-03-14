package com.bigong.oguri.feature.login.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginActionButton(
    iconResource: DrawableResource,
    titleText: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(size = 10.dp))
                .noRippleClickable(onClick = onClick)
                .padding(horizontal = 30.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(resource = iconResource),
            contentDescription = null,
            modifier = Modifier.size(size = 18.dp),
        )
        Spacer(modifier = Modifier.size(size = 12.dp))
        Text(
            text = titleText,
            style = OguriTheme.typography.cardSubtitle,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(size = 18.dp))
    }
}
