package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme

@Composable
fun PlaceDetailDescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = description.toBoldAnnotatedText(),
        style = OguriTheme.typography.bodyLarge,
        color = Neutral90,
        modifier = modifier,
    )
}

private fun String.toBoldAnnotatedText(): AnnotatedString {
    val text = this
    val marker = "**"

    return buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            val startMarkerIndex = text.indexOf(marker, startIndex = currentIndex)
            if (startMarkerIndex == -1) {
                append(text.substring(currentIndex))
                break
            }

            append(text.substring(currentIndex, startMarkerIndex))

            val endMarkerIndex = text.indexOf(marker, startIndex = startMarkerIndex + marker.length)
            if (endMarkerIndex == -1) {
                append(text.substring(startMarkerIndex))
                break
            }

            val boldText = text.substring(startMarkerIndex + marker.length, endMarkerIndex)
            val start = length
            append(boldText)
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = start + boldText.length,
            )
            currentIndex = endMarkerIndex + marker.length
        }
    }
}
