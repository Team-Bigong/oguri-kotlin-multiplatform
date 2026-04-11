package com.bigong.oguri.core.util.extension

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral20

@Composable
fun Modifier.dismissKeyboardOnOutsideTouch(): Modifier {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        })
    }
}

fun Modifier.noRippleClickable(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    enabled: Boolean = true,
): Modifier =
    this.clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )

fun Modifier.skeletonShimmer(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Neutral20.copy(alpha = 0.9f),
    highlightColor: Color = Neutral10.copy(alpha = 0.55f),
    durationMillis: Int = 1100,
): Modifier =
    composed {
        var measuredSize by remember { mutableStateOf(IntSize.Zero) }
        val transition = rememberInfiniteTransition(label = "skeleton")
        val animatedOffsetX by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "skeleton_offset",
        )

        val brush =
            if (measuredSize.width == 0 || measuredSize.height == 0) {
                Brush.linearGradient(colors = listOf(baseColor, highlightColor, baseColor))
            } else {
                val startX = measuredSize.width * (animatedOffsetX - 1f)
                val endX = startX + (measuredSize.width * 1.5f)
                Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(x = startX, y = 0f),
                    end = Offset(x = endX, y = measuredSize.height.toFloat()),
                )
            }

        this
            .clip(shape = shape)
            .background(brush = brush, shape = shape)
            .onSizeChanged { size ->
                measuredSize = size
            }
    }

@Composable
fun Modifier.consumeVerticalDragForBottomSheetContent(): Modifier {
    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset =
                    if (source == NestedScrollSource.UserInput) {
                        Offset(x = 0f, y = available.y)
                    } else {
                        Offset.Zero
                    }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity = Velocity(x = 0f, y = available.y)
            }
        }

    return this.nestedScroll(nestedScrollConnection)
}
