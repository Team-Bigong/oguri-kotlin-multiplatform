package com.bigong.oguri.feature.support.model

import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType
import com.bigong.oguri.getPlatform
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.support_email_body_template
import oguri.composeapp.generated.resources.support_inquiry_subject_bug
import oguri.composeapp.generated.resources.support_inquiry_subject_feature
import oguri.composeapp.generated.resources.support_inquiry_subject_other
import oguri.composeapp.generated.resources.support_inquiry_type_bug
import oguri.composeapp.generated.resources.support_inquiry_type_feature
import oguri.composeapp.generated.resources.support_inquiry_type_other
import org.jetbrains.compose.resources.getString

object SupportEmailDraftFactory {
    private const val APP_VERSION_TEXT: String = "1.0.0"
    private const val DUMMY_DEVICE_MODEL_TEXT: String = "Dummy Device"

    suspend fun create(supportInquiryType: SupportInquiryType): SupportEmailDraft {
        val inquiryTypeLabelText: String
        val subjectText: String
        when (supportInquiryType) {
            SupportInquiryType.FEATURE_SUGGESTION -> {
                inquiryTypeLabelText = getString(Res.string.support_inquiry_type_feature)
                subjectText = getString(Res.string.support_inquiry_subject_feature)
            }

            SupportInquiryType.BUG_REPORT -> {
                inquiryTypeLabelText = getString(Res.string.support_inquiry_type_bug)
                subjectText = getString(Res.string.support_inquiry_subject_bug)
            }

            SupportInquiryType.OTHER_INQUIRY -> {
                inquiryTypeLabelText = getString(Res.string.support_inquiry_type_other)
                subjectText = getString(Res.string.support_inquiry_subject_other)
            }
        }

        val bodyText: String =
            getString(
                Res.string.support_email_body_template,
                inquiryTypeLabelText,
                APP_VERSION_TEXT,
                getPlatform().name,
                DUMMY_DEVICE_MODEL_TEXT,
            )

        return SupportEmailDraft(
            subjectText = subjectText,
            bodyText = bodyText,
        )
    }
}
