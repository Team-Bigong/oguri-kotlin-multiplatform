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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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
import com.bigong.oguri.feature.travelplanner.ui.component.TravelBottomAction
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInviteSkeleton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIssueCard
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelReminderDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import oguri.composeapp.generated.resources.*
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
import oguri.composeapp.generated.resources.travel_invite_copy_unavailable
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
    cityViewModel: TravelPlannerViewModel,
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
    val cityState by cityViewModel.state.collectAsStateWithLifecycle()
    val language = androidx.compose.ui.text.intl.Locale.current.toLanguageTag()
    var showMembers by remember(planId) { mutableStateOf(false) }
    var showReminder by remember(planId) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val inviteToken = state.members?.inviteToken
    var copied by remember(inviteToken) { mutableStateOf(false) }
    var confirmationKind by remember { mutableStateOf("delete") }
    var confirmation by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(viewModel, reminderViewModel, planId) {
        viewModel.members(planId)
        reminderViewModel.load(planId)
    }
    LaunchedEffect(state.closed) { if (state.closed) onClosed() }
    LaunchedEffect(reminderState.failure) {
        reminderState.failure?.let(viewModel::applyAccessFailure)
    }
    TravelPlannerPage(stringResource(if (showMembers) Res.string.travel_participating_members else Res.string.travel_manage), { if (showMembers) showMembers = false else onBack() }, state.busy, showTopDivider = false) {
        if (state.members == null && state.failure == TravelPlanFailure.CONNECTION) {
            TravelPlansEmpty(
                illustration = Res.drawable.img_oguri_empty,
                message = stringResource(Res.string.travel_manage_load_failure_message),
                actionLabel = stringResource(Res.string.travel_list_retry),
                onAction = { viewModel.members(planId) },
                title = stringResource(Res.string.travel_list_failure_title),
                modifier = Modifier.weight(1f),
                illustrationSize = 180.dp,
                outlinedAction = true,
            )
            return@TravelPlannerPage
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
            if (state.detailsPlan == null) {
                state.failure?.let { TravelInlineFailure(it, onRetry = { viewModel.members(planId) }) }
            }
            val members = state.members
            if (members != null) {
                if (!showMembers && members.ownershipTarget == members.currentMemberId && !members.isOwner) {
                    val shape = RoundedCornerShape(8.dp)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).background(com.bigong.oguri.core.designsystem.Mint5, shape)
                        .border(1.dp, Mint40, shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(Res.string.travel_transfer_offer_title), style = OguriTheme.typography.cardSubtitle)
                        Text(stringResource(Res.string.travel_transfer_offer_description, members.members.firstOrNull { it.owner }?.nickname ?: stringResource(Res.string.travel_member)), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        TravelOutlineButton(modifier = Modifier.fillMaxWidth(), enabled = !state.busy, onClick = {
                            confirmationKind = "accept"; confirmation = { viewModel.acceptOwnership(planId) }
                        }) { Text(stringResource(Res.string.travel_accept_ownership)) }
                    }
                    Spacer(Modifier.height(18.dp))
                }
                TravelManagementSection(
                    if (members.members.size > 1) stringResource(Res.string.travel_member_count, members.members.size) else stringResource(Res.string.travel_members),
                    Res.drawable.ic_travel_members,
                    onClick = { showMembers = true },
                )
                Spacer(Modifier.height(10.dp))
                val colors = OguriTheme.colors
                val shape = RoundedCornerShape(8.dp)
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderStrong, shape)) {
                members.members.forEachIndexed { index, member ->
                    if (index > 0) HorizontalDivider(color = colors.borderSubtle)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(40.dp).background(if (member.owner) Mint40 else com.bigong.oguri.core.designsystem.Orange20, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(Res.drawable.ic_travel_member), null, Modifier.size(22.dp), tint = Neutral0)
                        }
                        Text(
                            member.nickname ?: stringResource(Res.string.travel_member),
                            Modifier.weight(1f), color = Neutral100, style = travelTextStyle(15, 18, FontWeight.Medium),
                        )
                        if (member.owner || member.id == members.currentMemberId) {
                            Text(listOfNotNull(stringResource(Res.string.travel_owner_role).takeIf { member.owner }, stringResource(Res.string.travel_me).takeIf { member.id == members.currentMemberId }).joinToString(" · "), color = colors.textTertiary, style = travelTextStyle(12, 14, FontWeight.Medium))
                        }
                        if (members.isOwner && !member.owner) {
                            var menuOpen by remember(member.id) { mutableStateOf(false) }
                            Box {
                                com.bigong.oguri.feature.travelplanner.ui.component.TravelIconButton(onClick = { menuOpen = true }, enabled = !state.busy) {
                                    Icon(painterResource(Res.drawable.ic_travel_more), stringResource(Res.string.travel_manage), Modifier.size(24.dp).rotate(90f))
                                }
                                if (menuOpen) androidx.compose.ui.window.Popup(alignment = Alignment.TopEnd, offset = IntOffset(0, with(LocalDensity.current) { 40.dp.roundToPx() }), onDismissRequest = { menuOpen = false }, properties = androidx.compose.ui.window.PopupProperties(focusable = true)) {
                                    androidx.compose.material3.Surface(modifier = Modifier.width(140.dp).border(1.dp, colors.borderStrong, shape), color = colors.surfaceCard, shape = shape, shadowElevation = 4.dp) {
                                        Column {
                                            TravelTextButton(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), enabled = !state.busy && members.ownershipTarget != member.id, onClick = { menuOpen = false; confirmationKind = "transfer"; confirmation = { viewModel.offerOwnership(planId, member.id) } }) {
                                                Text(stringResource(if (members.ownershipTarget == member.id) Res.string.travel_ownership_pending else Res.string.travel_offer_ownership), style = OguriTheme.typography.bodyMedium)
                                            }
                                            HorizontalDivider(color = colors.borderSubtle)
                                            TravelTextButton(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), enabled = !state.busy, onClick = { menuOpen = false; confirmationKind = "remove"; confirmation = { viewModel.remove(planId, member.id) } }) {
                                                Text(stringResource(Res.string.travel_remove_member), style = OguriTheme.typography.bodyMedium, color = com.bigong.oguri.core.designsystem.Orange50)
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                }
                if (!showMembers) {
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
                        if (!members.hasActiveInvite) {
                            TravelOutlineButton(enabled = !state.busy, onClick = { viewModel.issue(planId) }) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(Res.drawable.ic_travel_link), null, Modifier.size(16.dp))
                                    Text(stringResource(Res.string.travel_create_invite))
                                }
                            }
                        } else if (inviteToken != null) {
                            TravelOutlineButton(enabled = !state.busy, onClick = {
                                clipboard.setText(AnnotatedString(travelInviteLink(inviteToken)))
                                copied = true
                            }) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(if (copied) Res.drawable.ic_travel_check else Res.drawable.ic_travel_link), null, Modifier.size(16.dp))
                                    Text(stringResource(if (copied) Res.string.travel_invite_copied else Res.string.travel_copy_invite))
                                }
                            }
                        }
                        if (members.hasActiveInvite) Box(
                            Modifier.heightIn(min = 45.dp)
                                .noRippleClickable(enabled = !state.busy, onClick = { confirmationKind = "revoke"; confirmation = { viewModel.revoke(planId) } })
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(Res.string.travel_revoke_invite), style = travelTextStyle(13, 16), color = colors.textTertiary)
                        }
                    }
                    if (members.hasActiveInvite && inviteToken == null) {
                        Text(
                            stringResource(Res.string.travel_invite_copy_unavailable),
                            Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp),
                            style = OguriTheme.typography.caption,
                            color = colors.textTertiary,
                        )
                    }
                    members.inviteExpiresAt?.let { expires ->
                        Text(stringResource(Res.string.travel_invite_expiry_hint, travelWeekDate(expires.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date)), Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp), style = OguriTheme.typography.caption, color = colors.textTertiary)
                    }
                }
                Spacer(Modifier.height(30.dp))
                TravelManagementSection(stringResource(Res.string.travel_manage_title), Res.drawable.ic_travel_map)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = colors.borderSubtle)
                TravelManagementRow(stringResource(Res.string.travel_trip_info), !state.busy) { viewModel.loadDetails(planId) }
                HorizontalDivider(color = colors.borderSubtle)
                Spacer(Modifier.height(26.dp))
                TravelManagementSection(stringResource(Res.string.travel_private_settings), Res.drawable.ic_travel_bell)
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
                        confirmationKind = "delete"; confirmation = { viewModel.delete(planId) }
                    }
                    HorizontalDivider(color = colors.borderSubtle)
                } else {
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = colors.borderSubtle)
                    TravelManagementRow(stringResource(Res.string.travel_leave_trip), !state.busy, destructive = true) {
                        confirmationKind = "leave"; confirmation = { viewModel.leave(planId) }
                    }
                    HorizontalDivider(color = colors.borderSubtle)
                    Text(stringResource(Res.string.travel_leave_explanation), Modifier.padding(horizontal = 24.dp, vertical = 10.dp), style = OguriTheme.typography.caption, color = colors.textTertiary)
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
            onRefreshPermission = reminderViewModel::refreshPermission,
        )
    }
    state.detailsPlan?.let { plan ->
        TravelDetailsDialog(
            plan,
            state.busy,
            state.failure,
            { cityViewModel.closeCitySearch(); viewModel.dismissDetails() },
            citySearch = cityState.citySearch,
            onOpenCitySearch = cityViewModel::openCitySearch,
            onCloseCitySearch = cityViewModel::closeCitySearch,
            onSearchCities = { cityViewModel.searchCities(it, androidx.compose.ui.text.intl.Locale.current.toLanguageTag()) },
            onChooseCity = cityViewModel::chooseCity,
            onResolveCityTimeZone = { city -> cityViewModel.resolveCityTimeZone(city, language) },
            onReload = { viewModel.loadDetails(planId) },
            onApply = { details, affected -> viewModel.updateDetails(plan, details, affected) },
            canEdit = state.members?.isOwner == true,
            startEditing = false,
            onOpenSource = { source ->
                viewModel.dismissDetails()
                onOpenSource(source)
            },
        )
    }
    val action = confirmation
    if (action != null) {
        val labels = when (confirmationKind) {
            "leave" -> Triple(Res.string.travel_confirm_leave_title, Res.string.travel_confirm_leave_message, Res.string.travel_action_leave)
            "remove" -> Triple(Res.string.travel_confirm_remove_title, Res.string.travel_confirm_remove_message, Res.string.travel_action_remove)
            "revoke" -> Triple(Res.string.travel_confirm_revoke_title, Res.string.travel_confirm_revoke_message, Res.string.travel_action_revoke)
            "transfer" -> Triple(Res.string.travel_confirm_transfer_title, Res.string.travel_confirm_transfer_message, Res.string.travel_action_request)
            "accept" -> Triple(Res.string.travel_confirm_accept_title, Res.string.travel_confirm_accept_message, Res.string.travel_action_accept)
            else -> Triple(Res.string.travel_confirm_delete_title, Res.string.travel_confirm_delete_message, Res.string.travel_delete)
        }
        com.bigong.oguri.core.ui.component.ConfirmAlertDialog(
            iconResource = if (confirmationKind in listOf("transfer", "accept")) Res.drawable.ic_info else Res.drawable.ic_alert,
            iconTint = if (confirmationKind in listOf("transfer", "accept")) com.bigong.oguri.core.designsystem.Mint70 else com.bigong.oguri.core.designsystem.Orange50,
            messageColor = if (confirmationKind in listOf("transfer", "accept")) OguriTheme.colors.textTertiary else com.bigong.oguri.core.designsystem.Orange50,
            titleText = stringResource(labels.first), messageText = stringResource(labels.second),
            confirmButtonText = stringResource(labels.third), cancelButtonText = stringResource(Res.string.travel_cancel),
            onDismissRequest = { confirmation = null }, onCancelClick = { confirmation = null },
            onConfirmClick = { confirmation = null; action() },
        )
    }
}

@Composable
private fun TravelManagementSection(label: String, icon: org.jetbrains.compose.resources.DrawableResource, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).noRippleClickable(enabled = onClick != null, onClick = { onClick?.invoke() }).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(painterResource(icon), null, Modifier.size(24.dp), tint = Mint40)
        Text(label, style = OguriTheme.typography.cardSubtitle, color = OguriTheme.colors.textPrimary)
    }
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
            color = if (destructive) com.bigong.oguri.core.designsystem.Orange50 else Neutral100,
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
    onOpenPlans: () -> Unit = onBack,
) {
    val loadedState = viewModel.state.collectAsStateWithLifecycle().value
    val state = if (viewModel.matchesInvite(token)) loadedState else TravelCollaborationState(busy = token != null)
    LaunchedEffect(viewModel, token) { if (token != null) viewModel.preview(token) }
    LaunchedEffect(viewModel, token, state.joinedPlanId) {
        if (viewModel.matchesInvite(token)) state.joinedPlanId?.let(onJoined)
    }
    TravelPlannerPage(
        title = stringResource(Res.string.travel_invitation),
        onBack = onBack,
        isSaving = state.busy && state.preview != null,
        showTopDivider = false,
    ) {
        val preview = state.preview
        val terminal = state.failure in listOf(com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_EXPIRED, com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_REVOKED, com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_TRIP_DELETED)
        if (token == null || (preview == null && state.failure == TravelPlanFailure.INVALID)) {
            TravelPlansEmpty(
                illustration = Res.drawable.img_oguri_empty,
                message = stringResource(Res.string.travel_invite_missing),
                actionLabel = stringResource(Res.string.travel_go_to_plans),
                onAction = onOpenPlans,
                title = stringResource(Res.string.travel_invite_missing_title),
                modifier = Modifier.weight(1f),
                illustrationSize = 180.dp,
                outlinedAction = true,
            )
            return@TravelPlannerPage
        }
        if (preview == null && state.busy) {
            TravelInviteSkeleton(Modifier.weight(1f))
            TravelBottomAction(enabled = false, onClick = {}) {
                Text(stringResource(Res.string.travel_join_trip))
            }
            return@TravelPlannerPage
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = if (terminal) 24.dp else 20.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = if (preview != null && !terminal) Arrangement.Top else Arrangement.Center) {
            if (terminal) {
                androidx.compose.foundation.Image(painterResource(Res.drawable.img_oguri_empty), null, Modifier.size(180.dp))
                val deleted = state.failure == com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_TRIP_DELETED
                val title = when (state.failure) {
                    com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_REVOKED -> Res.string.travel_invite_revoked_title
                    com.bigong.oguri.domain.model.TravelPlanFailure.INVITE_TRIP_DELETED -> Res.string.travel_invite_deleted_title
                    else -> Res.string.travel_invite_expired_title
                }
                Text(stringResource(title), style = OguriTheme.typography.cardTitle, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 24.dp))
                Text(stringResource(if (deleted) Res.string.travel_invite_deleted_message else Res.string.travel_invite_expired_message), style = OguriTheme.typography.bodyMedium, color = OguriTheme.colors.textTertiary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 16.dp, bottom = 28.dp))
                TravelOutlineButton(onClick = onOpenPlans, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.travel_go_to_plans)) }
            } else if (preview != null) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.Image(painterResource(Res.drawable.img_oguri_parasol), null, Modifier.size(160.dp))
                Spacer(Modifier.height(24.dp))
                preview.invitedBy?.let { Text(stringResource(Res.string.travel_invited_by, it), style = OguriTheme.typography.bodyMedium, color = OguriTheme.colors.textTertiary) }
                Text(stringResource(Res.string.travel_invite_heading), style = OguriTheme.typography.sectionTitle, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp).clip(RoundedCornerShape(8.dp)).background(OguriTheme.colors.surfaceCard).border(1.dp, OguriTheme.colors.borderStrong, RoundedCornerShape(8.dp))) {
                    Text(preview.title, Modifier.fillMaxWidth().background(com.bigong.oguri.core.designsystem.Mint70).padding(horizontal = 18.dp, vertical = 12.dp), style = OguriTheme.typography.cardTitle, color = OguriTheme.colors.textOnMint)
                    Text(travelCardDates(com.bigong.oguri.domain.model.TravelDates(preview.startDate, preview.endDate), true), Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp), style = OguriTheme.typography.cardSubtitle)
                    preview.city?.let { city ->
                        Row(Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(painterResource(Res.drawable.ic_travel_pin), null, Modifier.size(14.dp), tint = OguriTheme.colors.textTertiary)
                            Text(city + " · " + stringResource(Res.string.travel_member_count, preview.memberCount ?: 1), style = OguriTheme.typography.labelSmall, color = OguriTheme.colors.textTertiary)
                        }
                    }
                }
                Text(stringResource(Res.string.travel_invite_description), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                state.failure?.let {
                    if (it == TravelPlanFailure.CONNECTION) {
                        TravelIssueCard(
                            title = stringResource(Res.string.travel_invite_join_failure_title),
                            description = stringResource(Res.string.travel_invite_join_failure_message),
                            onRetry = { if (signedIn) viewModel.join(token) else onSignIn() },
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    } else {
                        TravelInlineFailure(it)
                    }
                }
            } else {
                state.failure?.let { TravelInlineFailure(it, onRetry = { viewModel.preview(token) }) }
            }
        }
        if (!terminal && preview != null) TravelBottomAction(enabled = !state.busy, onClick = { if (signedIn) viewModel.join(token) else onSignIn() }) {
            Text(stringResource(if (signedIn) Res.string.travel_join_trip else Res.string.travel_sign_in))
        }
    }
}
