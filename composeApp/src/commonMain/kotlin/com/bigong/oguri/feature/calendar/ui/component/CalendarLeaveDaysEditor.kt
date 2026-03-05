package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_dialog_cancel
import oguri.composeapp.generated.resources.calendar_dialog_confirm
import oguri.composeapp.generated.resources.calendar_dialog_leave_days_title
import oguri.composeapp.generated.resources.ic_pen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val LEAVE_DAYS_EDITOR_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun CalendarLeaveDaysEditor(
    leaveDays: Int,
    onLeaveDaysChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Neutral0, shape = LEAVE_DAYS_EDITOR_SHAPE)
                .border(width = 1.dp, color = Mint70, shape = LEAVE_DAYS_EDITOR_SHAPE)
                .noRippleClickable(onClick = { isDialogVisible = true })
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = leaveDays.toString(),
                style = OguriTheme.typography.cardTitle,
                color = Mint70,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Image(
            painter = painterResource(resource = Res.drawable.ic_pen),
            contentDescription = null,
        )
    }

    if (isDialogVisible) {
        LeaveDaysInputDialog(
            initialLeaveDays = leaveDays,
            onDismiss = { isDialogVisible = false },
            onConfirm = { nextLeaveDays ->
                onLeaveDaysChanged(nextLeaveDays)
                isDialogVisible = false
            },
        )
    }
}

@Composable
private fun LeaveDaysInputDialog(
    initialLeaveDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var inputText by remember(initialLeaveDays) { mutableStateOf(initialLeaveDays.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.calendar_dialog_leave_days_title),
                style = OguriTheme.typography.cardSubtitle,
            )
        },
        text = {
            TextField(
                value = inputText,
                onValueChange = { nextText ->
                    if (nextText.all { character -> character.isDigit() }) {
                        inputText = nextText
                    }
                },
                textStyle = OguriTheme.typography.bodyLarge,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Neutral5,
                        unfocusedContainerColor = Neutral5,
                        disabledContainerColor = Neutral5,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedLeaveDays = inputText.toIntOrNull() ?: initialLeaveDays
                    onConfirm(parsedLeaveDays.coerceIn(minimumValue = 1, maximumValue = 30))
                },
            ) {
                Text(
                    text = stringResource(Res.string.calendar_dialog_confirm),
                    color = Mint70,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.calendar_dialog_cancel),
                    color = Neutral40,
                )
            }
        },
    )
}
