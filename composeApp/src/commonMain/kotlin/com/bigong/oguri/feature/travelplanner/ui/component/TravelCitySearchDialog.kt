package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.travel_search_cities_empty
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelCitySearchDialog(
    state: TravelCitySearchState,
    onQuery: (String) -> Unit,
    onChoose: (TravelCitySuggestion) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
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
                )
                when {
                    state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    state.failure != null -> TravelInlineFailure(state.failure, onRetry = { onQuery(state.query) })
                    state.query.trim().length >= 2 && state.suggestions.isEmpty() ->
                        Text(stringResource(Res.string.travel_search_cities_empty))
                }
                LazyColumn(Modifier.heightIn(max = 220.dp)) {
                    items(state.suggestions, key = TravelCitySuggestion::id) { suggestion ->
                        TextButton(onClick = { onChoose(suggestion) }, enabled = !state.loading) {
                            Text(suggestion.label)
                        }
                    }
                }
                if (state.suggestions.isNotEmpty()) GooglePlacesAttribution(emptyList())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) } },
    )
}
