package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_travel_close
import oguri.composeapp.generated.resources.ic_travel_location
import oguri.composeapp.generated.resources.ic_travel_search
import oguri.composeapp.generated.resources.travel_city_aewol
import oguri.composeapp.generated.resources.travel_city_barcelona
import oguri.composeapp.generated.resources.travel_city_blue_mountains
import oguri.composeapp.generated.resources.travel_city_girona
import oguri.composeapp.generated.resources.travel_city_ho_chi_minh
import oguri.composeapp.generated.resources.travel_city_incheon
import oguri.composeapp.generated.resources.travel_city_jeju
import oguri.composeapp.generated.resources.travel_city_kamakura
import oguri.composeapp.generated.resources.travel_city_keelung
import oguri.composeapp.generated.resources.travel_city_kyoto
import oguri.composeapp.generated.resources.travel_city_my_tho
import oguri.composeapp.generated.resources.travel_city_nara
import oguri.composeapp.generated.resources.travel_city_osaka
import oguri.composeapp.generated.resources.travel_city_paris
import oguri.composeapp.generated.resources.travel_city_saint_germain_en_laye
import oguri.composeapp.generated.resources.travel_city_seogwipo
import oguri.composeapp.generated.resources.travel_city_seoul
import oguri.composeapp.generated.resources.travel_city_sitges
import oguri.composeapp.generated.resources.travel_city_suwon
import oguri.composeapp.generated.resources.travel_city_sydney
import oguri.composeapp.generated.resources.travel_city_taipei
import oguri.composeapp.generated.resources.travel_city_tamsui
import oguri.composeapp.generated.resources.travel_city_tokyo
import oguri.composeapp.generated.resources.travel_city_versailles
import oguri.composeapp.generated.resources.travel_city_vung_tau
import oguri.composeapp.generated.resources.travel_city_wollongong
import oguri.composeapp.generated.resources.travel_city_yokohama
import oguri.composeapp.generated.resources.travel_city_search_placeholder
import oguri.composeapp.generated.resources.travel_nearby_cities
import oguri.composeapp.generated.resources.travel_recommended_cities
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.travel_search_cities_empty
import oguri.composeapp.generated.resources.travel_search_cities_empty_hint
import oguri.composeapp.generated.resources.travel_search_close
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val VISIBLE_CITY_RESULTS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelCitySearchDialog(
    state: TravelCitySearchState,
    recommendedCities: List<String>,
    onQuery: (String) -> Unit,
    onChoose: (TravelCitySuggestion) -> Unit,
    onChooseRecommendedCity: (String) -> Unit,
    onDismiss: () -> Unit,
    selectedCities: List<String> = emptyList(),
    nearbyCities: List<String> = emptyList(),
) {
    val colors = OguriTheme.colors
    val searchLabel = stringResource(Res.string.travel_search_cities)
    ModalBottomSheet(
        scrimColor = TravelSheetScrim,
        dragHandle = null,
        sheetGesturesEnabled = false,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        containerColor = colors.backgroundSecondary,
        contentColor = colors.textPrimary,
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).imePadding()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TravelSheetHandle() }
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, top = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_search_cities), Modifier.weight(1f), style = OguriTheme.typography.cardTitle, color = colors.textPrimary)
                TravelIconButton(onClick = onDismiss) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(24.dp)) }
            }
            Column(Modifier.weight(1f).padding(start = 20.dp, end = 20.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                TravelTextField(
                    value = state.query, onValueChange = onQuery, limit = TRAVEL_SEARCH_QUERY_LIMIT,
                    textStyle = travelTextStyle(15, 18).copy(color = colors.textPrimary), cursorBrush = SolidColor(colors.textPrimary),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = searchLabel },
                    decorationBox = { field ->
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).background(colors.surfaceCard, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.borderStrong, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(painterResource(Res.drawable.ic_travel_search), null, Modifier.size(20.dp), tint = colors.textTertiary)
                            Box(Modifier.weight(1f)) {
                                if (state.query.isEmpty()) Text(stringResource(Res.string.travel_city_search_placeholder), style = travelTextStyle(15, 18), color = colors.textTertiary)
                                field()
                            }
                        }
                    },
                )
                when {
                    state.loading && state.suggestions.isEmpty() -> TravelSearchResultsSkeleton()
                    state.failure != null -> TravelInlineFailure(state.failure, onRetry = { onQuery(state.query) })
                    state.query.trim().length >= 2 && state.suggestions.isEmpty() -> Column(
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(Res.string.travel_search_cities_empty), style = OguriTheme.typography.cardSubtitle)
                        Text(
                            stringResource(Res.string.travel_search_cities_empty_hint),
                            style = OguriTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                }
                if (state.query.isBlank()) {
                    Column(
                        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val nearby = nearbyCities.isNotEmpty()
                        Text(
                            stringResource(if (nearby) Res.string.travel_nearby_cities else Res.string.travel_recommended_cities),
                            style = OguriTheme.typography.labelMedium,
                            color = colors.textTertiary,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            (if (nearby) nearbyCities else recommendedCities).forEach { city ->
                                val selected = city in selectedCities || city == state.selected?.label
                                Box(Modifier.selectable(selected, enabled = !state.loading, role = Role.Checkbox, onClick = { onChooseRecommendedCity(city) })
                                    .background(if (selected) OguriTheme.colors.mintContainer else colors.surfaceCard, CircleShape)
                                    .then(if (selected) Modifier else Modifier.border(1.dp, colors.borderSubtle, CircleShape))
                                    .padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(city, style = OguriTheme.typography.bodyMedium, color = if (selected) colors.textPrimary else colors.textSecondary)
                                }
                            }
                        }
                    }
                }
                if (state.suggestions.isNotEmpty()) {
                    val resultStyle = travelTextStyle(15, 18)
                    val rowHeight = with(LocalDensity.current) { (resultStyle.lineHeight.toDp() * 2 + 12.dp).coerceAtLeast(48.dp) }
                    LazyColumn(
                        Modifier.weight(1f, fill = false).heightIn(max = rowHeight * VISIBLE_CITY_RESULTS),
                    ) {
                        items(state.suggestions, key = { it.id }) { suggestion ->
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = rowHeight)
                                    .noRippleClickable(enabled = !state.loading, onClick = { onChoose(suggestion) })
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(painterResource(Res.drawable.ic_travel_location), null, Modifier.size(20.dp), tint = colors.textTertiary)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(suggestion.name, style = resultStyle, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    suggestion.regionLabel?.let { region ->
                                        Text(region, style = OguriTheme.typography.caption, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun cityLabel(city: TravelCity): String =
    stringResource(
        when (city) {
            TravelCity.SEOUL -> Res.string.travel_city_seoul
            TravelCity.JEJU -> Res.string.travel_city_jeju
            TravelCity.OSAKA -> Res.string.travel_city_osaka
            TravelCity.TOKYO -> Res.string.travel_city_tokyo
            TravelCity.TAIPEI -> Res.string.travel_city_taipei
            TravelCity.PARIS -> Res.string.travel_city_paris
            TravelCity.HO_CHI_MINH -> Res.string.travel_city_ho_chi_minh
            TravelCity.SYDNEY -> Res.string.travel_city_sydney
            TravelCity.BARCELONA -> Res.string.travel_city_barcelona
            TravelCity.INCHEON -> Res.string.travel_city_incheon
            TravelCity.SUWON -> Res.string.travel_city_suwon
            TravelCity.SEOGWIPO -> Res.string.travel_city_seogwipo
            TravelCity.AEWOL -> Res.string.travel_city_aewol
            TravelCity.KYOTO -> Res.string.travel_city_kyoto
            TravelCity.NARA -> Res.string.travel_city_nara
            TravelCity.YOKOHAMA -> Res.string.travel_city_yokohama
            TravelCity.KAMAKURA -> Res.string.travel_city_kamakura
            TravelCity.TAMSUI -> Res.string.travel_city_tamsui
            TravelCity.KEELUNG -> Res.string.travel_city_keelung
            TravelCity.VERSAILLES -> Res.string.travel_city_versailles
            TravelCity.SAINT_GERMAIN_EN_LAYE -> Res.string.travel_city_saint_germain_en_laye
            TravelCity.VUNG_TAU -> Res.string.travel_city_vung_tau
            TravelCity.MY_THO -> Res.string.travel_city_my_tho
            TravelCity.BLUE_MOUNTAINS -> Res.string.travel_city_blue_mountains
            TravelCity.WOLLONGONG -> Res.string.travel_city_wollongong
            TravelCity.SITGES -> Res.string.travel_city_sitges
            TravelCity.GIRONA -> Res.string.travel_city_girona
        },
    )
