package com.bigong.oguri.feature.travelplanner.ui.component

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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.travel_latest_saved_values), style = OguriTheme.typography.cardSubtitle)
        fields.forEach { field ->
            val label =
                when (field) {
                    TravelVisitField.DAY -> Res.string.travel_visit_day
                    TravelVisitField.NAME -> Res.string.travel_visit_name
                    TravelVisitField.LOCATION -> Res.string.travel_visit_location
                    TravelVisitField.TIME -> Res.string.travel_visit_time
                    TravelVisitField.NOTE -> Res.string.travel_visit_note
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
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
