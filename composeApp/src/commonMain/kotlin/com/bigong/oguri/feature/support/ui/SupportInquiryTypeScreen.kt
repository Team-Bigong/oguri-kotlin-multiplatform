package com.bigong.oguri.feature.support.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.support.model.SupportEmailDraftFactory
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
import oguri.composeapp.generated.resources.support_subtitle
import oguri.composeapp.generated.resources.support_title
import org.jetbrains.compose.resources.stringResource

private sealed interface SupportScreenStep {
    data object SelectType : SupportScreenStep
    data class PreviewEmail(val supportInquiryType: SupportInquiryType) : SupportScreenStep
}

@Composable
fun SupportInquiryTypeRoute(
    onOpenSupportEmail: (SupportEmailDraft) -> Unit,
) {
    var supportScreenStep: SupportScreenStep by remember {
        mutableStateOf<SupportScreenStep>(SupportScreenStep.SelectType)
    }

    val currentDraftRequestType: SupportInquiryType? = (supportScreenStep as? SupportScreenStep.PreviewEmail)?.supportInquiryType
    val supportEmailDraftState: SupportEmailDraft? by produceState<SupportEmailDraft?>(
        initialValue = null,
        key1 = currentDraftRequestType,
    ) {
        value = currentDraftRequestType?.let { supportInquiryType: SupportInquiryType ->
            SupportEmailDraftFactory.create(supportInquiryType = supportInquiryType)
        }
    }

    SupportInquiryTypeScreen(
        supportScreenStep = supportScreenStep,
        supportEmailDraft = supportEmailDraftState,
        onSelectInquiryType = { supportInquiryType: SupportInquiryType ->
            supportScreenStep = SupportScreenStep.PreviewEmail(supportInquiryType = supportInquiryType)
        },
        onBackToInquiryTypeSelection = {
            supportScreenStep = SupportScreenStep.SelectType
        },
        onOpenSupportEmail = onOpenSupportEmail,
    )
}

@Composable
private fun SupportInquiryTypeScreen(
    supportScreenStep: SupportScreenStep,
    supportEmailDraft: SupportEmailDraft?,
    onSelectInquiryType: (SupportInquiryType) -> Unit,
    onBackToInquiryTypeSelection: () -> Unit,
    onOpenSupportEmail: (SupportEmailDraft) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        PlaceholderHeader(
            screenTitleText = stringResource(Res.string.support_title),
            screenSubtitleText = stringResource(Res.string.support_subtitle),
        )

        when (supportScreenStep) {
            SupportScreenStep.SelectType -> {
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
            is SupportScreenStep.PreviewEmail -> {
                PlaceholderSectionCard {
                    PlaceholderSectionTitle(text = stringResource(Res.string.support_step_mail_title))
                    if (supportEmailDraft == null) {
                        PlaceholderInfoCard(lines = listOf(stringResource(Res.string.common_loading)))
                    } else {
                        PlaceholderSectionTitle(text = stringResource(Res.string.support_email_subject_label))
                        PlaceholderInfoCard(lines = listOf(supportEmailDraft.subjectText))
                        PlaceholderSectionTitle(text = stringResource(Res.string.support_email_body_label))
                        PlaceholderInfoCard(lines = supportEmailDraft.bodyText.split("\n"))
                        PlaceholderActionButton(
                            labelText = stringResource(Res.string.support_email_open_app),
                            onClick = { onOpenSupportEmail(supportEmailDraft) },
                        )
                        PlaceholderActionButton(
                            labelText = stringResource(Res.string.support_email_back),
                            onClick = onBackToInquiryTypeSelection,
                            emphasized = false,
                        )
                    }
                }
            }
        }
    }
}
