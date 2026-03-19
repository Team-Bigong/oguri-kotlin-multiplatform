package com.bigong.oguri.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
actual fun shareText(text: String) {
    if (text.isBlank()) {
        return
    }

    val rootViewController =
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return
    val topViewController = rootViewController.findTopMostViewController()
    val activityViewController =
        UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

    topViewController.presentViewController(
        viewControllerToPresent = activityViewController,
        animated = true,
        completion = null,
    )
}

private fun UIViewController.findTopMostViewController(): UIViewController {
    var currentViewController: UIViewController = this
    while (currentViewController.presentedViewController != null) {
        currentViewController = currentViewController.presentedViewController!!
    }
    return currentViewController
}
