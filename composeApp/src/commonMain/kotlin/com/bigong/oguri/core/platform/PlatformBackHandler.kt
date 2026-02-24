package com.bigong.oguri.core.platform

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
