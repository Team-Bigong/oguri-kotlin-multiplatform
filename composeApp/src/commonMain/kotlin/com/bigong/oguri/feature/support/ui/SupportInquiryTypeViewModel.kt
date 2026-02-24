package com.bigong.oguri.feature.support.ui

import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.data.model.SupportInquiryType
import com.bigong.oguri.feature.common.ui.RouteViewModel
import com.bigong.oguri.feature.support.model.SupportEmailDraftFactory
import com.bigong.oguri.feature.support.ui.model.SupportInquiryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupportInquiryTypeViewModel : RouteViewModel() {
    private val mutableSupportInquiryUiStateFlow: MutableStateFlow<SupportInquiryUiState> = MutableStateFlow(SupportInquiryUiState())
    val supportInquiryUiStateFlow: StateFlow<SupportInquiryUiState> = mutableSupportInquiryUiStateFlow.asStateFlow()

    fun selectInquiryType(supportInquiryType: SupportInquiryType) {
        mutableSupportInquiryUiStateFlow.update { previousUiState: SupportInquiryUiState ->
            previousUiState.copy(
                supportInquiryType = supportInquiryType,
                supportEmailDraft = null,
                isLoadingDraft = true,
            )
        }
        routeViewModelScope.launch {
            val supportEmailDraft: SupportEmailDraft = SupportEmailDraftFactory.create(supportInquiryType = supportInquiryType)
            mutableSupportInquiryUiStateFlow.update { previousUiState: SupportInquiryUiState ->
                previousUiState.copy(supportEmailDraft = supportEmailDraft, isLoadingDraft = false)
            }
        }
    }

    fun resetSelection() {
        mutableSupportInquiryUiStateFlow.value = SupportInquiryUiState()
    }
}
