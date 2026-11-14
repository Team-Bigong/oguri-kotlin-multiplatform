package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.deeplink.travelInviteLink
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.feature.travelplanner.ui.component.TravelDetailsDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIconButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelRecentChangesDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelReminderDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_pen
import oguri.composeapp.generated.resources.ic_right_arrow
import oguri.composeapp.generated.resources.travel_edit_trip
import oguri.composeapp.generated.resources.travel_accept_ownership
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_confirm_member_action
import oguri.composeapp.generated.resources.travel_copy_invite
import oguri.composeapp.generated.resources.travel_create_invite
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_delete_trip
import oguri.composeapp.generated.resources.travel_invitation
import oguri.composeapp.generated.resources.travel_invite_copied
import oguri.composeapp.generated.resources.travel_invite_explanation
import oguri.composeapp.generated.resources.travel_invite_missing
import oguri.composeapp.generated.resources.travel_invited_by
import oguri.composeapp.generated.resources.travel_join_trip
import oguri.composeapp.generated.resources.travel_leave_explanation
import oguri.composeapp.generated.resources.travel_leave_trip
import oguri.composeapp.generated.resources.travel_manage_title
import oguri.composeapp.generated.resources.travel_member
import oguri.composeapp.generated.resources.travel_members
import oguri.composeapp.generated.resources.travel_my_reminder
import oguri.composeapp.generated.resources.travel_offer_ownership
import oguri.composeapp.generated.resources.travel_owner_name
import oguri.composeapp.generated.resources.travel_ownership_pending
import oguri.composeapp.generated.resources.travel_recent_changes
import oguri.composeapp.generated.resources.travel_remove_member
import oguri.composeapp.generated.resources.travel_revoke_invite
import oguri.composeapp.generated.resources.travel_sign_in
import oguri.composeapp.generated.resources.travel_trip_info
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelMembersRoute(
    planId: String,
    viewModel: TravelCollaborationViewModel,
    reminderViewModel: TravelReminderViewModel,
    onBack: () -> Unit,
    onClosed: () -> Unit,
    onSelectDay: (Int) -> Unit,
    dates: TravelDates,
    onOpenSource: (TravelPlanSource) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reminderState by reminderViewModel.state.collectAsStateWithLifecycle()
    var showReminder by remember(planId) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    var copied by remember(state.invite) { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(viewModel, planId) { viewModel.members(planId) }
    LaunchedEffect(state.closed) { if (state.closed) onClosed() }
    LaunchedEffect(reminderState.failure) {
        reminderState.failure?.let(viewModel::applyAccessFailure)
    }
    TravelPlannerPage(stringResource(Res.string.travel_manage_title), onBack, state.busy) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.detailsPlan == null) {
                state.failure?.let { TravelInlineFailure(it, onRetry = { viewModel.members(planId) }) }
            }
            val members = state.members
            if (members != null) {
                Text(stringResource(Res.string.travel_members))
                members.members.forEach { member ->
                    Text(
                        if (member.owner) {
                            stringResource(
                                Res.string.travel_owner_name,
                                member.nickname ?: stringResource(Res.string.travel_member),
                            )
                        } else {
                            member.nickname ?: stringResource(Res.string.travel_member)
                        },
                    )
                    if (members.isOwner && !member.owner) {
                        TravelTextButton(enabled = !state.busy, onClick = {
                            confirmation = { viewModel.remove(planId, member.id) }
                        }) { Text(stringResource(Res.string.travel_remove_member)) }
                        if (members.ownershipTarget == member.id) {
                            Text(stringResource(Res.string.travel_ownership_pending))
                        } else {
                            TravelTextButton(enabled = !state.busy, onClick = {
                                confirmation = { viewModel.offerOwnership(planId, member.id) }
                            }) { Text(stringResource(Res.string.travel_offer_ownership)) }
                        }
                    }
                }
                TravelTextButton(enabled = !state.busy, onClick = { viewModel.recentChanges(planId) }) {
                    Text(stringResource(Res.string.travel_recent_changes))
                }
                HorizontalDivider()
                TravelTextButton(enabled = !state.busy, onClick = {
                    showReminder = true
                    reminderViewModel.load(planId)
                }) { Text(stringResource(Res.string.travel_my_reminder)) }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.travel_trip_info), Modifier.weight(1f), style = OguriTheme.typography.cardSubtitle)
                    TravelIconButton(enabled = !state.busy, onClick = { viewModel.loadDetails(planId) }) {
                        Icon(painterResource(if (members.isOwner) Res.drawable.ic_pen else Res.drawable.ic_right_arrow), stringResource(if (members.isOwner) Res.string.travel_edit_trip else Res.string.travel_trip_info), tint = Neutral100)
                    }
                }
                if (members.isOwner) {
                    Text(stringResource(Res.string.travel_invite_explanation))
                    if (state.invite == null) {
                        TravelPrimaryButton(enabled = !state.busy, onClick = { viewModel.issue(planId) }) {
                            Text(stringResource(Res.string.travel_create_invite))
                        }
                    } else {
                        TravelPrimaryButton(enabled = !state.busy, onClick = {
                            state.invite?.let {
                                clipboard.setText(AnnotatedString(travelInviteLink(it)))
                                copied = true
                            }
                        }) { Text(stringResource(if (copied) Res.string.travel_invite_copied else Res.string.travel_copy_invite)) }
                    }
                    TravelTextButton(enabled = !state.busy, onClick = { confirmation = { viewModel.revoke(planId) } }) {
                        Text(stringResource(Res.string.travel_revoke_invite))
                    }
                    TravelTextButton(enabled = !state.busy, onClick = { confirmation = { viewModel.delete(planId) } }) {
                        Text(stringResource(Res.string.travel_delete_trip))
                    }
                } else {
                    if (members.ownershipTarget == members.currentMemberId) {
                        TravelPrimaryButton(enabled = !state.busy, onClick = {
                            confirmation = { viewModel.acceptOwnership(planId) }
                        }) { Text(stringResource(Res.string.travel_accept_ownership)) }
                    }
                    Text(stringResource(Res.string.travel_leave_explanation))
                    TravelTextButton(enabled = !state.busy, onClick = { confirmation = { viewModel.leave(planId) } }) {
                        Text(stringResource(Res.string.travel_leave_trip))
                    }
                }
            }
        }
    }
    if (showReminder) {
        TravelReminderDialog(
            state = reminderState,
            onSchedule = { reminderViewModel.schedule(planId, it) },
            onCancel = { reminderViewModel.cancel(planId) },
            onReload = { reminderViewModel.load(planId) },
            onDismiss = { showReminder = false },
        )
    }
    state.detailsPlan?.let { plan ->
        TravelDetailsDialog(
            plan,
            state.busy,
            state.failure,
            viewModel::dismissDetails,
            onReload = { viewModel.loadDetails(planId) },
            onApply = { details, affected -> viewModel.updateDetails(plan, details, affected) },
            canEdit = state.members?.isOwner == true,
            startEditing = state.members?.isOwner == true,
            onOpenSource = { source ->
                viewModel.dismissDetails()
                onOpenSource(source)
            },
        )
    }
    state.recentChanges?.let { changes ->
        TravelRecentChangesDialog(
            changes,
            dates,
            onDismiss = { viewModel.dismissChanges(planId, changes.throughRevision) },
            onSelectDay = { day ->
                viewModel.dismissChanges(planId, changes.throughRevision)
                onSelectDay(day)
            },
        )
    }
    val action = confirmation
    if (action != null) {
        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            containerColor = Neutral0,
            onDismissRequest = { confirmation = null },
            title = { Text(stringResource(Res.string.travel_confirm_member_action)) },
            confirmButton = {
                TravelTextButton(onClick = {
                    confirmation = null
                    action()
                }) { Text(stringResource(Res.string.travel_apply)) }
            },
            dismissButton = {
                TravelTextButton(onClick = { confirmation = null }) { Text(stringResource(Res.string.travel_cancel)) }
            },
        )
    }
}

@Composable
internal fun TravelInviteRoute(
    token: String?,
    signedIn: Boolean,
    viewModel: TravelCollaborationViewModel,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    onJoined: (String) -> Unit,
) {
    val loadedState by viewModel.state.collectAsStateWithLifecycle()
    val state = if (viewModel.matchesInvite(token)) loadedState else TravelCollaborationState(busy = token != null)
    LaunchedEffect(viewModel, token) { if (token != null) viewModel.preview(token) }
    LaunchedEffect(viewModel, token, state.joinedPlanId) {
        if (viewModel.matchesInvite(token)) state.joinedPlanId?.let(onJoined)
    }
    TravelPlannerPage(
        title = stringResource(Res.string.travel_invitation),
        onBack = onBack,
        isSaving = state.busy && state.preview != null,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val preview = state.preview
            if (token == null) Text(stringResource(Res.string.travel_invite_missing))
            state.failure?.let {
                TravelInlineFailure(it, onRetry = if (token == null) null else ({ viewModel.preview(token) }))
            }
            if (preview != null && token != null) {
                Text(preview.title)
                Text(stringResource(Res.string.travel_dates_value, preview.startDate.toString(), preview.endDate.toString()))
                preview.invitedBy?.let { Text(stringResource(Res.string.travel_invited_by, it)) }
                if (state.failure == null || state.failure == com.bigong.oguri.domain.model.TravelPlanFailure.SIGN_IN_REQUIRED) {
                    TravelPrimaryButton(enabled = !state.busy, onClick = { if (signedIn) viewModel.join(token) else onSignIn() }) {
                        Text(stringResource(if (signedIn) Res.string.travel_join_trip else Res.string.travel_sign_in))
                    }
                }
            } else if (state.busy) {
                TravelLoading(Modifier.fillMaxWidth())
            }
        }
    }
}
