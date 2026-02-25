package com.bigong.oguri.feature.support.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.support.ui.component.EmailPreviewSection
import com.bigong.oguri.feature.support.ui.component.InquiryTypeSelectionSection
import com.bigong.oguri.feature.support.ui.model.SupportInquiryUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.support_subtitle
import oguri.composeapp.generated.resources.support_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupportInquiryTypeScreen(
    uiState: SupportInquiryUiState,
    onSelectInquiryType: (SupportInquiryType) -> Unit,
    onBackToInquiryTypeSelection: () -> Unit,
    onOpenSupportEmail: (SupportEmailDraft) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        PlaceholderHeader(
            screenTitleText = stringResource(Res.string.support_title),
            screenSubtitleText = stringResource(Res.string.support_subtitle),
        )

        if (uiState.supportInquiryType == null) {
            InquiryTypeSelectionSection(onSelectInquiryType = onSelectInquiryType)
        } else {
            EmailPreviewSection(
                uiState = uiState,
                onBackToInquiryTypeSelection = onBackToInquiryTypeSelection,
                onOpenSupportEmail = onOpenSupportEmail,
            )
        }
    }
}
