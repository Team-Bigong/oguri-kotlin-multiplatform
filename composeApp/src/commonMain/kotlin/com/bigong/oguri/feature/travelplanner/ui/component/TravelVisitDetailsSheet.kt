package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_offline_snapshot
import oguri.composeapp.generated.resources.travel_visit_details
import oguri.composeapp.generated.resources.travel_visit_local_time
import oguri.composeapp.generated.resources.travel_visit_note_shared
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelVisitDetailsSheet(
    visit: TravelVisit,
    title: String,
    dates: TravelDates,
    place: TravelPlace?,
    offline: Boolean,
    onDismiss: () -> Unit,
    onDirectionsOpened: () -> Unit,
) {
    ModalBottomSheet(containerColor = OguriTheme.colors.backgroundPrimary, onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.travel_visit_details), style = OguriTheme.typography.sectionTitle)
            if (offline) Text(stringResource(Res.string.travel_offline_snapshot), style = OguriTheme.typography.bodySmall)
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(title, style = OguriTheme.typography.cardTitle)
                    Text(travelDayLabel(dates, visit.dayNumber))
                    visit.details.category?.let { Text(stringResource(it.label())) }
                    (visit.details.location ?: place?.address)?.let { Text(it) }
                    visit.details.note?.let { note ->
                        Text(note)
                        Text(stringResource(Res.string.travel_visit_note_shared), style = OguriTheme.typography.bodySmall)
                    }
                }
            }
            if (place != null) GooglePlacesAttribution(place.attributions)
            TravelDirectionsButton(visit.details.googlePlaceId, place?.name ?: visit.details.location, onOpened = onDirectionsOpened)
            TravelTextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.travel_cancel)) }
        }
    }
}
