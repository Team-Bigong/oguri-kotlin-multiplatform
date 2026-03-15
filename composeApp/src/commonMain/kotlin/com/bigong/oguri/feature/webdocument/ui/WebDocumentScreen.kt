package com.bigong.oguri.feature.webdocument.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.PlatformWebView
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import org.jetbrains.compose.resources.painterResource

@Composable
fun WebDocumentScreen(
    titleText: String,
    url: String,
    goBackTrigger: Int,
    onCanGoBackChange: (Boolean) -> Unit,
    onCurrentUrlChange: (String?) -> Unit,
    onBackClick: () -> Unit,
) {
    PlatformBackHandler(
        enabled = true,
        onBack = onBackClick,
    )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral0),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.btn_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral100),
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .noRippleClickable(onClick = onBackClick),
            )

            Text(
                text = titleText,
                style = OguriTheme.typography.cardTitle,
                color = Neutral100,
                modifier = Modifier.align(Alignment.Center),
            )

            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
            )
        }
        HorizontalDivider(color = Neutral20)

        PlatformWebView(
            url = url,
            goBackTrigger = goBackTrigger,
            onCanGoBackChange = onCanGoBackChange,
            onCurrentUrlChange = onCurrentUrlChange,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
