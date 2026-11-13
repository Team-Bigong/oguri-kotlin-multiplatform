package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_create
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_empty_plans
import oguri.composeapp.generated.resources.travel_guest_plans
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_past
import oguri.composeapp.generated.resources.travel_past_collapsed
import oguri.composeapp.generated.resources.travel_past_expanded
import oguri.composeapp.generated.resources.travel_sign_in
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun TravelPlansScreen(
    state: TravelPlannerUiState,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit,
) {
    TravelPlannerPage(title = stringResource(Res.string.travel_list), onBack = onBack) {
        val failure = state.failure
        val plans = state.plans
        when {
            !signedIn ->
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(Res.string.travel_guest_plans))
                    Button(onClick = onCreate) { Text(stringResource(Res.string.travel_create)) }
                    TextButton(onClick = onSignIn) { Text(stringResource(Res.string.travel_sign_in)) }
                }
            failure != null -> TravelLoadFailure(failure, onRetry)
            plans == null -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            plans.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(Res.string.travel_empty_plans))
                    Button(onClick = onCreate) { Text(stringResource(Res.string.travel_create)) }
                }
            }
            else -> {
                var showPast by rememberSaveable { mutableStateOf(false) }
                val now = Clock.System.now()
                val (past, current) = plans.partition { plan -> plan.hasEnded { now.toLocalDateTime(it).date } }
                val pastState = stringResource(if (showPast) Res.string.travel_past_expanded else Res.string.travel_past_collapsed)

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(current, key = TravelPlanSummary::id) { plan ->
                        TravelPlanCard(plan) { onOpen(plan.id) }
                    }
                    if (past.isNotEmpty()) {
                        item {
                            TextButton(
                                onClick = { showPast = !showPast },
                                modifier = Modifier.semantics { stateDescription = pastState },
                            ) {
                                Text(stringResource(Res.string.travel_past))
                            }
                        }
                        if (showPast) {
                            items(past.asReversed(), key = TravelPlanSummary::id) { plan ->
                                TravelPlanCard(plan) { onOpen(plan.id) }
                            }
                        }
                    }
                    item {
                        TextButton(onClick = onCreate) { Text(stringResource(Res.string.travel_create)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelPlanCard(
    plan: TravelPlanSummary,
    onClick: () -> Unit,
) {
    val details = plan.details
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(details.title, style = MaterialTheme.typography.titleMedium)
            Text(details.city)
            Text(
                stringResource(Res.string.travel_dates_value, details.dates.start.toString(), details.dates.endInclusive.toString()),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
