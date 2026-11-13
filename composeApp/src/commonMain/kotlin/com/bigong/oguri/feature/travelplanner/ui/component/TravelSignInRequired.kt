package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_guest_plans
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_sign_in
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelSignInRequired(
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    TravelPlannerPage(title = stringResource(Res.string.travel_list), onBack = onBack) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.travel_guest_plans))
            Button(onClick = onSignIn) { Text(stringResource(Res.string.travel_sign_in)) }
        }
    }
}
