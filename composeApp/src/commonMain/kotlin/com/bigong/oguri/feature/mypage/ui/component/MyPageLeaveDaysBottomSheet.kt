package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_exit
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_done
import oguri.composeapp.generated.resources.mypage_leave_days_field_preferred
import oguri.composeapp.generated.resources.mypage_leave_days_field_remaining
import oguri.composeapp.generated.resources.mypage_leave_days_sheet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val BOTTOM_SHEET_SHAPE = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
private val FIELD_SHAPE = RoundedCornerShape(size = 8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageLeaveDaysBottomSheet(
    currentRemainingLeaveDays: Int,
    currentPreferredLeaveDays: Int,
    onDismissRequest: () -> Unit,
    onSubmit: (Int, Int) -> Unit,
) {
    var remainingLeaveDaysInput by remember(currentRemainingLeaveDays) { mutableStateOf(currentRemainingLeaveDays.toString()) }
    var preferredLeaveDaysInput by remember(currentPreferredLeaveDays) { mutableStateOf(currentPreferredLeaveDays.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(currentRemainingLeaveDays, currentPreferredLeaveDays) {
        remainingLeaveDaysInput = currentRemainingLeaveDays.toString()
        preferredLeaveDaysInput = currentPreferredLeaveDays.toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = BOTTOM_SHEET_SHAPE,
        dragHandle = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier =
                        Modifier
                            .size(width = 68.dp, height = 4.dp)
                            .background(color = Neutral50, shape = RoundedCornerShape(size = 100.dp)),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        containerColor = Neutral0,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.mypage_leave_days_sheet_title),
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral90,
                )
                Image(
                    painter = painterResource(resource = Res.drawable.btn_exit),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).noRippleClickable(onClick = onDismissRequest),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LeaveDaysTextField(
                titleText = stringResource(Res.string.mypage_leave_days_field_remaining),
                value = remainingLeaveDaysInput,
                onValueChange = { remainingLeaveDaysInput = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            LeaveDaysTextField(
                titleText = stringResource(Res.string.mypage_leave_days_field_preferred),
                value = preferredLeaveDaysInput,
                onValueChange = { preferredLeaveDaysInput = it },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = Mint70, shape = RoundedCornerShape(size = 8.dp))
                        .noRippleClickable(
                            onClick = {
                                val remainingLeaveDays = remainingLeaveDaysInput.toIntOrNull() ?: return@noRippleClickable
                                val preferredLeaveDays = preferredLeaveDaysInput.toIntOrNull() ?: return@noRippleClickable
                                onSubmit(remainingLeaveDays, preferredLeaveDays)
                            },
                        ).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.calendar_leave_days_sheet_done),
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral0,
                )
            }
        }
    }
}

@Composable
private fun LeaveDaysTextField(
    titleText: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titleText,
            style = OguriTheme.typography.bodyMedium,
            color = Neutral50,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = Mint50.copy(alpha = 0.5f), shape = FIELD_SHAPE)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { nextText ->
                    onValueChange(nextText.filter { character -> character.isDigit() }.take(2))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle =
                    TextStyle(
                        color = Neutral90,
                    ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            style = OguriTheme.typography.bodyLarge,
                            color = Neutral40,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}
