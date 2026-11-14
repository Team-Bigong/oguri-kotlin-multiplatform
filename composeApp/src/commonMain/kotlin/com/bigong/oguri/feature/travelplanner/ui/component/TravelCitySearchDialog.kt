package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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

@Composable
internal fun TravelCitySearchDialog(
    state: TravelCitySearchState,
    recommendedCities: List<String>,
    onQuery: (String) -> Unit,
    onChoose: (TravelCitySuggestion) -> Unit,
    onChooseRecommendedCity: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        containerColor = Neutral0,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_search_cities)) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { if (it.length <= 120) onQuery(it) },
                    label = { Text(stringResource(Res.string.travel_search_cities)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = OguriTheme.typography.bodyLarge,
                    colors = TravelFieldColors(),
                )
                when {
                    state.loading -> TravelLoading(Modifier.fillMaxWidth())
                    state.failure != null -> TravelInlineFailure(state.failure, onRetry = { onQuery(state.query) })
                    state.query.trim().length >= 2 && state.suggestions.isEmpty() ->
                        Text(stringResource(Res.string.travel_search_cities_empty))
                }
                LazyColumn(Modifier.heightIn(max = 220.dp)) {
                    if (state.query.isBlank()) {
                        item {
                            Text(
                                stringResource(Res.string.travel_choose_city),
                                style = OguriTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        items(recommendedCities) { city ->
                            TravelTextButton(onClick = { onChooseRecommendedCity(city) }) {
                                Text(city, color = OguriTheme.colors.textPrimary)
                            }
                        }
                    }
                    items(state.suggestions, key = TravelCitySuggestion::id) { suggestion ->
                        TravelTextButton(onClick = { onChoose(suggestion) }, enabled = !state.loading) {
                            Text(suggestion.label)
                        }
                    }
                }
                if (state.suggestions.isNotEmpty()) GooglePlacesAttribution(emptyList())
            }
        },
        confirmButton = { TravelTextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) } },
    )
}
