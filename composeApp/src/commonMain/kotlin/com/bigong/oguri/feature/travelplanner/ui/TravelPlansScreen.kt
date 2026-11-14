package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Mint90
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.floatingNavigationBarsPadding
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoadFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelPlansScreen(
    state: TravelPlannerUiState,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onBack: (() -> Unit)?,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    onFindNextHoliday: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = OguriTheme.colors
    TravelPlannerPage(
        title = if (onBack != null) stringResource(Res.string.travel_list) else "",
        onBack = onBack,
        modifier = Modifier.floatingNavigationBarsPadding(hasFloatingBottomNavigation = onBack == null),
    ) {
        val failure = state.failure
        val plans = state.plans
        when {
            !signedIn -> TravelPlansEmpty(
                illustration = Res.drawable.img_oguri_hello,
                message = stringResource(Res.string.travel_guest_plans),
                actionLabel = stringResource(Res.string.travel_sign_in),
                onAction = onSignIn,
            )
            failure != null -> TravelLoadFailure(failure, onRetry)
            plans == null -> Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(Modifier.fillMaxWidth().height(134.dp))
                TravelLoading(Modifier.fillMaxWidth(0.5f))
            }
            else -> {
                var showPast by rememberSaveable { mutableStateOf(false) }
                val now = Clock.System.now()
                val (past, current) = plans.partition { plan -> plan.hasEnded { now.toLocalDateTime(it).date } }
                val pastState = stringResource(if (showPast) Res.string.travel_past_expanded else Res.string.travel_past_collapsed)
                LazyColumn {
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (onBack == null) {
                                Text(stringResource(Res.string.travel_list), style = travelTextStyle(22, 26, FontWeight.Bold), color = Neutral100)
                            }
                            Text(
                                stringResource(Res.string.travel_list_description),
                                style = travelTextStyle(15, 18, FontWeight.Medium),
                                color = colors.textTertiary,
                            )
                            val shape = CircleShape
                            Row(
                                Modifier.padding(top = 12.dp).heightIn(min = 44.dp).noRippleClickable(onClick = onCreate)
                                    .padding(vertical = 7.dp).heightIn(min = 30.dp)
                                    .background(Mint5, shape).border(1.dp, Mint70, shape).padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(painterResource(Res.drawable.ic_travel_add), null, Modifier.size(12.dp), tint = Mint90)
                                Text(stringResource(Res.string.travel_create), style = travelTextStyle(12, 14, FontWeight.Medium), color = Mint90)
                            }
                        }
                        HorizontalDivider(color = colors.borderSubtle)
                    }
                    if (plans.isEmpty()) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Image(painterResource(Res.drawable.img_oguri_walking), null, Modifier.size(112.dp))
                                Text(stringResource(Res.string.travel_empty_plans), color = colors.textTertiary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    items(current, key = TravelPlanSummary::id) { plan ->
                        TravelPlanCard(plan, now.toLocalDateTime(plan.details.timeZone).date) { onOpen(plan.id) }
                    }
                    if (past.isNotEmpty()) {
                        item {
                            HorizontalDivider(color = colors.borderSubtle)
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = pastState }
                                    .noRippleClickable(onClick = { showPast = !showPast }).padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(Res.string.travel_past), Modifier.weight(1f), style = travelTextStyle(15, 18, FontWeight.Medium), color = Neutral100)
                                Icon(
                                    painterResource(Res.drawable.ic_right_arrow), null,
                                    Modifier.rotate(if (showPast) -90f else 90f), tint = colors.textTertiary,
                                )
                            }
                            if (!showPast) HorizontalDivider(color = colors.borderSubtle)
                        }
                        if (showPast) {
                            items(past.asReversed(), key = TravelPlanSummary::id) { plan ->
                                TravelPlanCard(plan, null) { onOpen(plan.id) }
                            }
                            item {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.End) {
                                    TravelTextButton(onClick = onFindNextHoliday) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(Res.string.travel_find_next_holiday))
                                            Icon(painterResource(Res.drawable.ic_right_arrow), null, Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelPlansEmpty(
    illustration: DrawableResource,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(painterResource(illustration), contentDescription = null, modifier = Modifier.size(128.dp))
        Text(message, style = OguriTheme.typography.bodyMedium, color = Neutral70, textAlign = TextAlign.Center)
        TravelPrimaryButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(actionLabel) }
    }
}

@Composable
private fun TravelPlanCard(
    plan: TravelPlanSummary,
    today: LocalDate?,
    onClick: () -> Unit,
) {
    val details = plan.details
    val colors = OguriTheme.colors
    val shape = RoundedCornerShape(8.dp)
    val dayOffset = today?.let { details.dates.start.toEpochDays() - it.toEpochDays() }
    val meta = listOfNotNull(details.city, plan.memberCount?.let { stringResource(Res.string.travel_member_count, it) })
        .joinToString(" · ")
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (today == null) 6.dp else 24.dp)
            .clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderStrong, shape)
            .noRippleClickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().then(if (today != null) Modifier.background(Mint40) else Modifier)
                .padding(horizontal = 18.dp, vertical = if (today == null) 10.dp else 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(details.title, Modifier.weight(1f),
                style = if (today == null) travelTextStyle(15, 18, FontWeight.Medium) else travelTextStyle(18, 21, FontWeight.SemiBold),
                color = Neutral100)
            Icon(painterResource(Res.drawable.ic_right_arrow), null, tint = colors.textPrimary)
        }
        Column(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = if (today == null) 0.dp else 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (dayOffset != null) {
                Text(
                    if (dayOffset > 0) stringResource(Res.string.travel_list_countdown, dayOffset.toInt())
                    else stringResource(Res.string.travel_list_in_progress),
                    style = travelTextStyle(12, 14, FontWeight.Medium),
                    color = Mint90,
                )
                Text(travelCardDates(details.dates, includeYear = true), style = travelTextStyle(15, 18, FontWeight.Medium), color = colors.textPrimary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(Res.drawable.ic_travel_location), null, Modifier.size(14.dp), tint = colors.textTertiary)
                    Text(meta, style = travelTextStyle(11, 13), color = colors.textTertiary)
                }
            } else {
                Text(
                    "${travelCardDates(details.dates, includeYear = true)} · $meta",
                    style = OguriTheme.typography.caption,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
