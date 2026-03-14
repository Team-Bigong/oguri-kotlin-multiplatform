package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_right_arrow
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPageMenuSection(
    menuItems: List<MyPageMenuItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = Neutral20)
        menuItems.forEach { menuItem: MyPageMenuItem ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .noRippleClickable(onClick = menuItem.onClick)
                        .padding(start = 24.dp, end = 12.dp)
                        .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = menuItem.label,
                    style = OguriTheme.typography.cardSubtitle,
                    color = Neutral100,
                )
                Image(
                    painter = painterResource(Res.drawable.ic_right_arrow),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Neutral100),
                )
            }
        }
    }
}

data class MyPageMenuItem(
    val label: String,
    val onClick: () -> Unit,
)
