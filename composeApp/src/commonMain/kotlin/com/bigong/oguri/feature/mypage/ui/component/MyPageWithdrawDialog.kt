package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.util.extension.dismissKeyboardOnOutsideTouch
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_alert
import oguri.composeapp.generated.resources.mypage_dialog_cancel
import oguri.composeapp.generated.resources.mypage_dialog_confirm
import oguri.composeapp.generated.resources.mypage_withdraw_dialog_description
import oguri.composeapp.generated.resources.mypage_withdraw_dialog_instruction
import oguri.composeapp.generated.resources.mypage_withdraw_dialog_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageWithdrawDialog(
    inputText: String,
    targetPhrase: String,
    isConfirmEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .dismissKeyboardOnOutsideTouch()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_alert),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = stringResource(Res.string.mypage_withdraw_dialog_title),
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral90,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                Text(
                    text = stringResource(Res.string.mypage_withdraw_dialog_description),
                    style = OguriTheme.typography.bodyMedium,
                    color = Orange50,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
                Text(
                    text = stringResource(Res.string.mypage_withdraw_dialog_instruction),
                    style = OguriTheme.typography.bodyMedium,
                    color = Neutral50,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Text(
                    text = targetPhrase,
                    style = OguriTheme.typography.bodyLarge,
                    color = Mint70,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(horizontal = 8.dp)
                            .border(width = 1.dp, color = Neutral90, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle =
                            OguriTheme.typography.bodyLarge.merge(
                                TextStyle(color = Neutral90),
                            ),
                        decorationBox = { innerTextField ->
                            if (inputText.isBlank()) {
                                Text(
                                    text = targetPhrase,
                                    style = OguriTheme.typography.bodyLarge,
                                    color = Neutral40,
                                )
                            }
                            innerTextField()
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                ) {
                    val confirmButtonBackground = if (isConfirmEnabled) Mint70 else Neutral10
                    val confirmButtonTextColor = if (isConfirmEnabled) Neutral0 else Neutral40

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(color = confirmButtonBackground, shape = RoundedCornerShape(8.dp))
                                .then(
                                    if (isConfirmEnabled) {
                                        Modifier.noRippleClickable(onClick = onConfirmClick)
                                    } else {
                                        Modifier
                                    },
                                ).padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.mypage_dialog_confirm),
                            style = OguriTheme.typography.bodyMedium,
                            color = confirmButtonTextColor,
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(color = Neutral10, shape = RoundedCornerShape(8.dp))
                                .noRippleClickable(onClick = onDismissRequest)
                                .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.mypage_dialog_cancel),
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral90,
                        )
                    }
                }
            }
        }
    }
}
