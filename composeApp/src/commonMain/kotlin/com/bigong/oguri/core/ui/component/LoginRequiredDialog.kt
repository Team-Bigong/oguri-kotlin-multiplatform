package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_exit
import oguri.composeapp.generated.resources.img_oguri_hello
import oguri.composeapp.generated.resources.login_required_button
import oguri.composeapp.generated.resources.login_required_title
import oguri.composeapp.generated.resources.login_required_title_highlight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginRequiredDialog(
    onDismissRequest: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val titleText = stringResource(Res.string.login_required_title)
    val highlightedTitleText = stringResource(Res.string.login_required_title_highlight)
    val loginButtonText = stringResource(Res.string.login_required_button)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Neutral10,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.img_oguri_hello),
                        contentDescription = null,
                        modifier = Modifier.size(180.dp),
                    )

                    Text(
                        text =
                            titleText.getStyledText(
                                style = OguriTheme.typography.cardTitle.copy(color = Mint70),
                                highlightedTitleText,
                            ),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral90,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .background(color = Mint70, shape = RoundedCornerShape(8.dp))
                                .noRippleClickable(onClick = onLoginClick)
                                .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = loginButtonText,
                            style = OguriTheme.typography.cardTitle,
                            color = Neutral0,
                        )
                    }
                }

                Image(
                    painter = painterResource(Res.drawable.btn_exit),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .noRippleClickable(onClick = onDismissRequest),
                )
            }
        }
    }
}
