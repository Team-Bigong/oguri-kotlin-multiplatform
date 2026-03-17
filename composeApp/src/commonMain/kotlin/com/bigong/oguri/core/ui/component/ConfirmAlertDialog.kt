package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.util.extension.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ConfirmAlertDialog(
    iconResource: DrawableResource,
    titleText: String,
    messageText: String,
    confirmButtonText: String,
    cancelButtonText: String,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
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
            color = Neutral0,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(iconResource),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )

                Text(
                    text = titleText,
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral90,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )

                if (messageText.isNotBlank()) {
                    Text(
                        text = messageText,
                        style = OguriTheme.typography.bodyMedium,
                        color = Orange50,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = if (messageText.isNotBlank()) 18.dp else 14.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(color = Mint70, shape = RoundedCornerShape(8.dp))
                                .noRippleClickable(onClick = onConfirmClick)
                                .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral0,
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(color = Neutral10, shape = RoundedCornerShape(8.dp))
                                .noRippleClickable(onClick = onCancelClick)
                                .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelButtonText,
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral90,
                        )
                    }
                }
            }
        }
    }
}
