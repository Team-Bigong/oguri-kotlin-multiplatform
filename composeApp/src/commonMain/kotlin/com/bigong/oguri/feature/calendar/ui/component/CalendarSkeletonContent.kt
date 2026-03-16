package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.GuideHeaderSkeleton
import com.bigong.oguri.core.ui.component.SkeletonBox

@Composable
fun CalendarSkeletonContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).width(220.dp).height(42.dp))
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).width(240.dp).height(24.dp))
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(58.dp))
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Neutral20)
        Spacer(modifier = Modifier.height(18.dp))
        SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).width(160.dp).height(36.dp))
        Spacer(modifier = Modifier.height(14.dp))
        SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(268.dp))
        Spacer(modifier = Modifier.height(22.dp))
        GuideHeaderSkeleton(
            modifier = Modifier.padding(horizontal = 20.dp),
            withSubtitle = true,
        )
        Spacer(modifier = Modifier.height(18.dp))
        repeat(3) {
            SkeletonBox(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(78.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
