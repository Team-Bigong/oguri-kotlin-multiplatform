package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.GuideHeaderSkeleton
import com.bigong.oguri.core.ui.component.SkeletonBox

@Composable
fun MyPageSkeletonContent() {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5),
    ) {
        item {
            androidx.compose.foundation.layout.Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Neutral10)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    SkeletonBox(
                        modifier = Modifier.width(48.dp).height(48.dp),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(24.dp),
                    )
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SkeletonBox(modifier = Modifier.width(130.dp).height(26.dp))
                        SkeletonBox(modifier = Modifier.width(220.dp).height(20.dp))
                    }
                }
            }
            androidx.compose.material3.HorizontalDivider(color = Neutral20)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            GuideHeaderSkeleton(
                modifier = Modifier.padding(horizontal = 20.dp),
                withSubtitle = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(2) {
                    SkeletonBox(modifier = Modifier.width(174.dp).height(118.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            GuideHeaderSkeleton(
                modifier = Modifier.padding(horizontal = 20.dp),
                withSubtitle = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(3) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.width(130.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(92.dp))
                        SkeletonBox(modifier = Modifier.width(74.dp).height(20.dp))
                        SkeletonBox(modifier = Modifier.width(52.dp).height(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            repeat(5) {
                SkeletonBox(
                    modifier =
                        Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .height(30.dp),
                )
                Spacer(modifier = Modifier.height(22.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
