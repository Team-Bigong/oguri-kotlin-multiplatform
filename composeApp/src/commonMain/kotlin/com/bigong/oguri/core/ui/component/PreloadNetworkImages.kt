package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale

private const val DEFAULT_PRELOAD_IMAGE_COUNT: Int = 8

@Composable
fun PreloadNetworkImages(
    imageUrls: List<String>,
    maxPreloadImageCount: Int = DEFAULT_PRELOAD_IMAGE_COUNT,
) {
    val platformContext = LocalPlatformContext.current
    val imageLoader = SingletonImageLoader.get(platformContext)

    LaunchedEffect(imageUrls, maxPreloadImageCount) {
        imageUrls
            .asSequence()
            .map { imageUrl -> imageUrl.trim() }
            .filter { imageUrl -> imageUrl.isNotEmpty() }
            .distinct()
            .take(maxPreloadImageCount)
            .forEach { imageUrl ->
                val imageRequest =
                    ImageRequest
                        .Builder(platformContext)
                        .data(imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .precision(Precision.INEXACT)
                        .scale(ContentScale.Crop.toCoilScale())
                        .build()
                imageLoader.enqueue(imageRequest)
            }
    }
}

private fun ContentScale.toCoilScale(): Scale =
    when (this) {
        ContentScale.Crop,
        ContentScale.FillBounds,
        ContentScale.FillHeight,
        ContentScale.FillWidth -> Scale.FILL

        else -> Scale.FIT
    }
