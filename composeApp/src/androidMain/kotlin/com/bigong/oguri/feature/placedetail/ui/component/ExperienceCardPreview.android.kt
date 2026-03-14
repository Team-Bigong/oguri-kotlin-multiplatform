package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Experience

@Preview(showBackground = true)
@Composable
private fun ExperienceCardPreview() {
    OguriTheme {
        ExperienceCard(
            experience =
                Experience(
                    title = "다이빙 체험",
                    summary = "맑은 바다 속에서 형형색색 산호와 물고기를 만나요.",
                    thumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/oguri-af89b.firebasestorage.app/o/drawable%2Fimg_activity_2.jpg?alt=media&token=d537d230-b208-4a74-8e92-137b09b36945",
                    advertisementUrl = "https://www.klook.com/ko/",
                ),
        )
    }
}
