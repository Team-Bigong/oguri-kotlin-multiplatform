package com.bigong.oguri.core.util.extension

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode

actual fun String.toCurrencyDisplayName(): String {
    val normalizedCurrencyCode = uppercase()
    return NSLocale(localeIdentifier = KOREAN_LOCALE_IDENTIFIER)
        .displayNameForKey(
            key = NSLocaleCurrencyCode,
            value = normalizedCurrencyCode,
        )?.toCurrencyUnitName(currencyCode = normalizedCurrencyCode)
        ?: normalizedCurrencyCode
}

private const val KOREAN_LOCALE_IDENTIFIER = "ko_KR"

private fun String.toCurrencyUnitName(currencyCode: String): String {
    val displayName = trim()
    if (displayName.isEmpty()) {
        return currencyCode
    }

    return displayName
        .split(Regex(pattern = "\\s+"))
        .lastOrNull()
        ?: currencyCode
}
