package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue

internal const val TRAVEL_SEARCH_QUERY_LIMIT: Int = 120

@Composable
internal fun TravelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    limit: Int,
    textStyle: TextStyle,
    cursorBrush: Brush,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
) {
    var input by remember { mutableStateOf(TextFieldValue(value)) }
    var previousValue by remember { mutableStateOf(value) }
    val currentInput =
        if (value != previousValue && value != input.text) {
            TextFieldValue(value, selection = TextRange(value.length))
        } else {
            input
        }
    SideEffect {
        previousValue = value
        input = currentInput
    }
    BasicTextField(
        value = currentInput,
        onValueChange = { nextInput ->
            if (nextInput.text.length <= limit) {
                val textChanged = nextInput.text != input.text
                // Feed selection and composition back to the IME before publishing plain text.
                input = nextInput
                if (textChanged) onValueChange(nextInput.text)
            }
        },
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        interactionSource = interactionSource,
        decorationBox = decorationBox,
    )
}
