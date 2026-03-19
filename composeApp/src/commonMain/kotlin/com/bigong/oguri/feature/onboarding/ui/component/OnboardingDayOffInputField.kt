package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50

@Composable
fun OnboardingDayOffInputField(
    value: String,
    unitText: String,
    warningText: String?,
    onValueChange: (String) -> Unit,
    onInputCommitted: () -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
) {
    val focusManager = LocalFocusManager.current
    var hasFocus by remember { mutableStateOf(false) }
    val borderColor =
        if (warningText.isNullOrBlank()) {
            Neutral90
        } else {
            Orange50
        }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (!labelText.isNullOrBlank()) {
            Text(
                text = labelText,
                style = OguriTheme.typography.labelMedium,
                color = Neutral50,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp)),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = OguriTheme.typography.bodyLarge.copy(color = Neutral90),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            onInputCommitted()
                        },
                    ),
                cursorBrush = SolidColor(Neutral90),
                modifier =
                    Modifier
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocus = true
                            } else if (hasFocus) {
                                hasFocus = false
                                onInputCommitted()
                            }
                        }
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 42.dp),
            )

            Text(
                text = unitText,
                style = OguriTheme.typography.labelLarge,
                color = Neutral90,
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
            )
        }

        if (!warningText.isNullOrBlank()) {
            Text(
                text = warningText,
                style = OguriTheme.typography.bodyMedium,
                color = Orange50,
                modifier = Modifier.padding(start = 2.dp, top = 8.dp),
            )
        }
    }
}
