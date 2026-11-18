package com.bigong.oguri.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val NAVIGATION_FADE_DURATION_MILLIS = 180
private const val NAVIGATION_SLIDE_DURATION_MILLIS = 240

internal fun navigationEnter(fromRight: Boolean, parallax: Boolean = false): EnterTransition =
    slideInHorizontally(tween(NAVIGATION_SLIDE_DURATION_MILLIS)) { width ->
        navigationOffset(width, fromRight, parallax)
    } + fadeIn(tween(NAVIGATION_FADE_DURATION_MILLIS))

internal fun navigationExit(toRight: Boolean, parallax: Boolean = false): ExitTransition =
    slideOutHorizontally(tween(NAVIGATION_SLIDE_DURATION_MILLIS)) { width ->
        navigationOffset(width, toRight, parallax)
    } + fadeOut(tween(NAVIGATION_FADE_DURATION_MILLIS))

private fun navigationOffset(width: Int, right: Boolean, parallax: Boolean): Int {
    val distance = if (parallax) width / 3 else width
    return if (right) distance else -distance
}
