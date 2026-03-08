package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val ALERT_DIALOG_SHAPE = RoundedCornerShape(size = 8.dp)

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
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Image(
                painter = painterResource(iconResource),
                contentDescription = null,
            )
        },
        title = {
            Text(
                text = titleText,
                style = OguriTheme.typography.sectionTitle,
                color = Neutral70,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = messageText,
                style = OguriTheme.typography.cardTitle,
                color = Orange50,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(color = Mint70, shape = ALERT_DIALOG_SHAPE)
                            .noRippleClickable(onClick = onConfirmClick)
                            .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = confirmButtonText,
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral0,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(color = Neutral10, shape = ALERT_DIALOG_SHAPE)
                            .noRippleClickable(onClick = onCancelClick)
                            .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = cancelButtonText,
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral70,
                    )
                }
            }
        },
        dismissButton = {},
        shape = ALERT_DIALOG_SHAPE,
        containerColor = Neutral0,
        titleContentColor = Neutral90,
        textContentColor = Neutral70,
    )
}
