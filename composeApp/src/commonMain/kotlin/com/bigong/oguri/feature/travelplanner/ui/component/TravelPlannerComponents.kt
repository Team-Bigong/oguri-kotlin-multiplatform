package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.domain.model.TravelPlanFailure
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.common_network_error_message
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_back
import oguri.composeapp.generated.resources.travel_conflict
import oguri.composeapp.generated.resources.travel_invalid_request
import oguri.composeapp.generated.resources.travel_invite_expired
import oguri.composeapp.generated.resources.travel_invite_revoked
import oguri.composeapp.generated.resources.travel_invite_trip_deleted
import oguri.composeapp.generated.resources.travel_rate_limit
import oguri.composeapp.generated.resources.travel_sign_in_required
import oguri.composeapp.generated.resources.travel_unavailable
import oguri.composeapp.generated.resources.travel_undo_unavailable
import oguri.composeapp.generated.resources.travel_visit_limit
import oguri.composeapp.generated.resources.travel_visit_removed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelPlannerPage(
    title: String,
    onBack: () -> Unit,
    isSaving: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    PlatformBackHandler(enabled = isSaving) {}
    PlatformBackGestureContainer(enabled = !isSaving, onBack = onBack) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.safeDrawingPadding().imePadding()) {
                TopAppBar(
                    title = { Text(title) },
                    actions = actions,
                    navigationIcon = {
                        IconButton(enabled = !isSaving, onClick = onBack) {
                            Icon(painterResource(Res.drawable.btn_back), stringResource(Res.string.travel_back))
                        }
                    },
                )
                if (isSaving) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                content()
            }
        }
    }
}

@Composable
internal fun TravelLoadFailure(
    failure: TravelPlanFailure,
    onRetry: () -> Unit,
) {
    if (failure == TravelPlanFailure.CONNECTION) {
        NetworkErrorRetryContent(onRetryClick = onRetry)
    } else {
        TravelInlineFailure(failure)
    }
}

@Composable
internal fun TravelInlineFailure(
    failure: TravelPlanFailure,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = stringResource(Res.string.common_retry_button),
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text =
                stringResource(
                    when (failure) {
                        TravelPlanFailure.SIGN_IN_REQUIRED -> Res.string.travel_sign_in_required
                        TravelPlanFailure.UNAVAILABLE -> Res.string.travel_unavailable
                        TravelPlanFailure.CONFLICT -> Res.string.travel_conflict
                        TravelPlanFailure.VISIT_REMOVED -> Res.string.travel_visit_removed
                        TravelPlanFailure.UNDO_UNAVAILABLE -> Res.string.travel_undo_unavailable
                        TravelPlanFailure.VISIT_LIMIT -> Res.string.travel_visit_limit
                        TravelPlanFailure.RATE_LIMIT -> Res.string.travel_rate_limit
                        TravelPlanFailure.INVALID -> Res.string.travel_invalid_request
                        TravelPlanFailure.INVITE_EXPIRED -> Res.string.travel_invite_expired
                        TravelPlanFailure.INVITE_REVOKED -> Res.string.travel_invite_revoked
                        TravelPlanFailure.INVITE_TRIP_DELETED -> Res.string.travel_invite_trip_deleted
                        TravelPlanFailure.CONNECTION -> Res.string.common_network_error_message
                    },
                ),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (onRetry != null) TextButton(onClick = onRetry) { Text(retryLabel) }
    }
}
