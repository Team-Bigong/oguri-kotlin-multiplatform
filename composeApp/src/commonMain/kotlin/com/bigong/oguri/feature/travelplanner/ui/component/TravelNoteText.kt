package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint90
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelVisitDetails
import io.ktor.http.Url
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_manual_note_placeholder
import org.jetbrains.compose.resources.StringResource

@Composable
internal fun TravelNoteField(
    note: String,
    onChange: (String) -> Unit,
    label: StringResource,
    enabled: Boolean,
    supportingText: StringResource? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TravelPlaceField(
            value = note,
            onChange = onChange,
            label = label,
            placeholder = Res.string.travel_manual_note_placeholder,
            limit = TravelVisitDetails.MAX_NOTE_LENGTH,
            enabled = enabled,
            multiline = true,
            supportingText = supportingText,
        )
        TravelNoteText(note, style = OguriTheme.typography.bodySmall, linksOnly = true)
    }
}

@Composable
internal fun TravelNoteText(
    note: String,
    modifier: Modifier = Modifier,
    style: TextStyle = OguriTheme.typography.bodyMedium,
    linksOnly: Boolean = false,
) {
    val links = remember(note) { findTravelNoteLinks(note) }
    if (linksOnly && links.isEmpty()) return

    val linkColor = Mint90
    val annotated = remember(note, links, linkColor, linksOnly) {
        val linkStyles = TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
        buildAnnotatedString {
            var previousEnd = 0
            links.forEachIndexed { index, link ->
                if (!linksOnly) {
                    append(note.substring(previousEnd, link.range.first))
                } else if (index > 0) {
                    append('\n')
                }
                withLink(LinkAnnotation.Url(link.url, linkStyles)) {
                    append(note.substring(link.range))
                }
                previousEnd = link.range.last + 1
            }
            if (!linksOnly) append(note.substring(previousEnd))
        }
    }
    Text(annotated, modifier, style = style, color = Neutral100)
}

internal data class TravelNoteLink(val range: IntRange, val url: String)

private val websiteInNote = Regex("""\b(?:https?://|www\.)[^\s<>"“”‘’「」『』、，。！？；：（）]+""", RegexOption.IGNORE_CASE)

internal fun findTravelNoteLinks(note: String): List<TravelNoteLink> =
    websiteInNote.findAll(note).mapNotNull { match ->
        val text = match.value.trimLinkEnding()
        val url = if (text.startsWith("www.", ignoreCase = true)) {
            "https://$text"
        } else {
            text.substringBefore("://").lowercase() + "://" + text.substringAfter("://", "")
        }
        val authority = url.substringAfter("://", "").substringBefore('/').substringBefore('?').substringBefore('#')
        if (authority.isBlank() || runCatching { Url(url).host }.getOrNull().isNullOrBlank()) {
            null
        } else {
            val start = match.range.first
            TravelNoteLink(start until start + text.length, url)
        }
    }.toList()

private fun String.trimLinkEnding(): String =
    trimEnd('.', ',', ';', ':', '!', '?', '…').let { value ->
        val endingPairs = mapOf(')' to '(', ']' to '[', '}' to '{')
        var end = value.length
        while (end > 0) {
            val closing = value[end - 1]
            val opening = endingPairs[closing] ?: break
            val remaining = value.take(end)
            if (remaining.count { it == closing } <= remaining.count { it == opening }) break
            end--
        }
        value.take(end)
    }
