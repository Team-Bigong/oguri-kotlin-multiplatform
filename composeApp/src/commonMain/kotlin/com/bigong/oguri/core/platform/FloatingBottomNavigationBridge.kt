package com.bigong.oguri.core.platform

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

expect fun isFloatingBottomNavigationEnabled(): Boolean

expect fun notifyFloatingBottomNavigationState(
    isVisible: Boolean,
    selectedTabIndex: Int,
    selectedColorArgb: Long,
    unselectedColorArgb: Long,
    backgroundColorArgb: Long,
    homeTabLabel: String,
    calendarTabLabel: String,
    myPageTabLabel: String,
)

private val floatingBottomNavigationSelectionEvents: MutableSharedFlow<Int> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

@Suppress("unused")
fun emitFloatingBottomNavigationSelection(tabIndex: Int) {
    floatingBottomNavigationSelectionEvents.tryEmit(tabIndex)
}

fun floatingBottomNavigationSelectionFlow(): Flow<Int> = floatingBottomNavigationSelectionEvents.asSharedFlow()
