package com.bigong.oguri.core.platform

import platform.Foundation.NSNotificationCenter

private const val KAKAO_SHARE_REQUEST_NOTIFICATION_NAME = "OguriKakaoShareRequest"
private const val KAKAO_SHARE_TITLE_KEY = "title"
private const val KAKAO_SHARE_DESCRIPTION_KEY = "description"
private const val KAKAO_SHARE_IMAGE_URL_KEY = "imageUrl"
private const val KAKAO_SHARE_DEEP_LINK_URL_KEY = "deepLinkUrl"
private const val KAKAO_SHARE_BUTTON_TITLE_KEY = "buttonTitle"
private const val KAKAO_SHARE_FALLBACK_MESSAGE_KEY = "fallbackMessage"
private const val KAKAO_SHARE_FALLBACK_URL_KEY = "fallbackUrl"

actual fun shareContent(payload: SharePayload) {
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = KAKAO_SHARE_REQUEST_NOTIFICATION_NAME,
        `object` = null,
        userInfo =
            mapOf(
                KAKAO_SHARE_TITLE_KEY to payload.title,
                KAKAO_SHARE_DESCRIPTION_KEY to payload.description,
                KAKAO_SHARE_IMAGE_URL_KEY to payload.imageUrl,
                KAKAO_SHARE_DEEP_LINK_URL_KEY to payload.deepLinkUrl,
                KAKAO_SHARE_BUTTON_TITLE_KEY to payload.buttonTitle,
                KAKAO_SHARE_FALLBACK_MESSAGE_KEY to payload.fallbackMessage,
                KAKAO_SHARE_FALLBACK_URL_KEY to payload.fallbackUrl,
            ),
    )
}
