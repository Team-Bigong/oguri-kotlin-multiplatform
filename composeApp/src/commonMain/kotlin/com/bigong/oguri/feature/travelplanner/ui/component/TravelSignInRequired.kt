package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.travelplanner.ui.TravelPlansEmpty
import com.bigong.oguri.feature.travelplanner.ui.TravelPlansHeading
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.img_oguri_hello
import oguri.composeapp.generated.resources.travel_guest_plans
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_login_action
import oguri.composeapp.generated.resources.travel_login_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelSignInRequired(onSignIn: () -> Unit, onBack: () -> Unit) {
    TravelPlannerPage(title = stringResource(Res.string.travel_list), onBack = onBack, showTopDivider = false) {
        TravelSignInContent(onSignIn = onSignIn, showPageTitle = false)
    }
}

@Composable
internal fun ColumnScope.TravelSignInContent(
    onSignIn: () -> Unit,
    showPageTitle: Boolean,
) {
    TravelPlansHeading(showPageTitle = showPageTitle)
    TravelPlansEmpty(
        illustration = Res.drawable.img_oguri_hello,
        message = stringResource(Res.string.travel_guest_plans),
        actionLabel = stringResource(Res.string.travel_login_action),
        onAction = onSignIn,
        title = stringResource(Res.string.travel_login_title),
        modifier = Modifier.weight(1f),
    )
}
