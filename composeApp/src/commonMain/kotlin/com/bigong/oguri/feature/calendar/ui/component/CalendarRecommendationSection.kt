package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_end_of_list_message
import oguri.composeapp.generated.resources.calendar_recommendation_guide_highlight
import oguri.composeapp.generated.resources.calendar_recommendation_guide_title
import oguri.composeapp.generated.resources.ic_thumb
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarRecommendationSection(
    periodCards: List<CalendarPeriodCardUiModel>,
    expandedPeriodId: Long?,
    isLoadingNextPage: Boolean,
    showEndHint: Boolean,
    listViewportBottomInWindow: Float,
    onCardClick: (Long) -> Unit,
    onSaveToggleClick: (Long) -> Unit,
    onDetailClick: (Long) -> Unit,
    onRequestScrollBy: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GuideHeader(
            iconResource = Res.drawable.ic_thumb,
            titleText = stringResource(Res.string.calendar_recommendation_guide_title),
            highlightedText = stringResource(Res.string.calendar_recommendation_guide_highlight),
            subtitleText = null,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            periodCards.forEach { periodCard ->
                CalendarRecommendationCard(
                    periodCard = periodCard,
                    isExpanded = expandedPeriodId == periodCard.id,
                    onCardClick = onCardClick,
                    onSaveToggleClick = onSaveToggleClick,
                    onDetailClick = onDetailClick,
                    listViewportBottomInWindow = listViewportBottomInWindow,
                    onRequestScrollBy = onRequestScrollBy,
                )
            }
        }

        if (isLoadingNextPage) {
            Spacer(modifier = Modifier.height(18.dp))
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
            )
        }

        if (showEndHint) {
            Spacer(modifier = Modifier.height(14.dp))
            androidx.compose.material3.Text(
                text = stringResource(Res.string.calendar_end_of_list_message),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodySmall,
                color = com.bigong.oguri.core.designsystem.Neutral50,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
