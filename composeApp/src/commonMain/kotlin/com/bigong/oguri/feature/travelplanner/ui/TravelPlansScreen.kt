package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.bigong.oguri.feature.travelplanner.ui.component.travelLoadingSemantics
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelSignInContent
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
            !signedIn -> TravelSignInContent(onSignIn = onSignIn, showPageTitle = onBack == null)
            failure != null && !state.isLoading -> {
                TravelPlansHeading(showPageTitle = onBack == null)
                TravelPlansEmpty(Res.drawable.img_oguri_empty, stringResource(Res.string.travel_list_failure_message), stringResource(Res.string.travel_list_retry), onRetry, stringResource(Res.string.travel_list_failure_title), Modifier.weight(1f), illustrationSize = 200.dp)
            }
            plans == null || failure != null -> {
                TravelPlansHeading(showPageTitle = onBack == null)
                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp).travelLoadingSemantics()) {
                    SkeletonBox(Modifier.fillMaxWidth().height(134.dp))
                    Spacer(Modifier.height(16.dp))
                    SkeletonBox(Modifier.fillMaxWidth().height(45.dp))
                    Spacer(Modifier.height(28.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.5f).height(16.dp), RoundedCornerShape(4.dp))
                }
            }
            plans.isEmpty() -> {
                TravelPlansHeading(showPageTitle = onBack == null)
                TravelPlansEmpty(Res.drawable.img_oguri_parasol, stringResource(Res.string.travel_empty_description), stringResource(Res.string.travel_create), onCreate, stringResource(Res.string.travel_empty_title), Modifier.weight(1f), showPlus = true)
            }
            else -> {
                var showPast by rememberSaveable { mutableStateOf(false) }
                val now = Clock.System.now()
                val (past, current) = plans.partition { plan -> plan.hasEnded { now.toLocalDateTime(it).date } }
                val pastState = stringResource(if (showPast) Res.string.travel_past_expanded else Res.string.travel_past_collapsed)
                LazyColumn {
                    item {
                        TravelPlansHeading(showPageTitle = onBack == null, bottomPadding = 24.dp)
                    }
                    items(current, key = TravelPlanSummary::id) { plan ->
                        TravelPlanCard(plan, now.toLocalDateTime(plan.details.timeZone).date) { onOpen(plan.id) }
                    }
                    item {
                        TravelOutlineButton(onClick = onCreate, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = if (current.isEmpty()) 24.dp else 0.dp, bottom = 24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(painterResource(Res.drawable.ic_travel_plus), null, Modifier.size(18.dp))
                                Text(stringResource(Res.string.travel_create))
                            }
                        }
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
private fun TravelPlansHeading(
    showPageTitle: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp = 16.dp,
) {
    val colors = OguriTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showPageTitle) {
            Text(stringResource(Res.string.travel_list), style = travelTextStyle(22, 26, FontWeight.Bold), color = Neutral100)
        }
        Text(
            stringResource(Res.string.travel_list_description),
            style = travelTextStyle(15, 18, FontWeight.Medium),
            color = colors.textTertiary,
        )
    }
    HorizontalDivider(color = colors.borderSubtle)
}

@Composable
internal fun TravelPlansEmpty(
    illustration: DrawableResource,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    title: String = "",
    modifier: Modifier = Modifier,
    showPlus: Boolean = false,
    illustrationSize: androidx.compose.ui.unit.Dp = 160.dp,
    outlinedAction: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(illustration), contentDescription = null, modifier = Modifier.size(illustrationSize))
        Spacer(Modifier.height(24.dp))
        if (title.isNotBlank()) Text(title, style = OguriTheme.typography.cardTitle, color = Neutral100, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(message, style = OguriTheme.typography.bodyMedium, color = Neutral70, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        if (outlinedAction) {
            TravelOutlineButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                Text(actionLabel)
            }
        } else TravelPrimaryButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showPlus) Icon(painterResource(Res.drawable.ic_travel_plus), null, Modifier.size(18.dp))
                Text(actionLabel)
            }
        }
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
    if (today == null) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderSubtle, shape)
                .noRippleClickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(details.title, style = travelTextStyle(15, 18, FontWeight.Medium), color = Neutral100)
                Text(
                    "${travelCardDates(details.dates, includeYear = true)} · $meta",
                    style = travelTextStyle(11, 13), color = colors.textTertiary,
                )
            }
            Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(20.dp), tint = colors.textTertiary)
        }
        return
    }
    val headingColor = if (OguriTheme.isDarkTheme) Neutral100 else Color.White
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
            .clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderStrong, shape)
            .noRippleClickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().background(if (OguriTheme.isDarkTheme) Mint40 else Mint70)
                .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                details.title, Modifier.weight(1f), style = travelTextStyle(18, 21, FontWeight.SemiBold),
                color = headingColor, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Icon(painterResource(Res.drawable.ic_travel_card_arrow), null, Modifier.size(24.dp), tint = headingColor)
        }
        Column(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (dayOffset != null && dayOffset > 0) stringResource(Res.string.travel_list_countdown, dayOffset.toInt())
                else stringResource(Res.string.travel_list_in_progress),
                style = travelTextStyle(12, 14, FontWeight.Medium),
                color = if (OguriTheme.isDarkTheme) Mint90 else Mint70,
            )
            Text(travelCardDates(details.dates, includeYear = true), style = travelTextStyle(15, 18, FontWeight.Medium), color = colors.textPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painterResource(Res.drawable.ic_travel_pin), null, Modifier.size(14.dp), tint = colors.textTertiary)
                Text(meta, style = travelTextStyle(11, 13), color = colors.textTertiary)
            }
        }
    }
}
