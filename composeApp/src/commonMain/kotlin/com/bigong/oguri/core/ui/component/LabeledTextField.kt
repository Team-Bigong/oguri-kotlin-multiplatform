package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme

private val TEXT_FIELD_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun LabeledTextField(
    labelText: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    unitText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = labelText,
            style = OguriTheme.typography.labelMedium,
            color = Neutral50,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(width = 1.dp, color = Neutral90, shape = TEXT_FIELD_SHAPE),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = OguriTheme.typography.bodyLarge.copy(color = Neutral90),
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(Neutral90),
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = if (unitText != null) 42.dp else 16.dp),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholderText,
                            style = OguriTheme.typography.bodyLarge,
                            color = Neutral50,
                        )
                    }
                    innerTextField()
                },
            )

            if (unitText != null) {
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
        }
    }
}
