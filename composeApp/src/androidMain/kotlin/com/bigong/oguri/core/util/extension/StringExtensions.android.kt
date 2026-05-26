package com.bigong.oguri.core.util.extension

import java.util.Currency
import java.util.Locale

actual fun String.toCurrencyDisplayName(): String {
    val normalizedCurrencyCode = uppercase(Locale.ROOT)
    return runCatching {
        Currency
            .getInstance(normalizedCurrencyCode)
            .getDisplayName(Locale.getDefault())
            .toCurrencyUnitName(currencyCode = normalizedCurrencyCode)
    }.getOrElse {
        normalizedCurrencyCode
    }
}

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
