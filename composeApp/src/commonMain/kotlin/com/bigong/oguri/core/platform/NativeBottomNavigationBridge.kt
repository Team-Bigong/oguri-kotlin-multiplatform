package com.bigong.oguri.core.platform

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

expect fun isNativeBottomNavigationEnabled(): Boolean

expect fun notifyNativeBottomNavigationState(
    isVisible: Boolean,
    selectedTabIndex: Int,
    selectedColorArgb: Long,
    unselectedColorArgb: Long,
    homeTabLabel: String,
    calendarTabLabel: String,
    myPageTabLabel: String,
)

private val nativeBottomNavigationSelectionEvents: MutableSharedFlow<Int> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

@Suppress("unused")
fun emitNativeBottomNavigationSelection(tabIndex: Int) {
    nativeBottomNavigationSelectionEvents.tryEmit(tabIndex)
}

fun nativeBottomNavigationSelectionFlow(): Flow<Int> = nativeBottomNavigationSelectionEvents.asSharedFlow()
