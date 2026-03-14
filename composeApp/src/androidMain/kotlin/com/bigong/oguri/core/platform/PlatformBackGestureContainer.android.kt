package com.bigong.oguri.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackGestureContainer(
    enabled: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}
