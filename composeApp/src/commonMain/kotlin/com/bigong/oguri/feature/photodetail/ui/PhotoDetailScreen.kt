package com.bigong.oguri.feature.photodetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.feature.photodetail.ui.component.PhotoDetailTopBar
import com.bigong.oguri.feature.photodetail.ui.component.ZoomablePhotoImage
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.photo_detail_page_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoDetailScreen(
    imageUrls: List<String>,
    initialPage: Int,
    onBackClick: () -> Unit,
) {
    val totalPageCount = imageUrls.size
    val safeInitialPage =
        if (totalPageCount == 0) {
            0
        } else {
            initialPage.coerceIn(minimumValue = 0, maximumValue = totalPageCount - 1)
        }
    val pagerState =
        rememberPagerState(
            initialPage = safeInitialPage,
            pageCount = { if (totalPageCount == 0) 1 else totalPageCount },
        )
    val zoomActiveStateByPage = remember { mutableStateMapOf<Int, Boolean>() }
    val isCurrentPageZoomed = zoomActiveStateByPage[pagerState.currentPage] == true

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .navigationBarsPadding(),
    ) {
        val pageTitleText =
            stringResource(
                Res.string.photo_detail_page_title,
                if (totalPageCount == 0) 0 else pagerState.currentPage + 1,
                totalPageCount,
            )

        PhotoDetailTopBar(
            titleText = pageTitleText,
            onBackClick = onBackClick,
        )

        Box(
            modifier = Modifier.weight(1f),
        ) {
            if (totalPageCount > 0) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !isCurrentPageZoomed,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    ZoomablePhotoImage(
                        imageUrl = imageUrls[page],
                        modifier = Modifier.fillMaxSize(),
                        onZoomActiveChanged = { isZoomActive ->
                            zoomActiveStateByPage[page] = isZoomActive
                        },
                    )
                }
            }
        }
    }
}
