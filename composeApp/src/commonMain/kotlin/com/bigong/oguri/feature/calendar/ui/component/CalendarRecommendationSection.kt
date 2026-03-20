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
import androidx.paging.compose.LazyPagingItems
import com.bigong.oguri.core.ad.AdMobBanner
import com.bigong.oguri.core.ad.AdMobBannerPlacement
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.toRecommendationPeriodKey
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_end_of_list_message
import oguri.composeapp.generated.resources.calendar_recommendation_guide_highlight
import oguri.composeapp.generated.resources.calendar_recommendation_guide_title
import oguri.composeapp.generated.resources.ic_thumb
import org.jetbrains.compose.resources.stringResource

private const val INITIAL_INLINE_BANNER_AFTER_CARD_INDEX = 1
private const val INLINE_BANNER_INTERVAL = 6

@Composable
fun CalendarRecommendationSection(
    pagedPeriodCards: LazyPagingItems<CalendarPeriodCardUiModel>,
    savedStateByPeriodKey: Map<String, Boolean>,
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
            for (index in 0 until pagedPeriodCards.itemCount) {
                val periodCard: CalendarPeriodCardUiModel = pagedPeriodCards[index] ?: continue
                val periodKey: String = periodCard.toRecommendationPeriodKey()
                val resolvedSavedState: Boolean = savedStateByPeriodKey[periodKey] ?: periodCard.isSaved
                val resolvedPeriodCard =
                    if (resolvedSavedState == periodCard.isSaved) {
                        periodCard
                    } else {
                        periodCard.copy(isSaved = resolvedSavedState)
                    }
                androidx.compose.runtime.key(periodCard.id) {
                    CalendarRecommendationCard(
                        periodCard = resolvedPeriodCard,
                        isExpanded = expandedPeriodId == resolvedPeriodCard.id,
                        onCardClick = onCardClick,
                        onSaveToggleClick = onSaveToggleClick,
                        onDetailClick = onDetailClick,
                        listViewportBottomInWindow = listViewportBottomInWindow,
                        onRequestScrollBy = onRequestScrollBy,
                    )
                }

                if (shouldShowInlineBanner(afterCardIndex = index)) {
                    AdMobBanner(
                        placement = AdMobBannerPlacement.CALENDAR_INLINE,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                    )
                }
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

private fun shouldShowInlineBanner(afterCardIndex: Int): Boolean {
    if (afterCardIndex < INITIAL_INLINE_BANNER_AFTER_CARD_INDEX) {
        return false
    }
    if (afterCardIndex == INITIAL_INLINE_BANNER_AFTER_CARD_INDEX) {
        return true
    }
    return (afterCardIndex - INITIAL_INLINE_BANNER_AFTER_CARD_INDEX) % INLINE_BANNER_INTERVAL == 0
}
