package com.bigong.oguri.feature.photodetail.ui.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

private const val MINIMUM_SCALE = 1f
private const val MAXIMUM_SCALE = 4f
private const val DOUBLE_TAP_SCALE = 2.5f
private const val SCALE_ACTIVE_THRESHOLD = 0.01f
private const val OUT_OF_BOUNDS_RESISTANCE = 0.62f
private const val OUT_OF_BOUNDS_CURVE_DISTANCE = 640f
private const val SETTLE_TRIGGER_DELAY_MILLIS = 80L
private const val SETTLE_ANIMATION_DURATION_MILLIS = 220

@Composable
fun ZoomablePhotoImage(
    imageUrl: String,
    onZoomActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember(imageUrl) { mutableFloatStateOf(MINIMUM_SCALE) }
    var offset by remember(imageUrl) { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val painter = rememberAsyncImagePainter(model = imageUrl)

    fun resolveImageSize(): IntSize {
        val intrinsicSize = painter.intrinsicSize
        return if (intrinsicSize != Size.Unspecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
            IntSize(
                width = intrinsicSize.width.toInt(),
                height = intrinsicSize.height.toInt(),
            )
        } else {
            containerSize
        }
    }

    suspend fun settleOffsetWithinBounds() {
        val imageSize = resolveImageSize()
        val bounds = calculateOffsetBounds(scale = scale, containerSize = containerSize, imageSize = imageSize)
        val targetOffset =
            Offset(
                x = offset.x.coerceIn(minimumValue = -bounds.x, maximumValue = bounds.x),
                y = offset.y.coerceIn(minimumValue = -bounds.y, maximumValue = bounds.y),
            )

        val startOffset = offset
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = SETTLE_ANIMATION_DURATION_MILLIS),
        ) { progress, _ ->
            offset =
                Offset(
                    x = startOffset.x + (targetOffset.x - startOffset.x) * progress,
                    y = startOffset.y + (targetOffset.y - startOffset.y) * progress,
                )
        }
    }

    fun scheduleSettle() {
        settleJob?.cancel()
        settleJob =
            coroutineScope.launch {
                delay(SETTLE_TRIGGER_DELAY_MILLIS)
                if (scale <= MINIMUM_SCALE + SCALE_ACTIVE_THRESHOLD) {
                    scale = MINIMUM_SCALE
                    offset = Offset.Zero
                    onZoomActiveChanged(false)
                } else {
                    settleOffsetWithinBounds()
                    onZoomActiveChanged(true)
                }
            }
    }

    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            val imageSize = resolveImageSize()
            val nextScale = (scale * zoomChange).coerceIn(minimumValue = MINIMUM_SCALE, maximumValue = MAXIMUM_SCALE)
            val scaleFactor = nextScale / scale
            val scaledOffset = offset * scaleFactor
            val nextOffset = scaledOffset + panChange
            val bounds =
                calculateOffsetBounds(
                    scale = nextScale,
                    containerSize = containerSize,
                    imageSize = imageSize,
                )

            offset =
                Offset(
                    x = applyResistance(value = nextOffset.x, maxOffset = bounds.x),
                    y = applyResistance(value = nextOffset.y, maxOffset = bounds.y),
                )
            scale = nextScale
            onZoomActiveChanged(nextScale > MINIMUM_SCALE + SCALE_ACTIVE_THRESHOLD)
            scheduleSettle()
        }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier =
            modifier
                .onSizeChanged { measuredSize ->
                    containerSize = measuredSize
                }.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }.pointerInput(imageUrl) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            settleJob?.cancel()

                            if (scale > MINIMUM_SCALE + SCALE_ACTIVE_THRESHOLD) {
                                scale = MINIMUM_SCALE
                                offset = Offset.Zero
                                onZoomActiveChanged(false)
                                return@detectTapGestures
                            }

                            val nextScale = DOUBLE_TAP_SCALE
                            scale = nextScale

                            val centerOffset =
                                Offset(
                                    x = containerSize.width / 2f,
                                    y = containerSize.height / 2f,
                                )
                            val tapDelta = tapOffset - centerOffset
                            val imageSize = resolveImageSize()
                            val bounds =
                                calculateOffsetBounds(
                                    scale = nextScale,
                                    containerSize = containerSize,
                                    imageSize = imageSize,
                                )

                            offset =
                                Offset(
                                    x = (-tapDelta.x).coerceIn(minimumValue = -bounds.x, maximumValue = bounds.x),
                                    y = (-tapDelta.y).coerceIn(minimumValue = -bounds.y, maximumValue = bounds.y),
                                )
                            onZoomActiveChanged(true)
                        },
                    )
                }.transformable(
                    state = transformableState,
                    canPan = { scale > MINIMUM_SCALE + SCALE_ACTIVE_THRESHOLD },
                    lockRotationOnZoomPan = true,
                ),
    )
}

private fun calculateOffsetBounds(
    scale: Float,
    containerSize: IntSize,
    imageSize: IntSize,
): Offset {
    if (containerSize.width <= 0 || containerSize.height <= 0 || imageSize.width <= 0 || imageSize.height <= 0) {
        return Offset.Zero
    }

    val containerWidth = containerSize.width.toFloat()
    val containerHeight = containerSize.height.toFloat()
    val imageWidth = imageSize.width.toFloat()
    val imageHeight = imageSize.height.toFloat()

    val baseScale = min(containerWidth / imageWidth, containerHeight / imageHeight)
    val scaledWidth = imageWidth * baseScale * scale
    val scaledHeight = imageHeight * baseScale * scale

    val horizontalBound = max((scaledWidth - containerWidth) / 2f, 0f)
    val verticalBound = max((scaledHeight - containerHeight) / 2f, 0f)

    return Offset(horizontalBound, verticalBound)
}

private fun applyResistance(
    value: Float,
    maxOffset: Float,
): Float {
    val absoluteValue = abs(value)

    if (maxOffset <= 0f) {
        val resistedExtra =
            (absoluteValue * OUT_OF_BOUNDS_RESISTANCE) /
                (1f + absoluteValue / OUT_OF_BOUNDS_CURVE_DISTANCE)
        return sign(value) * resistedExtra
    }

    if (absoluteValue <= maxOffset) {
        return value
    }

    val extraDistance = absoluteValue - maxOffset
    val resistedExtra =
        (extraDistance * OUT_OF_BOUNDS_RESISTANCE) /
            (1f + extraDistance / OUT_OF_BOUNDS_CURVE_DISTANCE)
    return sign(value) * (maxOffset + resistedExtra)
}
