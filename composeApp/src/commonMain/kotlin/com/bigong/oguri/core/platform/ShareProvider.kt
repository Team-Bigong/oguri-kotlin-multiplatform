package com.bigong.oguri.core.platform

data class SharePayload(
    val title: String,
    val description: String,
    val imageUrl: String,
    val deepLinkUrl: String,
    val buttonTitle: String,
    val fallbackMessage: String,
    val fallbackUrl: String,
)

expect fun shareContent(payload: SharePayload)
