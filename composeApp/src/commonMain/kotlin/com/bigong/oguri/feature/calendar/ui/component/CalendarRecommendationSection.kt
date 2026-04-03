package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.bigong.oguri.core.ad.AdMobBanner
import com.bigong.oguri.core.ad.AdMobBannerPlacement
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
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

private const val INITIAL_INLINE_BANNER_AFTER_CARD_INDEX = 3
private const val INLINE_BANNER_INTERVAL = 8

fun LazyListScope.calendarRecommendationSection(
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
) {
    item(key = "calendar_recommendation_guide") {
        GuideHeader(
            iconResource = Res.drawable.ic_thumb,
            titleText = stringResource(Res.string.calendar_recommendation_guide_title),
            highlightedText = stringResource(Res.string.calendar_recommendation_guide_highlight),
            subtitleText = null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
    }

    item(key = "calendar_recommendation_guide_spacing") {
        Spacer(modifier = Modifier.height(16.dp))
    }

    items(
        count = pagedPeriodCards.itemCount,
        key = { index -> "calendar_period_card_$index" },
    ) { index ->
        val periodCard: CalendarPeriodCardUiModel = pagedPeriodCards[index] ?: return@items
        val periodKey: String = periodCard.toRecommendationPeriodKey()
        val resolvedSavedState: Boolean = savedStateByPeriodKey[periodKey] ?: periodCard.isSaved
        val resolvedPeriodCard =
            if (resolvedSavedState == periodCard.isSaved) {
                periodCard
            } else {
                periodCard.copy(isSaved = resolvedSavedState)
            }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
        ) {
            CalendarRecommendationCard(
                periodCard = resolvedPeriodCard,
                isExpanded = expandedPeriodId == resolvedPeriodCard.id,
                onCardClick = onCardClick,
                onSaveToggleClick = onSaveToggleClick,
                onDetailClick = onDetailClick,
                listViewportBottomInWindow = listViewportBottomInWindow,
                onRequestScrollBy = onRequestScrollBy,
            )

            if (shouldShowInlineBanner(afterCardIndex = index)) {
                Spacer(modifier = Modifier.height(18.dp))
                AdMobBanner(
                    placement = AdMobBannerPlacement.CALENDAR_INLINE,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (isLoadingNextPage) {
        item(key = "calendar_recommendation_append_loading") {
            SkeletonBox(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(120.dp),
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (showEndHint) {
        item(key = "calendar_recommendation_end_hint") {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(Res.string.calendar_end_of_list_message),
                style = OguriTheme.typography.bodySmall,
                color = Neutral50,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
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
