package com.bigong.oguri.feature.webdocument.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bigong.oguri.core.navigation.WebDocumentType
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.mypage_menu_privacy_policy
import oguri.composeapp.generated.resources.mypage_menu_suggest
import oguri.composeapp.generated.resources.mypage_menu_terms_of_service
import oguri.composeapp.generated.resources.uri_privacy_policy
import oguri.composeapp.generated.resources.uri_suggestion
import oguri.composeapp.generated.resources.uri_terms_of_service
import org.jetbrains.compose.resources.stringResource

@Composable
fun WebDocumentRoute(
    documentType: String,
    onBackClick: () -> Unit,
) {
    var canGoBackInWebView by rememberSaveable { mutableStateOf(false) }
    var goBackTrigger by rememberSaveable { mutableIntStateOf(0) }
    val webDocumentType =
        runCatching { WebDocumentType.valueOf(documentType) }
            .getOrDefault(WebDocumentType.TERMS_OF_SERVICE)

    val titleText =
        when (webDocumentType) {
            WebDocumentType.SUGGESTION -> stringResource(Res.string.mypage_menu_suggest)
            WebDocumentType.TERMS_OF_SERVICE -> stringResource(Res.string.mypage_menu_terms_of_service)
            WebDocumentType.PRIVACY_POLICY -> stringResource(Res.string.mypage_menu_privacy_policy)
        }
    val targetUrl =
        when (webDocumentType) {
            WebDocumentType.SUGGESTION -> stringResource(Res.string.uri_suggestion)
            WebDocumentType.TERMS_OF_SERVICE -> stringResource(Res.string.uri_terms_of_service)
            WebDocumentType.PRIVACY_POLICY -> stringResource(Res.string.uri_privacy_policy)
        }

    WebDocumentScreen(
        titleText = titleText,
        url = targetUrl,
        goBackTrigger = goBackTrigger,
        onCanGoBackChange = { canGoBackInWebView = it },
        onCurrentUrlChange = {},
        onBackClick = {
            if (canGoBackInWebView) {
                goBackTrigger += 1
            } else {
                onBackClick()
            }
        },
    )
}
