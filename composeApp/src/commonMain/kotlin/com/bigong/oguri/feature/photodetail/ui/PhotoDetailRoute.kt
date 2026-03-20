package com.bigong.oguri.feature.photodetail.ui

import androidx.compose.runtime.Composable

@Composable
fun PhotoDetailRoute(
    imageUrls: List<String>,
    initialPage: Int,
    onBackClick: () -> Unit,
) {
    PhotoDetailScreen(
        imageUrls = imageUrls,
        initialPage = initialPage,
        onBackClick = onBackClick,
    )
}
