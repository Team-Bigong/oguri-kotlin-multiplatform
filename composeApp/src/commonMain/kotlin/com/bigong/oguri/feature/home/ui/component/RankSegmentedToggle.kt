package com.bigong.oguri.feature.home.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform

private const val RANK_TOGGLE_ANIMATION_DURATION_MILLIS = 220

@Composable
fun RankSegmentedToggle(
    selectedRank: Int,
    onRankSelected: (Int) -> Unit,
    rankLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height = 26.dp)
                .background(
                    color = Neutral0,
                    shape = RoundedCornerShape(999.dp),
                ).padding(2.dp),
    ) {
        val segmentWidth = maxWidth / rankLabels.size
        val targetOffset = segmentWidth * (selectedRank - 1)
        val animatedOffset =
            animateDpAsState(
                targetValue = targetOffset,
                animationSpec = tween(durationMillis = RANK_TOGGLE_ANIMATION_DURATION_MILLIS),
                label = "rank_segment_offset",
            )

        Box(
            modifier =
                Modifier
                    .offset(x = animatedOffset.value)
                    .width(segmentWidth)
                    .height(height = 26.dp)
                    .background(
                        color = Mint40,
                        shape = RoundedCornerShape(999.dp),
                    ),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            rankLabels.forEachIndexed { index: Int, rankLabelText: String ->
                val rank = index + 1
                Box(
                    modifier =
                        Modifier
                            .width(segmentWidth)
                            .noRippleClickable(
                                onClick = {
                                    if (rank != selectedRank) {
                                        HapticType.Selection.perform()
                                    }
                                    onRankSelected(rank)
                                },
                            ).height(height = 26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rankLabelText,
                        style = OguriTheme.typography.labelMedium,
                        color = Neutral100,
                    )
                }
            }
        }
    }
}
