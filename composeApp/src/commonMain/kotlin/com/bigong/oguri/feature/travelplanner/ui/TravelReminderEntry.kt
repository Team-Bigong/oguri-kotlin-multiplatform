package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.notification.consumeTravelReminderOpening
import com.bigong.oguri.core.notification.travelReminderOpening
import com.bigong.oguri.core.notification.travelReminderTokenVersion
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_my_reminder
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_sign_in
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelReminderEntry(
    viewModel: TravelReminderViewModel,
    ready: Boolean,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val tokenVersion by travelReminderTokenVersion.collectAsStateWithLifecycle()
    val reminderId by travelReminderOpening.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, lifecycle, tokenVersion) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.syncDevice() }
    }
    LaunchedEffect(viewModel, ready, signedIn, reminderId) {
        if (ready && signedIn) reminderId?.let(viewModel::open)
    }
    LaunchedEffect(viewModel, state.openedPlanId) {
        state.openedPlanId?.let { planId ->
            if (reminderId == state.openedReminderId) {
                state.openedReminderId?.let(::consumeTravelReminderOpening)
                viewModel.clearOpening()
                onOpen(planId)
            }
        }
    }
    val failure = state.failure
    val needsSignIn = !signedIn || failure == TravelPlanFailure.SIGN_IN_REQUIRED
    if (ready && reminderId != null && (needsSignIn || failure != null)) {
        val dismiss = {
            reminderId?.let(::consumeTravelReminderOpening)
            viewModel.clearOpening()
        }
        com.bigong.oguri.feature.travelplanner.ui.component.TravelFormSheet(
            shape = RoundedCornerShape(8.dp),
            containerColor = Neutral0,
            onDismissRequest = dismiss,
            title = { Text(stringResource(Res.string.travel_my_reminder)) },
            text = { TravelInlineFailure(if (needsSignIn) TravelPlanFailure.SIGN_IN_REQUIRED else requireNotNull(failure)) },
            confirmButton = {
                if (needsSignIn) {
                    TravelTextButton(onClick = onSignIn) { Text(stringResource(Res.string.travel_sign_in)) }
                } else if (failure == TravelPlanFailure.CONNECTION) {
                    TravelTextButton(enabled = !state.busy, onClick = { reminderId?.let(viewModel::open) }) {
                        Text(stringResource(Res.string.common_retry_button))
                    }
                }
            },
            dismissButton = { TravelTextButton(onClick = dismiss) { Text(stringResource(Res.string.travel_search_close)) } },
        )
    }
}
