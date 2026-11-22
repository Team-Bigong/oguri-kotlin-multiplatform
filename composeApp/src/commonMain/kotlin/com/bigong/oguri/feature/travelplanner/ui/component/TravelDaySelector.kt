package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_travel_overview_calendar
import oguri.composeapp.generated.resources.travel_all_days_tab
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val dayBarFadeWidth = 28.dp
private val dayBarChipHeight = 30.dp
private val dayBarTouchPadding = PaddingValues(top = 4.dp, bottom = 10.dp)

@Composable
internal fun TravelDaySelector(
    dates: TravelDates,
    selectedDay: Int,
    enabled: Boolean,
    onSelectDay: (Int) -> Unit,
    onOverview: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDay, dates) {
        if (selectedDay in 1..dates.dayCount) {
            val index = selectedDay - 1
            val layout = listState.layoutInfo
            val visibleEnd = layout.viewportEndOffset - layout.afterContentPadding
            if (layout.visibleItemsInfo.none { it.index == index && it.offset >= 0 && it.offset + it.size <= visibleEnd }) {
                listState.scrollToItem(index)
            }
        }
    }

    TravelDayBar(
        datesContent = {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = dayBarFadeWidth),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(dates.dayCount) { index ->
                    val day = index + 1
                    TravelChoiceChip(
                        label = travelDayLabel(dates, day),
                        selected = selectedDay == day,
                        enabled = enabled,
                        onClick = { onSelectDay(day) },
                        touchPadding = dayBarTouchPadding,
                    )
                }
            }
        },
        overviewContent = { TravelOverviewButton(enabled = enabled, onClick = onOverview) },
    )
}

@Composable
internal fun TravelDayBar(
    datesContent: @Composable () -> Unit,
    overviewContent: @Composable () -> Unit,
) {
    val background = OguriTheme.colors.backgroundSecondary
    Row(Modifier.fillMaxWidth().heightIn(min = 49.dp)) {
        Box(Modifier.weight(1f)) {
            datesContent()
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 4.dp)
                    .width(dayBarFadeWidth).height(dayBarChipHeight)
                    .drawBehind {
                        val isLtr = layoutDirection == LayoutDirection.Ltr
                        // The Figma fade reaches full opacity 24px into the 28px overlap.
                        drawRect(
                            Brush.horizontalGradient(
                                0f to background.copy(alpha = 0f),
                                0.87f to background,
                                startX = if (isLtr) 0f else size.width,
                                endX = if (isLtr) size.width else 0f,
                            ),
                        )
                    },
            )
        }
        Box(Modifier.padding(end = 20.dp)) { overviewContent() }
    }
}

@Composable
private fun TravelOverviewButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = OguriTheme.colors
    val foreground = if (enabled) colors.textSecondary else Neutral40
    Row(
        modifier = Modifier
            .clickable(interactionSource = null, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(dayBarTouchPadding)
            .widthIn(min = 80.dp, max = 160.dp)
            .heightIn(min = dayBarChipHeight)
            .background(if (enabled) colors.surfaceCard else colors.backgroundTertiary, CircleShape)
            .border(1.dp, colors.borderSubtle, CircleShape)
            .padding(start = 9.dp, end = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(Res.drawable.ic_travel_overview_calendar), null, Modifier.size(14.dp), tint = foreground)
        Text(
            stringResource(Res.string.travel_all_days_tab),
            style = travelTextStyle(12, 14, FontWeight.Medium),
            color = foreground,
        )
    }
}
