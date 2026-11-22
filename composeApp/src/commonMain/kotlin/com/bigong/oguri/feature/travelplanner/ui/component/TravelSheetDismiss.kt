package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberTravelSheetDismiss(sheetState: SheetState, onDismiss: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentDismiss by rememberUpdatedState(onDismiss)
    return remember(sheetState, scope) {
        var hideJob: Job? = null
        action@{
            if (hideJob?.isActive == true) return@action
            hideJob = scope.launch {
                sheetState.hide()
                if (!sheetState.isVisible) currentDismiss()
            }
        }
    }
}
