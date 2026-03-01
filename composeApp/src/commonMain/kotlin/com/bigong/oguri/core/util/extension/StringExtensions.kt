package com.bigong.oguri.core.util.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString

fun String.getStyledText(
    style: TextStyle,
    vararg highlightedText: String,
): AnnotatedString =
    buildAnnotatedString {
        append(this@getStyledText)

        val spanStyle = style.toSpanStyle()

        highlightedText.forEach { target ->
            val startIndex = this@getStyledText.indexOf(target)
            if (startIndex != -1) {
                addStyle(
                    style = spanStyle,
                    start = startIndex,
                    end = startIndex + target.length,
                )
            }
        }
    }

@Composable
fun String.getColoredText(
    color: Color,
    vararg highlightedText: String,
): AnnotatedString =
    buildAnnotatedString {
        append(this@getColoredText)

        highlightedText.forEach { target ->
            val startIndex = this@getColoredText.indexOf(target)
            if (startIndex != -1) {
                addStyle(
                    style = SpanStyle(color = color),
                    start = startIndex,
                    end = startIndex + target.length,
                )
            }
        }
    }
