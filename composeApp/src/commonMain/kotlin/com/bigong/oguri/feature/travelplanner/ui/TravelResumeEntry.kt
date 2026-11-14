package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.model.hasEnded
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun TravelResumeEntry(
    viewModel: TravelPlannerViewModel,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.loadResume() }
    }
    val plan = state.resumePlan ?: return
    TravelEntryCard(
        label = stringResource(Res.string.travel_resume),
        plan = plan,
        onClick = { onOpen(plan.id) },
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 12.dp),
    )
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
            style = OguriTheme.typography.bodySmall, color = colors.textTertiary,
        )
        Text(stringResource(Res.string.travel_home_create_plan), style = OguriTheme.typography.labelLarge, color = colors.textPrimary)
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
                Text(stringResource(Res.string.travel_create_another), color = colors.textTertiary)
            }
        }
    } else {
        TravelOutlineButton(onClick = onCreate, modifier = modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(Res.drawable.ic_travel_add), null, Modifier.size(16.dp), tint = colors.mint)
                Text(stringResource(Res.string.travel_create_from_city))
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
            Text(label, style = OguriTheme.typography.caption, color = colors.textTertiary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(plan.details.title, style = OguriTheme.typography.cardSubtitle, color = colors.textPrimary)
                Text(travelCardDates(plan.details.dates, includeYear = false), style = OguriTheme.typography.bodySmall, color = colors.textTertiary)
            }
        }
        if (action != null) Text(action, style = OguriTheme.typography.labelLarge, color = colors.textPrimary)
        Icon(painterResource(Res.drawable.ic_right_arrow), null, Modifier.size(16.dp), tint = colors.textTertiary)
    }
}

@Composable
private fun Modifier.travelEntrySurface(onClick: () -> Unit): Modifier {
    val colors = OguriTheme.colors
    val shape = RoundedCornerShape(8.dp)
    return fillMaxWidth().heightIn(min = 48.dp).clip(shape).background(colors.surfaceCard).border(1.dp, colors.borderSubtle, shape)
        .noRippleClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
}
