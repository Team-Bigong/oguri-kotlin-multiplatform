package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.deeplink.travelInviteLink
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange70
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.domain.model.TravelReminderStatus
import com.bigong.oguri.feature.travelplanner.ui.component.TravelDetailsDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelReminderDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_travel_member
import oguri.composeapp.generated.resources.ic_travel_link
import oguri.composeapp.generated.resources.ic_travel_row_arrow
import oguri.composeapp.generated.resources.ic_right_arrow
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
import oguri.composeapp.generated.resources.travel_manage
import oguri.composeapp.generated.resources.travel_manage_title
import oguri.composeapp.generated.resources.travel_member
import oguri.composeapp.generated.resources.travel_members
import oguri.composeapp.generated.resources.travel_my_reminder
import oguri.composeapp.generated.resources.travel_offer_ownership
import oguri.composeapp.generated.resources.travel_owner_role
import oguri.composeapp.generated.resources.travel_ownership_pending
import oguri.composeapp.generated.resources.travel_private_settings
import oguri.composeapp.generated.resources.travel_recent_changes
import oguri.composeapp.generated.resources.travel_reminder_off
import oguri.composeapp.generated.resources.travel_reminder_status_on
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
    LaunchedEffect(viewModel, reminderViewModel, planId) {
        viewModel.members(planId)
        reminderViewModel.load(planId)
    }
    LaunchedEffect(state.closed) { if (state.closed) onClosed() }
    LaunchedEffect(reminderState.failure) {
        reminderState.failure?.let(viewModel::applyAccessFailure)
    }
    TravelPlannerPage(stringResource(Res.string.travel_manage), onBack, state.busy, showTopDivider = false) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
            if (state.detailsPlan == null) {
                state.failure?.let { TravelInlineFailure(it, onRetry = { viewModel.members(planId) }) }
            }
            val members = state.members
            if (members != null) {
                TravelManagementSection(stringResource(Res.string.travel_members))
                Spacer(Modifier.height(10.dp))
                val colors = OguriTheme.colors
                val shape = RoundedCornerShape(8.dp)
                members.members.forEach { member ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(shape).background(colors.surfaceCard)
                            .border(1.dp, colors.borderStrong, shape).padding(horizontal = 14.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(40.dp).background(Mint40, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(Res.drawable.ic_travel_member), null, Modifier.size(22.dp), tint = Neutral0)
                        }
                        Text(
                            member.nickname ?: stringResource(Res.string.travel_member),
                            Modifier.weight(1f), color = Neutral100, style = travelTextStyle(15, 18, FontWeight.Medium),
                        )
                        if (member.owner) {
                            Text(stringResource(Res.string.travel_owner_role), color = colors.textTertiary, style = travelTextStyle(12, 14, FontWeight.Medium))
                        }
                    }
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
                if (members.isOwner) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(Res.string.travel_invite_explanation),
                        Modifier.padding(horizontal = 24.dp),
                        style = travelTextStyle(13, 16),
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.invite == null) {
                            TravelOutlineButton(enabled = !state.busy, onClick = { viewModel.issue(planId) }) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(Res.drawable.ic_travel_link), null, Modifier.size(16.dp))
                                    Text(stringResource(Res.string.travel_create_invite))
                                }
                            }
                        } else {
                            TravelOutlineButton(enabled = !state.busy, onClick = {
                                state.invite?.let {
                                    clipboard.setText(AnnotatedString(travelInviteLink(it)))
                                    copied = true
                                }
                            }) { Text(stringResource(if (copied) Res.string.travel_invite_copied else Res.string.travel_copy_invite)) }
                        }
                        Box(
                            Modifier.heightIn(min = 45.dp)
                                .noRippleClickable(enabled = !state.busy, onClick = { confirmation = { viewModel.revoke(planId) } })
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(Res.string.travel_revoke_invite), style = travelTextStyle(13, 16), color = colors.textTertiary)
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
                TravelManagementSection(stringResource(Res.string.travel_manage_title))
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = colors.borderSubtle)
                TravelManagementRow(stringResource(Res.string.travel_trip_info), !state.busy) { viewModel.loadDetails(planId) }
                HorizontalDivider(color = colors.borderSubtle)
                Spacer(Modifier.height(26.dp))
                TravelManagementSection(stringResource(Res.string.travel_private_settings))
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = colors.borderSubtle)
                val reminderLabel = reminderState.settings?.let {
                    val active = it.reminder?.status == TravelReminderStatus.PENDING
                    stringResource(if (active) Res.string.travel_reminder_status_on else Res.string.travel_reminder_off)
                }
                TravelManagementRow(stringResource(Res.string.travel_my_reminder), !state.busy, trailing = reminderLabel) {
                    showReminder = true
                    reminderViewModel.load(planId)
                }
                HorizontalDivider(color = colors.borderSubtle)
                if (members.isOwner) {
                    Spacer(Modifier.height(40.dp))
                    HorizontalDivider(color = colors.borderSubtle)
                    TravelManagementRow(stringResource(Res.string.travel_delete_trip), !state.busy, destructive = true) {
                        confirmation = { viewModel.delete(planId) }
                    }
                    HorizontalDivider(color = colors.borderSubtle)
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
    val action = confirmation
    if (action != null) {
        com.bigong.oguri.feature.travelplanner.ui.component.TravelFormSheet(
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
private fun TravelManagementSection(label: String) {
    Text(
        label, Modifier.padding(horizontal = 24.dp),
        style = travelTextStyle(12, 14, FontWeight.Medium), color = OguriTheme.colors.textTertiary,
    )
}

@Composable
private fun TravelManagementRow(
    label: String,
    enabled: Boolean,
    trailing: String? = null,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = OguriTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).noRippleClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label, Modifier.weight(1f), style = travelTextStyle(15, 18, FontWeight.Medium),
            color = if (destructive) Orange70 else Neutral100,
        )
        trailing?.let { Text(it, style = OguriTheme.typography.bodySmall, color = colors.textTertiary) }
        if (!destructive) Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(24.dp), tint = colors.textPrimary)
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
