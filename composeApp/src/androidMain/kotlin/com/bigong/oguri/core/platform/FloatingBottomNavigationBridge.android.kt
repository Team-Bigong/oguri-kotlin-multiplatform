package com.bigong.oguri.core.platform

actual fun isFloatingBottomNavigationEnabled(): Boolean = false

actual fun notifyFloatingBottomNavigationState(
    isVisible: Boolean,
    selectedTabIndex: Int,
    selectedColorArgb: Long,
    unselectedColorArgb: Long,
    backgroundColorArgb: Long,
    homeTabLabel: String,
    calendarTabLabel: String,
    myPageTabLabel: String,
) = Unit
