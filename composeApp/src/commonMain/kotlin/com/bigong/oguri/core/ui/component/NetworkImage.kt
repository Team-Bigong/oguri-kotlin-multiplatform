package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale

@Composable
fun NetworkImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val platformContext = LocalPlatformContext.current
    val imageRequest =
        remember(imageUrl, contentScale, platformContext) {
            ImageRequest
                .Builder(platformContext)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .precision(Precision.INEXACT)
                .scale(contentScale.toScale())
                .build()
        }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

private fun ContentScale.toScale(): Scale =
    when (this) {
        ContentScale.Crop,
        ContentScale.FillBounds,
        ContentScale.FillHeight,
        ContentScale.FillWidth,
        -> Scale.FILL

        else -> Scale.FIT
    }
