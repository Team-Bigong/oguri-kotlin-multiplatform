package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformWebView(
    url: String,
    goBackTrigger: Int,
    onCanGoBackChange: (Boolean) -> Unit,
    onCurrentUrlChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
)
