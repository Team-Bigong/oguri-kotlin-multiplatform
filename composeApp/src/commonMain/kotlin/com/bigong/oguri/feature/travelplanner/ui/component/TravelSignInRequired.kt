package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.travelplanner.ui.TravelPlansEmpty
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.img_oguri_hello
import oguri.composeapp.generated.resources.travel_guest_plans
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_list_description
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
    val colors = OguriTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showPageTitle) {
            Text(stringResource(Res.string.travel_list), style = OguriTheme.typography.sectionTitle)
        }
        Text(stringResource(Res.string.travel_list_description), style = OguriTheme.typography.cardSubtitle, color = colors.textTertiary)
    }
    HorizontalDivider(color = colors.borderSubtle)
    TravelPlansEmpty(
        illustration = Res.drawable.img_oguri_hello,
        message = stringResource(Res.string.travel_guest_plans),
        actionLabel = stringResource(Res.string.travel_login_action),
        onAction = onSignIn,
        title = stringResource(Res.string.travel_login_title),
        modifier = Modifier.weight(1f),
    )
}
