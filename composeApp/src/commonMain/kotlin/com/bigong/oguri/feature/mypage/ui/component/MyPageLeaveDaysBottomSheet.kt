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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType

private val BOTTOM_SHEET_SHAPE = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageLeaveDaysBottomSheet(
    currentLeaveDays: Int,
    onDismissRequest: () -> Unit,
    onSubmit: (Int) -> Unit,
) {
    var inputText by remember(currentLeaveDays) { mutableStateOf(currentLeaveDays.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(currentLeaveDays) {
        inputText = currentLeaveDays.toString()
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
                    text = stringResource(Res.string.calendar_leave_days_sheet_title),
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

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = Mint50.copy(alpha = 0.5f), shape = RoundedCornerShape(size = 8.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { nextText ->
                        inputText = nextText.filter { character -> character.isDigit() }.take(2)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle =
                        TextStyle(
                            color = Neutral90,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                        ),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
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

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = Mint70, shape = RoundedCornerShape(size = 8.dp))
                        .noRippleClickable(
                            onClick = {
                                val leaveDays = inputText.toIntOrNull() ?: return@noRippleClickable
                                onSubmit(leaveDays)
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
