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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.platform.isFloatingBottomNavigationEnabled

private val FLOATING_BOTTOM_NAVIGATION_SNACKBAR_RESERVED_PADDING = 142.dp

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

@Composable
fun Modifier.oguriElevation(
    elevation: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = Color.Black,
): Modifier {
    if (elevation <= 0.dp) {
        return this
    }

    val shadows =
        if (OguriTheme.isDarkTheme) {
            darkModeShadows(elevation = elevation, color = color)
        } else {
            lightModeShadows(elevation = elevation, color = color)
        }

    return oguriElevation(shadows = shadows, shape = shape)
}

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

fun Modifier.floatingNavigationBarsPadding(
    hasFloatingBottomNavigation: Boolean,
    baseBottomPadding: Dp = 0.dp,
): Modifier {
    val additionalBottomPadding =
        calculateFloatingBottomNavigationAdditionalBottomPadding(
            hasFloatingBottomNavigation = hasFloatingBottomNavigation,
            baseBottomPadding = baseBottomPadding,
        )
    if (additionalBottomPadding <= 0.dp) {
        return this
    }
    return this.padding(bottom = additionalBottomPadding)
}

fun calculateFloatingBottomNavigationAdditionalBottomPadding(
    hasFloatingBottomNavigation: Boolean,
    baseBottomPadding: Dp = 0.dp,
): Dp {
    val shouldApplyFloatingBottomPadding = hasFloatingBottomNavigation && isFloatingBottomNavigationEnabled()
    if (!shouldApplyFloatingBottomPadding) {
        return 0.dp
    }
    return (FLOATING_BOTTOM_NAVIGATION_SNACKBAR_RESERVED_PADDING - baseBottomPadding).coerceAtLeast(0.dp)
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

private fun Modifier.oguriElevation(
    shadows: List<Shadow>,
    shape: Shape,
): Modifier {
    var modifier = this
    shadows.forEach { shadow ->
        modifier = modifier.dropShadow(shape = shape, shadow = shadow)
    }
    return modifier
}

private fun lightModeShadows(
    elevation: Dp,
    color: Color,
): List<Shadow> =
    listOf(
        oguriShadow(
            radius = elevation * 1.2f,
            offsetY = elevation * 0.4f,
            alpha = 0.09f,
            color = color,
        ),
        oguriShadow(
            radius = elevation * 7f,
            offsetY = elevation * 1.2f,
            alpha = 0.045f,
            color = color,
        ),
    )

private fun darkModeShadows(
    elevation: Dp,
    color: Color,
): List<Shadow> =
    listOf(
        oguriShadow(
            radius = elevation * 1.2f,
            offsetY = elevation * 0.4f,
            alpha = 0.12f,
            color = color,
        ),
    )

private fun oguriShadow(
    radius: Dp,
    offsetY: Dp = 0.dp,
    alpha: Float,
    color: Color,
): Shadow =
    Shadow(
        radius = radius,
        color = color,
        offset = DpOffset(x = 0.dp, y = offsetY),
        alpha = alpha,
    )
