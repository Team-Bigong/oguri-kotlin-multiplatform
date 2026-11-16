package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.SkeletonBox

private val skeletonLineShape = RoundedCornerShape(4.dp)

internal fun Modifier.travelLoadingSemantics(): Modifier =
    semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }

@Composable
internal fun TravelPlanSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().travelLoadingSemantics()) {
        TravelDayBar(
            datesContent = {
                Row(
                    Modifier.fillMaxWidth().clipToBounds()
                        .horizontalScroll(rememberScrollState(), enabled = false)
                        .padding(start = 20.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { SkeletonBox(Modifier.size(width = 112.dp, height = 30.dp), CircleShape) }
                }
            },
            overviewContent = {
                SkeletonBox(Modifier.padding(top = 4.dp).size(width = 84.dp, height = 30.dp), CircleShape)
            },
        )
        HorizontalDivider(color = OguriTheme.colors.borderSubtle)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            SkeletonBox(Modifier.fillMaxWidth().height(230.dp))
            Spacer(Modifier.height(20.dp))
            SkeletonBox(Modifier.size(width = 128.dp, height = 16.dp), skeletonLineShape)
            Spacer(Modifier.height(16.dp))
            listOf(124.dp, 166.dp, 105.dp).forEach { titleWidth ->
                Row(
                    Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SkeletonBox(Modifier.size(24.dp), CircleShape)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBox(Modifier.size(width = titleWidth, height = 14.dp), skeletonLineShape)
                        SkeletonBox(Modifier.size(width = 82.dp, height = 10.dp), skeletonLineShape)
                    }
                }
            }
        }
    }
}

@Composable
internal fun TravelSearchResultsSkeleton() {
    Column(Modifier.fillMaxWidth().travelLoadingSemantics()) {
        listOf(100.dp to 182.dp, 124.dp to 206.dp, 88.dp to 162.dp).forEach { (titleWidth, addressWidth) ->
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBox(Modifier.width(titleWidth).height(14.dp), skeletonLineShape)
                SkeletonBox(Modifier.width(addressWidth).height(10.dp), skeletonLineShape)
            }
            HorizontalDivider(color = OguriTheme.colors.borderSubtle)
        }
    }
}

@Composable
internal fun TravelInviteSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().travelLoadingSemantics().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkeletonBox(Modifier.size(160.dp), CircleShape)
        Spacer(Modifier.height(20.dp))
        SkeletonBox(Modifier.size(width = 140.dp, height = 14.dp), skeletonLineShape)
        Spacer(Modifier.height(10.dp))
        SkeletonBox(Modifier.size(width = 230.dp, height = 22.dp), skeletonLineShape)
        Spacer(Modifier.height(28.dp))
        SkeletonBox(Modifier.fillMaxWidth().height(118.dp))
        Spacer(Modifier.height(16.dp))
        SkeletonBox(Modifier.size(width = 192.dp, height = 12.dp), skeletonLineShape)
    }
}
