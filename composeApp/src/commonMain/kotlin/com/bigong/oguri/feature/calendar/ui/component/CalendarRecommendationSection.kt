package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.domain.model.CalendarPeriod
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_recommendation_guide_highlight
import oguri.composeapp.generated.resources.calendar_recommendation_guide_subtitle
import oguri.composeapp.generated.resources.calendar_recommendation_guide_title
import oguri.composeapp.generated.resources.ic_thumb
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarRecommendationSection(
    periods: List<CalendarPeriod>,
    selectedPeriodId: Long?,
    onPeriodClick: (CalendarPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GuideHeader(
            iconResource = Res.drawable.ic_thumb,
            titleText = stringResource(Res.string.calendar_recommendation_guide_title),
            highlightedText = stringResource(Res.string.calendar_recommendation_guide_highlight),
            subtitleText = stringResource(Res.string.calendar_recommendation_guide_subtitle),
        )
        periods.forEach { period ->
            CalendarRecommendationCard(
                period = period,
                isSelected = selectedPeriodId == period.id,
                onClick = onPeriodClick,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
