package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun HomeProductCardPreview() {
    OguriTheme {
        HomeProductCard(
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/oguri-af89b.firebasestorage.app/o/drawable%2Fimg_hotel_1.jpg?alt=media&token=c41d7e52-469b-4b25-83fb-0ce154cf66a3",
            titleText = "아고다에서 예쁜 숙소를 둘러볼까요?",
            highlightedText = "예쁜 숙소",
        )
    }
}
