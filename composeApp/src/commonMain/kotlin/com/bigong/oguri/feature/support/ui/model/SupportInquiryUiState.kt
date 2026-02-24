package com.bigong.oguri.feature.support.ui.model

import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType

data class SupportInquiryUiState(
    val supportInquiryType: SupportInquiryType? = null,
    val supportEmailDraft: SupportEmailDraft? = null,
    val isLoadingDraft: Boolean = false,
)
