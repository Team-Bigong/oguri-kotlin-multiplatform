package com.bigong.oguri.core.platform

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
@Composable
actual fun PlatformBackGestureContainer(
    enabled: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val edgeWidthPx: Float = remember(density) { with(density) { 24.dp.toPx() } }
    val minimumSwipeBackDistancePx: Float = remember(density) { with(density) { 72.dp.toPx() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(enabled, edgeWidthPx, minimumSwipeBackDistancePx) {
                if (!enabled) {
                    return@pointerInput
                }

                var isSwipeCandidate: Boolean = false
                var totalHorizontalDrag: Float = 0f

                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isSwipeCandidate = offset.x <= edgeWidthPx
                        totalHorizontalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!isSwipeCandidate) {
                            return@detectHorizontalDragGestures
                        }
                        totalHorizontalDrag += dragAmount
                    },
                    onDragCancel = {
                        isSwipeCandidate = false
                        totalHorizontalDrag = 0f
                    },
                    onDragEnd = {
                        val isSwipeBack: Boolean =
                            isSwipeCandidate &&
                                totalHorizontalDrag > minimumSwipeBackDistancePx
                        if (isSwipeBack) {
                            onBack()
                        }
                        isSwipeCandidate = false
                        totalHorizontalDrag = 0f
                    },
                )
            },
    ) {
        content()
    }
}
