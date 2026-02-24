package com.bigong.oguri.feature.support.ui.component

import androidx.compose.runtime.Composable
import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.support.ui.model.SupportInquiryUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_loading
import oguri.composeapp.generated.resources.support_bug_report
import oguri.composeapp.generated.resources.support_email_back
import oguri.composeapp.generated.resources.support_email_body_label
import oguri.composeapp.generated.resources.support_email_open_app
import oguri.composeapp.generated.resources.support_email_subject_label
import oguri.composeapp.generated.resources.support_feature_suggestion
import oguri.composeapp.generated.resources.support_other_inquiry
import oguri.composeapp.generated.resources.support_step_mail_title
import oguri.composeapp.generated.resources.support_step_select_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InquiryTypeSelectionSection(
    onSelectInquiryType: (SupportInquiryType) -> Unit,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.support_step_select_title))
        PlaceholderActionButton(
            labelText = stringResource(Res.string.support_feature_suggestion),
            onClick = { onSelectInquiryType(SupportInquiryType.FEATURE_SUGGESTION) },
        )
        PlaceholderActionButton(
            labelText = stringResource(Res.string.support_bug_report),
            onClick = { onSelectInquiryType(SupportInquiryType.BUG_REPORT) },
            emphasized = false,
        )
        PlaceholderActionButton(
            labelText = stringResource(Res.string.support_other_inquiry),
            onClick = { onSelectInquiryType(SupportInquiryType.OTHER_INQUIRY) },
            emphasized = false,
        )
    }
}

@Composable
internal fun EmailPreviewSection(
    uiState: SupportInquiryUiState,
    onBackToInquiryTypeSelection: () -> Unit,
    onOpenSupportEmail: (SupportEmailDraft) -> Unit,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.support_step_mail_title))
        if (uiState.isLoadingDraft || uiState.supportEmailDraft == null) {
            PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
        } else {
            PlaceholderSectionTitle(text = stringResource(Res.string.support_email_subject_label))
            PlaceholderInfoCard(lines = listOf(uiState.supportEmailDraft.subjectText))
            PlaceholderSectionTitle(text = stringResource(Res.string.support_email_body_label))
            PlaceholderInfoCard(lines = uiState.supportEmailDraft.bodyText.split("\n"))
            PlaceholderActionButton(
                labelText = stringResource(Res.string.support_email_open_app),
                onClick = { onOpenSupportEmail(uiState.supportEmailDraft) },
            )
            PlaceholderActionButton(
                labelText = stringResource(Res.string.support_email_back),
                onClick = onBackToInquiryTypeSelection,
                emphasized = false,
            )
        }
    }
}
