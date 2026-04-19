package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.ui.component.OguriAppLogoImage
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_search
import oguri.composeapp.generated.resources.home_button_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeLogoHeader(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
) {
    Spacer(modifier = Modifier.height(height = 10.dp))
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OguriAppLogoImage(
            modifier = Modifier.height(height = 34.dp),
        )
        Image(
            painter = painterResource(resource = Res.drawable.btn_search),
            contentDescription = stringResource(resource = Res.string.home_button_search),
            colorFilter = ColorFilter.tint(Neutral100),
            modifier =
                Modifier
                    .size(size = 32.dp)
                    .noRippleClickable(onClick = onSearchClick),
        )
    }
}
