package com.bigong.oguri.feature.support.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel
import com.bigong.oguri.feature.support.ui.model.SupportInquiryUiState

@Composable
fun SupportInquiryTypeRoute(
    onOpenSupportEmail: (SupportEmailDraft) -> Unit,
) {
    val supportInquiryTypeViewModel: SupportInquiryTypeViewModel = rememberRouteViewModel {
        SupportInquiryTypeViewModel()
    }
    val supportInquiryUiState: SupportInquiryUiState by supportInquiryTypeViewModel.supportInquiryUiStateFlow.collectAsState()

    SupportInquiryTypeScreen(
        uiState = supportInquiryUiState,
        onSelectInquiryType = supportInquiryTypeViewModel::selectInquiryType,
        onBackToInquiryTypeSelection = supportInquiryTypeViewModel::resetSelection,
        onOpenSupportEmail = onOpenSupportEmail,
    )
}
