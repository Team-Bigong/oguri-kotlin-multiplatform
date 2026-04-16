package com.bigong.oguri.core.platform

import platform.Foundation.NSNotificationCenter
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithInt
import platform.UIKit.UIDevice

private const val NATIVE_BOTTOM_NAVIGATION_STATE_CHANGED_NOTIFICATION_NAME =
    "OguriNativeBottomNavigationStateChanged"
private const val KEY_IS_VISIBLE = "isVisible"
private const val KEY_SELECTED_TAB_INDEX = "selectedTabIndex"
private const val KEY_SELECTED_COLOR_ARGB = "selectedColorArgb"
private const val KEY_UNSELECTED_COLOR_ARGB = "unselectedColorArgb"
private const val KEY_HOME_TAB_LABEL = "homeTabLabel"
private const val KEY_CALENDAR_TAB_LABEL = "calendarTabLabel"
private const val KEY_MYPAGE_TAB_LABEL = "myPageTabLabel"
private const val NATIVE_BOTTOM_NAVIGATION_MINIMUM_IOS_MAJOR_VERSION = 26L

actual fun isNativeBottomNavigationEnabled(): Boolean {
    val systemVersionText = UIDevice.currentDevice.systemVersion
    val majorVersion = systemVersionText.substringBefore(".").toLongOrNull() ?: return false
    return majorVersion >= NATIVE_BOTTOM_NAVIGATION_MINIMUM_IOS_MAJOR_VERSION
}

actual fun notifyNativeBottomNavigationState(
    isVisible: Boolean,
    selectedTabIndex: Int,
    selectedColorArgb: Long,
    unselectedColorArgb: Long,
    homeTabLabel: String,
    calendarTabLabel: String,
    myPageTabLabel: String,
) {
    val userInfo: Map<Any?, Any?> =
        mapOf<Any?, Any?>(
            KEY_IS_VISIBLE to platform.Foundation.NSNumber.numberWithBool(isVisible),
            KEY_SELECTED_TAB_INDEX to platform.Foundation.NSNumber.numberWithInt(selectedTabIndex),
            KEY_SELECTED_COLOR_ARGB to selectedColorArgb.toString(),
            KEY_UNSELECTED_COLOR_ARGB to unselectedColorArgb.toString(),
            KEY_HOME_TAB_LABEL to homeTabLabel,
            KEY_CALENDAR_TAB_LABEL to calendarTabLabel,
            KEY_MYPAGE_TAB_LABEL to myPageTabLabel,
        )
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = NATIVE_BOTTOM_NAVIGATION_STATE_CHANGED_NOTIFICATION_NAME,
        `object` = null,
        userInfo = userInfo,
    )
}
