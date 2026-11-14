package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_resume
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelResumeEntry(
    viewModel: TravelPlannerViewModel,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.loadResume() }
    }
    val plan = state.resumePlan ?: return
    Card(
        onClick = { onOpen(plan.id) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.travel_resume), style = MaterialTheme.typography.labelLarge)
            Text(plan.details.title, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    Res.string.travel_dates_value,
                    plan.details.dates.start
                        .toString(),
                    plan.details.dates.endInclusive
                        .toString(),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
