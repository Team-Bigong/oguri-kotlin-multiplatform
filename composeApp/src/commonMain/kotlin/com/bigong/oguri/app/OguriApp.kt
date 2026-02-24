package com.bigong.oguri.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bigong.oguri.core.navigation.NavDisplay

@Composable
fun OguriApp(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onExitApp: () -> Unit = {},
) {
    NavDisplay(
        snackbarHostState = snackbarHostState,
        onExitApp = onExitApp,
    )
}
