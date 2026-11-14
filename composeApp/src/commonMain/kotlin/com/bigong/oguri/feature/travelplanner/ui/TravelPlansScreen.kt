package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkImage
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.floatingNavigationBarsPadding
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIconButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_right_arrow
import oguri.composeapp.generated.resources.ic_travel_add
import oguri.composeapp.generated.resources.travel_create
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_empty_plans
import oguri.composeapp.generated.resources.travel_guest_plans
import oguri.composeapp.generated.resources.travel_list
import oguri.composeapp.generated.resources.travel_past
import oguri.composeapp.generated.resources.travel_past_collapsed
import oguri.composeapp.generated.resources.travel_past_expanded
import oguri.composeapp.generated.resources.travel_sign_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelPlansScreen(
    state: TravelPlannerUiState,
    loadCover: suspend (TravelPlanSummary) -> String?,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onBack: (() -> Unit)?,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val canCreate = signedIn && state.plans != null && state.failure == null
    TravelPlannerPage(
        title = stringResource(Res.string.travel_list),
        onBack = onBack,
        modifier = Modifier.floatingNavigationBarsPadding(hasFloatingBottomNavigation = onBack == null),
        actions = {
            if (canCreate && state.plans.isNotEmpty()) {
                TravelIconButton(onClick = onCreate) {
                    Icon(painterResource(Res.drawable.ic_travel_add), stringResource(Res.string.travel_create), tint = Neutral100)
                }
            }
        },
    ) {
        val failure = state.failure
        val plans = state.plans
        when {
            !signedIn ->
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(Res.string.travel_guest_plans))
                    TravelPrimaryButton(onClick = onSignIn) { Text(stringResource(Res.string.travel_sign_in)) }
                }
            failure != null -> TravelLoadFailure(failure, onRetry)
            plans == null -> Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(Modifier.fillMaxWidth().height(134.dp))
                TravelLoading(Modifier.fillMaxWidth(0.5f))
            }
            plans.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(Res.string.travel_empty_plans))
                    TravelPrimaryButton(onClick = onCreate) { Text(stringResource(Res.string.travel_create)) }
                }
            }
            else -> {
                var showPast by rememberSaveable { mutableStateOf(false) }
                val now = Clock.System.now()
                val (past, current) = plans.partition { plan -> plan.hasEnded { now.toLocalDateTime(it).date } }
                val pastState = stringResource(if (showPast) Res.string.travel_past_expanded else Res.string.travel_past_collapsed)

                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(current, key = TravelPlanSummary::id) { plan ->
                        TravelPlanCard(plan, loadCover) { onOpen(plan.id) }
                    }
                    if (past.isNotEmpty()) {
                        item {
                            TravelTextButton(
                                onClick = { showPast = !showPast },
                                modifier = Modifier.semantics { stateDescription = pastState },
                            ) {
                                Text(stringResource(Res.string.travel_past))
                            }
                        }
                        if (showPast) {
                            items(past.asReversed(), key = TravelPlanSummary::id) { plan ->
                                TravelPlanCard(plan, loadCover) { onOpen(plan.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelPlanCard(
    plan: TravelPlanSummary,
    loadCover: suspend (TravelPlanSummary) -> String?,
    onClick: () -> Unit,
) {
    val details = plan.details
    val coverUrl by produceState<String?>(null, plan.details, loadCover) {
        value = loadCover(plan)
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Mint10)
            .noRippleClickable(onClick = onClick),
    ) {
        coverUrl?.let {
            NetworkImage(it, Modifier.fillMaxWidth().height(134.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(details.title, style = OguriTheme.typography.cardTitle, color = Neutral100)
                Text(
                    stringResource(Res.string.travel_dates_value, details.dates.start.toString(), details.dates.endInclusive.toString()),
                    style = OguriTheme.typography.labelMedium,
                    color = Neutral70,
                )
            }
            Icon(painterResource(Res.drawable.ic_right_arrow), contentDescription = null, tint = Neutral100)
        }
    }
}
