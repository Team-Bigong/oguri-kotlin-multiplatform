package com.bigong.oguri.feature.photodetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.ad.AdMobBanner
import com.bigong.oguri.core.ad.AdMobBannerPlacement
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
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    var bottomBannerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val topBarHeightDp = with(density) { topBarHeightPx.toDp() }
    val bottomBannerHeightDp = with(density) { bottomBannerHeightPx.toDp() }
    val zoomActiveStateByPage = remember { mutableStateMapOf<Int, Boolean>() }
    val isCurrentPageZoomed = zoomActiveStateByPage[pagerState.currentPage] == true

    Box(
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

        if (totalPageCount > 0) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isCurrentPageZoomed,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeightDp, bottom = bottomBannerHeightDp),
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

        PhotoDetailTopBar(
            titleText = pageTitleText,
            onBackClick = onBackClick,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { measuredSize ->
                        topBarHeightPx = measuredSize.height
                    },
        )

        AdMobBanner(
            placement = AdMobBannerPlacement.PHOTO_DETAIL_BOTTOM,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { measuredSize ->
                        bottomBannerHeightPx = measuredSize.height
                    },
        )
    }
}
