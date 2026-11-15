package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
internal expect fun TravelDialogSystemBars()

internal expect fun travelDialogProperties(): DialogProperties
