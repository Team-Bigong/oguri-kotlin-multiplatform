package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.AdvertisementCardSkeleton
import com.bigong.oguri.core.ui.component.GuideHeaderSkeleton
import com.bigong.oguri.core.ui.component.PlaceCardSkeleton
import com.bigong.oguri.core.ui.component.SkeletonBox

@Composable
fun HomeSkeletonContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SkeletonBox(
                    modifier = Modifier.padding(horizontal = 20.dp).width(98.dp).height(38.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonBox(modifier = Modifier.width(180.dp).height(22.dp))
                    SkeletonBox(modifier = Modifier.width(220.dp).height(34.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .background(
                                color = Neutral10,
                                shape =
                                    androidx.compose.foundation.shape
                                        .RoundedCornerShape(12.dp),
                            ).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            SkeletonBox(
                                modifier = Modifier.width(48.dp).height(26.dp),
                                shape =
                                    androidx.compose.foundation.shape
                                        .RoundedCornerShape(13.dp),
                            )
                        }
                    }
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(40.dp))
                    SkeletonBox(modifier = Modifier.width(170.dp).height(24.dp))
                    SkeletonBox(modifier = Modifier.width(132.dp).height(18.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeaderSkeleton(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    withSubtitle = true,
                )
            }
            item {
                Spacer(modifier = Modifier.height(18.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(3) {
                        PlaceCardSkeleton(modifier = Modifier.width(154.dp))
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeaderSkeleton(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    withSubtitle = true,
                )
            }
            items(3) {
                Spacer(modifier = Modifier.height(18.dp))
                AdvertisementCardSkeleton(modifier = Modifier.padding(horizontal = 20.dp))
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SkeletonBox(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
