package com.bigong.oguri.core.platform

import android.content.Context
import android.content.Intent
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import java.util.concurrent.ConcurrentHashMap

private const val KAKAO_EXECUTION_PARAMETER_DEEPLINK = "deeplink"

private val cachedKakaoShareIntentByPayloadKey: MutableMap<String, Intent> = ConcurrentHashMap()
private val inFlightKakaoSharePayloadKeys: MutableSet<String> = ConcurrentHashMap.newKeySet()

actual fun preloadShareContent(payload: SharePayload) {
    val applicationContext = OguriPlatformContextHolder.applicationContext ?: return
    if (!ShareClient.instance.isKakaoTalkSharingAvailable(applicationContext)) {
        return
    }
    prepareKakaoShareIntent(
        context = applicationContext,
        payload = payload,
        onPrepared = {},
    )
}

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
    prepareKakaoShareIntent(
        context = context,
        payload = payload,
        onPrepared = { cachedIntent: Intent? ->
            if (cachedIntent == null) {
                shareFallbackMessage(
                    context = context,
                    text = buildFallbackText(payload = payload),
                )
                return@prepareKakaoShareIntent
            }
            context.startActivity(
                cachedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
    )
}

private fun prepareKakaoShareIntent(
    context: Context,
    payload: SharePayload,
    onPrepared: (Intent?) -> Unit,
) {
    val payloadKey = payload.toSharePayloadKey()
    val cachedIntent: Intent? = cachedKakaoShareIntentByPayloadKey[payloadKey]
    if (cachedIntent != null) {
        onPrepared(Intent(cachedIntent))
        return
    }

    if (!inFlightKakaoSharePayloadKeys.add(payloadKey)) {
        onPrepared(null)
        return
    }

    val executionParameters =
        mapOf(
            KAKAO_EXECUTION_PARAMETER_DEEPLINK to payload.deepLinkUrl,
        )
    val shareLink =
        Link(
            mobileWebUrl = payload.deepLinkUrl,
            webUrl = payload.deepLinkUrl,
            androidExecutionParams = executionParameters,
            iosExecutionParams = executionParameters,
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
        inFlightKakaoSharePayloadKeys.remove(payloadKey)
        if (throwable != null || sharingResult?.intent == null) {
            onPrepared(null)
            return@shareDefault
        }
        val preparedIntent = Intent(sharingResult.intent)
        cachedKakaoShareIntentByPayloadKey[payloadKey] = preparedIntent
        onPrepared(Intent(preparedIntent))
    }
}

private fun SharePayload.toSharePayloadKey(): String =
    listOf(
        title,
        description,
        imageUrl,
        deepLinkUrl,
        buttonTitle,
    ).joinToString(separator = "|")

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
