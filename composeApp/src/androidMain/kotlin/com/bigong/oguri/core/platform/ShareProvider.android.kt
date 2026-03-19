package com.bigong.oguri.core.platform

import android.content.Context
import android.content.Intent
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link

actual fun shareContent(payload: SharePayload) {
    if (payload.fallbackMessage.isBlank() || payload.fallbackUrl.isBlank()) {
        return
    }

    val applicationContext = OguriPlatformContextHolder.applicationContext ?: return
    if (ShareClient.instance.isKakaoTalkSharingAvailable(applicationContext)) {
        shareWithKakaoTalk(
            context = applicationContext,
            payload = payload,
        )
        return
    }

    shareFallbackMessage(
        context = applicationContext,
        text = buildFallbackText(payload = payload),
    )
}

private fun shareWithKakaoTalk(
    context: Context,
    payload: SharePayload,
) {
    val shareLink =
        Link(
            mobileWebUrl = payload.deepLinkUrl,
            webUrl = payload.deepLinkUrl,
        )
    val shareTemplate =
        FeedTemplate(
            content =
                Content(
                    title = payload.title,
                    description = payload.description,
                    imageUrl = payload.imageUrl,
                    link = shareLink,
                ),
            buttons =
                listOf(
                    Button(
                        title = payload.buttonTitle,
                        link = shareLink,
                    ),
                ),
        )
    ShareClient.instance.shareDefault(context, shareTemplate) { sharingResult, throwable ->
        if (throwable != null || sharingResult == null) {
            shareFallbackMessage(
                context = context,
                text = buildFallbackText(payload = payload),
            )
            return@shareDefault
        }
        context.startActivity(
            sharingResult.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun shareFallbackMessage(
    context: Context,
    text: String,
) {
    if (text.isBlank()) {
        return
    }

    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    val chooserIntent =
        Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(chooserIntent)
}

private fun buildFallbackText(payload: SharePayload): String = "${payload.fallbackMessage}\n\n${payload.fallbackUrl}"
