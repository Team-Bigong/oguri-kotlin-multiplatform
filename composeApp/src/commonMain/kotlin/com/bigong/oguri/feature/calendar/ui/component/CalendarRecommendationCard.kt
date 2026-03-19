package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_card_detail
import oguri.composeapp.generated.resources.calendar_card_summary_with_holiday
import oguri.composeapp.generated.resources.calendar_card_summary_without_holiday
import oguri.composeapp.generated.resources.calendar_card_total_days
import oguri.composeapp.generated.resources.calendar_d_day_after
import oguri.composeapp.generated.resources.calendar_d_day_before
import oguri.composeapp.generated.resources.calendar_period_range
import oguri.composeapp.generated.resources.ic_right_arrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

private const val MAX_AUTO_SCROLL_PIXELS = 420f
private const val CLIPPED_THRESHOLD_PIXELS = 2f

@Composable
fun CalendarRecommendationCard(
    periodCard: CalendarPeriodCardUiModel,
    isExpanded: Boolean,
    listViewportBottomInWindow: Float,
    onCardClick: (Long) -> Unit,
    onDetailClick: (Long) -> Unit,
    onRequestScrollBy: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldAutoScrollAfterExpand by remember(periodCard.id) { mutableStateOf(false) }
    val currentYear =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year

    val startDateText = periodCard.startDate.toRangeText(currentYear = currentYear)
    val endDateText = periodCard.endDate.toRangeText(currentYear = currentYear)
    val plainTitleText =
        stringResource(
            Res.string.calendar_period_range,
            startDateText,
            endDateText,
        )
    val plainCalendarCardTitleText = stringResource(Res.string.calendar_card_total_days, plainTitleText, periodCard.totalTripCount)
    val dDayText =
        if (periodCard.dDay >= 0) {
            stringResource(Res.string.calendar_d_day_before, periodCard.dDay)
        } else {
            stringResource(Res.string.calendar_d_day_after, -periodCard.dDay)
        }

    val summaryHolidayText = periodCard.holidayNames.joinToString(separator = "・")
    val summaryText =
        if (summaryHolidayText.isBlank()) {
            stringResource(
                Res.string.calendar_card_summary_without_holiday,
                periodCard.totalTripCount - periodCard.dayOffCount,
                periodCard.dayOffCount,
            )
        } else {
            stringResource(
                Res.string.calendar_card_summary_with_holiday,
                summaryHolidayText,
                periodCard.totalTripCount - periodCard.dayOffCount,
                periodCard.dayOffCount,
            )
        }

    val arrowRotation =
        animateFloatAsState(
            targetValue = if (isExpanded) 90f else 0f,
            animationSpec = tween(durationMillis = 240),
            label = "calendar_card_arrow_rotation",
        ).value
    val titleStyleProgress =
        animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(durationMillis = 220),
            label = "calendar_card_title_style_progress",
        ).value
    val animatedTitleStyle =
        lerp(
            start = OguriTheme.typography.cardSubtitle,
            stop = OguriTheme.typography.cardTitle,
            fraction = titleStyleProgress,
        )

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            shouldAutoScrollAfterExpand = true
        } else {
            shouldAutoScrollAfterExpand = false
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(
                    onClick = {
                        HapticType.Selection.perform()
                        onCardClick(periodCard.id)
                    },
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Mint70)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            val shouldBreakAtHyphen = plainTitleText.contains("년")
            val calendarCardTitleText =
                if (shouldBreakAtHyphen) {
                    stringResource(
                        Res.string.calendar_card_total_days,
                        plainTitleText.replace(" - ", "\n- "),
                        periodCard.totalTripCount,
                    )
                } else {
                    plainCalendarCardTitleText
                }

            Text(
                text = calendarCardTitleText,
                style = animatedTitleStyle,
                color = Neutral0,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 36.dp),
            )
            Image(
                painter = painterResource(Res.drawable.ic_right_arrow),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral0),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp)
                        .rotate(arrowRotation),
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Neutral0)
                    .border(
                        width = 1.dp,
                        color = Neutral30,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    ).animateContentSize(animationSpec = tween(durationMillis = 280))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(durationMillis = 260)) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(durationMillis = 220)) + fadeOut(),
            ) {
                Column(
                    modifier =
                        Modifier.noRippleClickable(
                            onClick = {
                                HapticType.Selection.perform()
                                onDetailClick(periodCard.id)
                            },
                        ),
                ) {
                    Text(
                        text = dDayText,
                        style = OguriTheme.typography.labelMedium,
                        color = Mint70,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CalendarMonthGrid(
                        periodCard = periodCard,
                    )

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .background(
                                    color = Mint5,
                                    shape = RoundedCornerShape(8.dp),
                                ).border(
                                    width = 1.dp,
                                    color = Mint70,
                                    shape = RoundedCornerShape(8.dp),
                                ).onGloballyPositioned { coordinates ->
                                    if (!isExpanded || !shouldAutoScrollAfterExpand || listViewportBottomInWindow <= 0f) {
                                        return@onGloballyPositioned
                                    }
                                    val expandedBottomInWindow = coordinates.positionInWindow().y + coordinates.size.height
                                    val clippedHeight = expandedBottomInWindow - listViewportBottomInWindow
                                    if (clippedHeight > CLIPPED_THRESHOLD_PIXELS) {
                                        onRequestScrollBy(clippedHeight.coerceAtMost(MAX_AUTO_SCROLL_PIXELS))
                                    }
                                    shouldAutoScrollAfterExpand = false
                                }.noRippleClickable(
                                    onClick = {
                                        HapticType.Selection.perform()
                                        onDetailClick(periodCard.id)
                                    },
                                ).padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.calendar_card_detail),
                            style = OguriTheme.typography.cardTitle,
                            color = Mint70,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text =
                        summaryHolidayText.takeIf { holidayText -> holidayText.isNotBlank() }?.let { highlightedHoliday ->
                            summaryText.getStyledText(
                                style = OguriTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                highlightedHoliday,
                            )
                        } ?: androidx.compose.ui.text
                            .AnnotatedString(summaryText),
                    style = OguriTheme.typography.bodyMedium,
                    color = Neutral70,
                )
            }
        }
    }
}

private fun LocalDate.toRangeText(currentYear: Int): String =
    if (year == currentYear) {
        "${month.ordinal + 1}월 ${day}일"
    } else {
        "${year}년 ${month.ordinal + 1}월 ${day}일"
    }
