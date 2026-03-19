package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.AdvertisementCardSkeleton
import com.bigong.oguri.core.ui.component.GuideHeaderSkeleton
import com.bigong.oguri.core.ui.component.PlaceCardSkeleton
import com.bigong.oguri.core.ui.component.SkeletonBox

@Composable
fun PlaceDetailSkeletonContent() {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            SkeletonBox(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(0.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).width(170.dp).height(34.dp))
            Spacer(modifier = Modifier.height(12.dp))
            repeat(3) {
                SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(18.dp))
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            GuideHeaderSkeleton(
                modifier = Modifier.padding(horizontal = 20.dp),
                withSubtitle = false,
            )
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(3) {
                    Column(
                        modifier = Modifier.width(190.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(98.dp))
                        SkeletonBox(modifier = Modifier.width(90.dp).height(24.dp))
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(34.dp))
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(28.dp))
            GuideHeaderSkeleton(
                modifier = Modifier.padding(horizontal = 20.dp),
                withSubtitle = false,
            )
            Spacer(modifier = Modifier.height(18.dp))
            AdvertisementCardSkeleton(modifier = Modifier.padding(horizontal = 20.dp))
        }
        item {
            Spacer(modifier = Modifier.height(28.dp))
            GuideHeaderSkeleton(
                modifier = Modifier.padding(horizontal = 20.dp),
                withSubtitle = false,
            )
            Spacer(modifier = Modifier.height(18.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(3) {
                    PlaceCardSkeleton(modifier = Modifier.width(154.dp))
                }
            }
        }
    }
}
