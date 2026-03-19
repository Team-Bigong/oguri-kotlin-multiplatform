package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Place

@Preview(showBackground = true)
@Composable
private fun MyPageSavedPlaceCardPreview() {
    OguriTheme {
        MyPageSavedPlaceCard(
            place =
                Place(
                    id = 1L,
                    country = "필리핀",
                    city = "보라카이",
                    summary = "",
                    thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                ),
            onClick = {},
            onDeleteClick = {},
        )
    }
}
