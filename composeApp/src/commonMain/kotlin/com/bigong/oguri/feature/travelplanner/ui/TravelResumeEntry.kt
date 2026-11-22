package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.designsystem.Mint80
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import kotlin.time.Clock
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelResumeEntry(
    viewModel: TravelPlannerViewModel,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val plan = state.resumePlan ?: return
    TravelResumeCard(plan = plan, onClick = { onOpen(plan.id) })
}

@Composable
private fun TravelResumeCard(plan: TravelPlanSummary, onClick: () -> Unit) {
    val details = plan.details
    val today = Clock.System.now().toLocalDateTime(details.timeZone).date
    val daysUntilDeparture = today.daysUntil(details.dates.start)
    val badge = when {
        daysUntilDeparture > 0 -> stringResource(Res.string.travel_list_countdown, daysUntilDeparture)
        today <= details.dates.endInclusive -> stringResource(Res.string.travel_list_in_progress)
        else -> null
    }
    val colors = OguriTheme.colors
    val typography = OguriTheme.typography
    val labelStyle = typography.labelSmall.copy(lineHeight = 13.sp)

    Row(
        modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp).travelEntrySurface(
            onClick = onClick,
            minHeight = 84.dp,
            contentPadding = PaddingValues(start = 17.dp, top = 13.dp, end = 14.dp, bottom = 13.dp),
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.travel_resume),
                    modifier = Modifier.weight(1f, fill = false),
                    style = labelStyle,
                    color = colors.textTertiary,
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        modifier = Modifier.background(colors.mintContainer, CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        style = labelStyle,
                        color = Mint80,
                    )
                }
            }
            Text(
                text = details.title,
                style = typography.cardSubtitle.copy(lineHeight = 18.sp),
                color = Neutral100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = travelCardDates(details.dates, includeYear = false),
                style = typography.caption.copy(lineHeight = 14.sp),
                color = colors.textTertiary,
            )
        }
        Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(20.dp), tint = colors.textTertiary)
    }
}

@Composable
internal fun TravelHomeGuide(viewModel: TravelPlannerViewModel?, onCreate: () -> Unit) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value
    if (viewModel != null && (state?.resumePlan != null || state?.resumeLoaded != true)) return
    val colors = OguriTheme.colors
    Row(
        Modifier.padding(horizontal = 20.dp).padding(top = 20.dp).travelEntrySurface(onCreate),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.travel_home_guide), Modifier.weight(1f),
            style = travelTextStyle(13, 16), color = colors.textTertiary,
        )
        Text(stringResource(Res.string.travel_home_create_plan), style = OguriTheme.typography.labelLarge, color = Neutral100)
        Icon(painterResource(Res.drawable.ic_right_arrow), null, Modifier.size(16.dp), tint = colors.textTertiary)
    }
}

@Composable
internal fun TravelDestinationEntry(
    plans: List<TravelPlanSummary>,
    onOpen: ((String) -> Unit)?,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = Clock.System.now()
    val plan = plans.firstOrNull { !it.hasEnded { zone -> now.toLocalDateTime(zone).date } }
    val colors = OguriTheme.colors
    if (plan != null && onOpen != null) {
        Column(modifier) {
            TravelEntryCard(
                label = stringResource(Res.string.travel_destination_existing),
                plan = plan,
                action = stringResource(Res.string.travel_open_existing_city_plan),
                onClick = { onOpen(plan.id) },
            )
            TravelTextButton(onClick = onCreate, modifier = Modifier.align(Alignment.End)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(Res.drawable.ic_travel_plus), null, Modifier.size(14.dp), tint = colors.textTertiary)
                    Text(stringResource(Res.string.travel_create_another), style = travelTextStyle(13, 16), color = colors.textTertiary)
                }
            }
        }
    } else {
        TravelOutlineButton(onClick = onCreate, modifier = modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(Res.drawable.ic_travel_plus), null, Modifier.size(16.dp))
                Text(stringResource(Res.string.travel_create_from_city), style = travelTextStyle(18, 22, FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun TravelEntryCard(
    label: String,
    plan: TravelPlanSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: String? = null,
) {
    val colors = OguriTheme.colors
    Row(
        modifier.travelEntrySurface(onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = travelTextStyle(11, 14), color = colors.textTertiary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    plan.details.title,
                    modifier = Modifier.alignByBaseline(),
                    style = travelTextStyle(15, 18, FontWeight.Medium),
                    color = Neutral100,
                )
                Text(
                    travelCardDates(plan.details.dates, includeYear = false),
                    modifier = Modifier.alignByBaseline(),
                    style = travelTextStyle(12, 14),
                    color = colors.textTertiary,
                )
            }
        }
        if (action != null) Text(action, style = OguriTheme.typography.labelLarge, color = Neutral100)
        Icon(painterResource(Res.drawable.ic_right_arrow), null, Modifier.size(16.dp), tint = colors.textTertiary)
    }
}

@Composable
private fun Modifier.travelEntrySurface(
    onClick: () -> Unit,
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
): Modifier {
    val colors = OguriTheme.colors
    val shape = RoundedCornerShape(8.dp)
    return fillMaxWidth().heightIn(min = minHeight).clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderSubtle, shape)
        .noRippleClickable(onClick = onClick).padding(contentPadding)
}
