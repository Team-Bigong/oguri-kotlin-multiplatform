package com.bigong.oguri.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

private const val NAVIGATION_FADE_DURATION_MILLIS = 180

internal fun navigationEnter(): EnterTransition =
    fadeIn(tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))

internal fun navigationExit(): ExitTransition =
    fadeOut(tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
