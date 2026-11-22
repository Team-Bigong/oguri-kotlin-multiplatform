package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.dialogSecondaryButtonColor
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelInvitePreview
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.feature.travelplanner.ui.travelCardDates
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_travel_pin
import oguri.composeapp.generated.resources.img_oguri_parasol
import oguri.composeapp.generated.resources.travel_invite_confirm_heading
import oguri.composeapp.generated.resources.travel_invite_description
import oguri.composeapp.generated.resources.travel_invite_join_action
import oguri.composeapp.generated.resources.travel_invite_join_failure_message
import oguri.composeapp.generated.resources.travel_invite_join_failure_title
import oguri.composeapp.generated.resources.travel_invite_later
import oguri.composeapp.generated.resources.travel_invited_by
import oguri.composeapp.generated.resources.travel_member_count
import oguri.composeapp.generated.resources.travel_sign_in
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelInviteConfirmation(
    preview: TravelInvitePreview,
    signedIn: Boolean,
    busy: Boolean,
    failure: TravelPlanFailure?,
    onDismiss: () -> Unit,
    onJoin: () -> Unit,
) {
    val colors = OguriTheme.colors
    val typography = OguriTheme.typography
    val shape = RoundedCornerShape(8.dp)
    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.padding(24.dp).widthIn(max = 440.dp).verticalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 62.dp),
                shape = shape,
                color = colors.surfaceCard,
                contentColor = colors.textPrimary,
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, top = 72.dp, end = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    preview.invitedBy?.let { inviter ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(24.dp).background(colors.mintContainer, CircleShape))
                            Text(stringResource(Res.string.travel_invited_by, inviter), style = typography.bodySmall, color = colors.textTertiary)
                        }
                    }
                    Text(stringResource(Res.string.travel_invite_confirm_heading), style = typography.cardTitle, textAlign = TextAlign.Center)
                    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, colors.borderStrong, shape)) {
                        Text(
                            preview.title,
                            Modifier.fillMaxWidth().background(colors.mint).padding(horizontal = 16.dp, vertical = 10.dp),
                            style = typography.cardSubtitle,
                            color = travelAccentForeground,
                        )
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(travelCardDates(TravelDates(preview.startDate, preview.endDate), true), style = typography.cardSubtitle)
                            val city = preview.city
                            val members = preview.memberCount?.let { stringResource(Res.string.travel_member_count, it) }
                            if (city != null || members != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(painterResource(Res.drawable.ic_travel_pin), null, Modifier.size(14.dp), tint = colors.textTertiary)
                                    Text(listOfNotNull(city, members).joinToString(" · "), style = typography.labelSmall, color = colors.textTertiary)
                                }
                            }
                        }
                    }
                    Text(stringResource(Res.string.travel_invite_description), style = typography.caption, color = colors.textTertiary, textAlign = TextAlign.Center)
                    if (failure == TravelPlanFailure.CONNECTION) {
                        TravelIssueCard(
                            title = stringResource(Res.string.travel_invite_join_failure_title),
                            description = stringResource(Res.string.travel_invite_join_failure_message),
                        )
                    } else if (failure != null) {
                        TravelInlineFailure(failure)
                    }
                    if (busy) TravelLoading(Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        TravelTextButton(
                            onClick = onDismiss,
                            enabled = !busy,
                            modifier = Modifier.weight(1f).background(dialogSecondaryButtonColor, shape),
                        ) {
                            Text(stringResource(Res.string.travel_invite_later), style = typography.bodyMedium)
                        }
                        TravelPrimaryButton(onClick = onJoin, enabled = !busy, modifier = Modifier.weight(1f), textStyle = typography.bodyMedium) {
                            Text(stringResource(if (signedIn) Res.string.travel_invite_join_action else Res.string.travel_sign_in))
                        }
                    }
                }
            }
            Image(
                painter = painterResource(Res.drawable.img_oguri_parasol),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopCenter).size(116.dp),
            )
        }
    }
}
