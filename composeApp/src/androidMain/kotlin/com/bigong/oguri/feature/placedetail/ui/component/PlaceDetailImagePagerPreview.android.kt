package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun PlaceDetailImagePagerPreview() {
    OguriTheme {
        PlaceDetailImagePager(
            imageUrls =
                listOf(
                    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                    "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/b41acf66-b33b-448c-8144-d9aba0df12c0.jpeg",
                ),
            onImageClick = {},
        )
    }
}
