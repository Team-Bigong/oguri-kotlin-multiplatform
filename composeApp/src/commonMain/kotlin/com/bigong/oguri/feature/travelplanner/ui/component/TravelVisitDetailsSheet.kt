package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import oguri.composeapp.generated.resources.*
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
import com.bigong.oguri.core.designsystem.Neutral100
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissSheet = rememberTravelSheetDismiss(sheetState, onDismiss)
    ModalBottomSheet(scrimColor = TravelSheetScrim, dragHandle = { TravelSheetHandle() }, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp), containerColor = OguriTheme.colors.backgroundSecondary, contentColor = Neutral100, onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().height(430.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.travel_visit_details), style = OguriTheme.typography.cardTitle)
                Text(stringResource(if (offline) Res.string.travel_offline_readonly else Res.string.travel_readonly), Modifier.weight(1f), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                TravelIconButton(onClick = dismissSheet) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(22.dp)) }
            }
            SelectionContainer(Modifier.weight(1f)) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = OguriTheme.typography.sectionTitle, color = Neutral100)
                        val location = visit.details.location ?: place?.address
                        val attribution = stringResource(Res.string.travel_google_maps_attribution).takeIf { place != null }
                        Text(listOfNotNull(location, attribution).joinToString(" · "), style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textTertiary)
                    }
                    TravelVisitInfoRow(stringResource(Res.string.travel_visit_day), travelDayLabel(dates, visit.dayNumber))
                    visit.details.category?.let { TravelVisitInfoRow(stringResource(Res.string.travel_category_label), stringResource(it.label())) }
                    HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                    visit.details.note?.let { note ->
                        Text(stringResource(Res.string.travel_note_label), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        val shape = RoundedCornerShape(8.dp)
                        TravelNoteText(
                            note,
                            Modifier.fillMaxWidth().background(OguriTheme.colors.surfaceCard, shape)
                                .border(1.dp, OguriTheme.colors.borderSubtle, shape).padding(12.dp),
                        )
                    }
                    if (place?.attributions?.isNotEmpty() == true) GooglePlacesAttribution(place.attributions, includeGoogleBranding = false)
                }
            }
            HorizontalDivider(color = OguriTheme.colors.borderSubtle)
            Box(Modifier.fillMaxWidth().padding(20.dp)) {
                TravelDirectionsButton(visit.details.googlePlaceId, place?.name ?: visit.details.location, outlined = true, modifier = Modifier.fillMaxWidth(), onOpened = onDirectionsOpened)
            }
        }
    }
}

@Composable
private fun TravelVisitInfoRow(label: String, value: String) {
    HorizontalDivider(color = OguriTheme.colors.borderSubtle)
    Row(Modifier.fillMaxWidth().heightIn(min = 32.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
        Text(value, style = OguriTheme.typography.bodyMedium, color = Neutral100)
    }
}
