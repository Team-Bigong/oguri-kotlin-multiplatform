package com.bigong.oguri.core.platform

actual fun isNativeBottomNavigationEnabled(): Boolean = false

actual fun notifyNativeBottomNavigationState(
    isVisible: Boolean,
    selectedTabIndex: Int,
    selectedColorArgb: Long,
    unselectedColorArgb: Long,
    homeTabLabel: String,
    calendarTabLabel: String,
    myPageTabLabel: String,
) = Unit
