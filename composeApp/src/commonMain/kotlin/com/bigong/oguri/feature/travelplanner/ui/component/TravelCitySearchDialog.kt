package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.util.extension.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import oguri.composeapp.generated.resources.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_choose_city
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.travel_search_cities_empty
import org.jetbrains.compose.resources.stringResource

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
) {
    val colors = OguriTheme.colors
    val searchLabel = stringResource(Res.string.travel_search_cities)
    ModalBottomSheet(scrimColor = com.bigong.oguri.core.designsystem.Neutral90.copy(alpha = .45f),
        dragHandle = { TravelSheetHandle() },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        containerColor = colors.backgroundSecondary,
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.travel_search_cities), Modifier.weight(1f), style = OguriTheme.typography.cardTitle, color = colors.textPrimary)
                TravelIconButton(onClick = onDismiss) { Icon(painterResource(Res.drawable.ic_travel_close), stringResource(Res.string.travel_search_close), Modifier.size(24.dp)) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                BasicTextField(
                    value = state.query, onValueChange = { if (it.length <= 120) onQuery(it) }, singleLine = true,
                    textStyle = OguriTheme.typography.bodyLarge.copy(color = colors.textPrimary), cursorBrush = SolidColor(colors.textPrimary),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = searchLabel },
                    decorationBox = { field ->
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).background(colors.surfaceCard, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.borderStrong, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(painterResource(Res.drawable.ic_travel_search), null, Modifier.size(20.dp), tint = colors.textTertiary)
                            Box(Modifier.weight(1f)) {
                                if (state.query.isEmpty()) Text(stringResource(Res.string.travel_city_search_placeholder), style = OguriTheme.typography.bodyMedium, color = colors.textTertiary)
                                field()
                            }
                        }
                    },
                )
                when {
                    state.loading -> TravelLoading(Modifier.fillMaxWidth())
                    state.failure != null -> TravelInlineFailure(state.failure, onRetry = { onQuery(state.query) })
                    state.query.trim().length >= 2 && state.suggestions.isEmpty() -> Text(stringResource(Res.string.travel_search_cities_empty), style = OguriTheme.typography.bodyMedium)
                }
                if (state.query.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.travel_recommended_cities), style = OguriTheme.typography.labelMedium, color = colors.textTertiary)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recommendedCities.forEach { city ->
                                val selected = city in selectedCities || city == state.selected?.label
                                Box(Modifier.selectable(selected, enabled = !state.loading, role = Role.Checkbox, onClick = { onChooseRecommendedCity(city) })
                                    .background(if (selected) Mint40 else colors.surfaceCard, CircleShape)
                                    .then(if (selected) Modifier else Modifier.border(1.dp, colors.borderSubtle, CircleShape))
                                    .padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(city, style = OguriTheme.typography.bodyMedium, color = if (selected) colors.textPrimary else colors.textSecondary)
                                }
                            }
                        }
                    }
                    Text(stringResource(Res.string.travel_city_search_hint), Modifier.padding(horizontal = 4.dp), style = OguriTheme.typography.caption, color = colors.textTertiary)
                }
                state.suggestions.forEach { suggestion ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).noRippleClickable(enabled = !state.loading, onClick = { onChoose(suggestion) }), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painterResource(Res.drawable.ic_travel_location), null, Modifier.size(20.dp), tint = colors.textTertiary)
                        Text(suggestion.label, style = OguriTheme.typography.bodyLarge, color = colors.textPrimary)
                    }
                }
                if (state.suggestions.isNotEmpty()) GooglePlacesAttribution(emptyList())
            }
        }
    }
}
