package com.bigong.oguri.core.util.extension

import com.bigong.oguri.core.util.HapticType
import platform.UIKit.UISelectionFeedbackGenerator

actual fun HapticType.perform() {
    val generator = UISelectionFeedbackGenerator()
    generator.prepare()
    generator.selectionChanged()
}
