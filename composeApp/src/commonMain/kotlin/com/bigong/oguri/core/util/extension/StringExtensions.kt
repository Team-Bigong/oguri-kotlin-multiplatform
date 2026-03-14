package com.bigong.oguri.core.util.extension

import androidx.compose.ui.text.AnnotatedString
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
