package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_app_text
import oguri.composeapp.generated.resources.ic_app_text_dark
import org.jetbrains.compose.resources.painterResource

@Composable
fun OguriAppLogoImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val logoResource =
        if (OguriTheme.isDarkTheme) {
            Res.drawable.ic_app_text_dark
        } else {
            Res.drawable.ic_app_text
        }

    Image(
        painter = painterResource(resource = logoResource),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
