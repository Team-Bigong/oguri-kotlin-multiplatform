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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

private val RANK_TOGGLE_CONTAINER_HEIGHT = 38.dp
private val RANK_TOGGLE_TRACK_COLOR = Color(0xFFF2F4F5)
private val RANK_TOGGLE_INDICATOR_COLOR = Color(0xFFBFEDE5)
private val RANK_TOGGLE_SELECTED_TEXT_COLOR = Color(0xFF162227)
private val RANK_TOGGLE_UNSELECTED_TEXT_COLOR = Color(0xFF2C3740)
private const val RANK_TOGGLE_ANIMATION_DURATION_MILLIS: Int = 220

@Composable
fun RankSegmentedToggle(
    selectedRank: Int,
    onRankSelected: (Int) -> Unit,
    rankLabels: List<String>,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = RANK_TOGGLE_CONTAINER_HEIGHT)
            .background(
                color = RANK_TOGGLE_TRACK_COLOR,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / rankLabels.size
        val targetOffset = segmentWidth * (selectedRank - 1)
        val animatedOffset = animateDpAsState(
            targetValue = targetOffset,
            animationSpec = tween(durationMillis = RANK_TOGGLE_ANIMATION_DURATION_MILLIS),
            label = "rank_segment_offset",
        )

        Box(
            modifier = Modifier
                .offset(x = animatedOffset.value)
                .width(segmentWidth)
                .height(height = RANK_TOGGLE_CONTAINER_HEIGHT - 6.dp)
                .background(
                    color = RANK_TOGGLE_INDICATOR_COLOR,
                    shape = RoundedCornerShape(999.dp),
                ),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            rankLabels.forEachIndexed { index: Int, rankLabelText: String ->
                val rank = index + 1
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .noRippleClickable(onClick = { onRankSelected(rank) })
                        .height(height = RANK_TOGGLE_CONTAINER_HEIGHT - 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rankLabelText,
                        style = OguriTheme.typography.labelLarge,
                        color = if (selectedRank == rank) {
                            RANK_TOGGLE_SELECTED_TEXT_COLOR
                        } else {
                            RANK_TOGGLE_UNSELECTED_TEXT_COLOR
                        },
                    )
                }
            }
        }
    }
}
