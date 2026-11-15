package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitField
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelConflictComparison(
    draft: TravelVisitDraft,
    dates: TravelDates,
) {
    val fields = draft.latestDifferences()
    val latest = draft.visit ?: return
    if (fields.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().background(com.bigong.oguri.core.designsystem.Orange5, RoundedCornerShape(8.dp))
        .border(1.dp, com.bigong.oguri.core.designsystem.Orange20, RoundedCornerShape(8.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.travel_visit_conflict_title), style = OguriTheme.typography.cardSubtitle)
        Text(stringResource(Res.string.travel_visit_conflict_description), style = OguriTheme.typography.caption, color = OguriTheme.colors.textSecondary)
        fields.forEach { field ->
            val label =
                when (field) {
                    TravelVisitField.DAY -> Res.string.travel_visit_day
                    TravelVisitField.NAME -> Res.string.travel_visit_name
                    TravelVisitField.LOCATION -> Res.string.travel_visit_location
                    TravelVisitField.TIME -> Res.string.travel_visit_time
                    TravelVisitField.NOTE -> Res.string.travel_note_label
                    TravelVisitField.CATEGORY -> Res.string.travel_category
                }
            val value =
                when (field) {
                    TravelVisitField.DAY -> travelDayLabel(dates, latest.dayNumber)
                    TravelVisitField.NAME -> latest.details.name
                    TravelVisitField.LOCATION -> latest.details.location
                    TravelVisitField.TIME -> latest.details.localTime?.toString()
                    TravelVisitField.NOTE -> latest.details.note
                    TravelVisitField.CATEGORY -> latest.details.category?.let { stringResource(it.label()) }
                }?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.travel_value_not_set)
            Row(Modifier.fillMaxWidth().background(com.bigong.oguri.core.designsystem.Neutral0, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(label), Modifier.weight(1f), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                val valueStyle = OguriTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.End)
                if (field == TravelVisitField.NOTE) {
                    TravelNoteText(value, Modifier.weight(2f), style = valueStyle)
                } else {
                    Text(value, Modifier.weight(2f), style = valueStyle, color = OguriTheme.colors.textPrimary)
                }
            }
        }
    }
}
