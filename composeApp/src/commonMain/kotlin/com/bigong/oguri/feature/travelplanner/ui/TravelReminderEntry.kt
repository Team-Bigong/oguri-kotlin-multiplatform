package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.notification.consumeTravelReminderOpening
import com.bigong.oguri.core.notification.travelReminderOpening
import com.bigong.oguri.core.notification.travelReminderTokenVersion
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.travelAccentForeground
import com.bigong.oguri.core.ui.component.dialogSecondaryButtonColor
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_retry
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
        val hasConfirmAction = needsSignIn || failure == TravelPlanFailure.CONNECTION
        com.bigong.oguri.feature.travelplanner.ui.component.TravelFormSheet(
            shape = RoundedCornerShape(8.dp),
            containerColor = OguriTheme.colors.surfaceCard,
            onDismissRequest = dismiss,
            title = { Text(stringResource(Res.string.travel_my_reminder)) },
            text = { TravelInlineFailure(if (needsSignIn) TravelPlanFailure.SIGN_IN_REQUIRED else requireNotNull(failure)) },
            confirmButton = {
                if (needsSignIn) {
                    ReminderActionButton(
                        text = stringResource(Res.string.travel_sign_in),
                        onClick = onSignIn,
                        modifier = Modifier.weight(1f),
                        primary = true,
                    )
                } else if (failure == TravelPlanFailure.CONNECTION) {
                    ReminderActionButton(
                        text = stringResource(Res.string.travel_retry),
                        onClick = { reminderId?.let(viewModel::open) },
                        modifier = Modifier.weight(1f),
                        primary = true,
                        enabled = !state.busy,
                    )
                }
            },
            dismissButton = {
                ReminderActionButton(
                    text = stringResource(Res.string.travel_search_close),
                    onClick = dismiss,
                    modifier = if (hasConfirmAction) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                    primary = false,
                )
            },
        )
    }
}

@Composable
private fun ReminderActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    primary: Boolean,
    enabled: Boolean = true,
) {
    val background = when {
        !enabled -> OguriTheme.colors.borderStrong
        primary -> OguriTheme.colors.mint
        else -> dialogSecondaryButtonColor
    }
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(8.dp))
            .semantics { role = Role.Button }
            .noRippleClickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = OguriTheme.typography.bodyMedium,
            color = when {
                !enabled -> OguriTheme.colors.textSecondary
                primary -> travelAccentForeground
                else -> OguriTheme.colors.textPrimary
            },
        )
    }
}
