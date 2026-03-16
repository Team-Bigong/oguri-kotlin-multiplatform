package com.bigong.oguri.feature.mypage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.DeleteMyPageSavedPlaceUseCase
import com.bigong.oguri.domain.usecase.DeleteMyPageSelectedPeriodUseCase
import com.bigong.oguri.domain.usecase.GetMyPageInfoUseCase
import com.bigong.oguri.domain.usecase.LogoutUseCase
import com.bigong.oguri.domain.usecase.UpdateMyPageLeaveDaysUseCase
import com.bigong.oguri.feature.mypage.ui.model.MyPageSideEffect
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class MyPageViewModel(
    private val getMyPageInfoUseCase: GetMyPageInfoUseCase,
    private val updateMyPageLeaveDaysUseCase: UpdateMyPageLeaveDaysUseCase,
    private val deleteMyPageSelectedPeriodUseCase: DeleteMyPageSelectedPeriodUseCase,
    private val deleteMyPageSavedPlaceUseCase: DeleteMyPageSavedPlaceUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<MyPageUiState> = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()
    private val _sideEffect: MutableSharedFlow<MyPageSideEffect> = MutableSharedFlow(extraBufferCapacity = 1)
    val sideEffect: SharedFlow<MyPageSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadMyPageInfo()
    }

    fun loadMyPageInfo() {
        viewModelScope.launch {
            _uiState.update { currentUiState: MyPageUiState ->
                currentUiState.copy(isLoading = true, isError = false)
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    getMyPageInfoUseCase()
                }
            }.onSuccess { myPageInfo ->
                _uiState.update { currentUiState: MyPageUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = false,
                        myPageInfo = myPageInfo,
                    )
                }
            }.onFailure {
                _uiState.update { currentUiState: MyPageUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = true,
                        myPageInfo = null,
                    )
                }
            }
        }
    }

    fun showEditLeaveDaysBottomSheet() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(isEditLeaveDaysBottomSheetVisible = true)
        }
    }

    fun hideEditLeaveDaysBottomSheet() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(isEditLeaveDaysBottomSheetVisible = false)
        }
    }

    fun updateLeaveDays(
        remainingLeaveDays: Int,
        preferredLeaveDays: Int,
    ) {
        if (remainingLeaveDays <= 0 || preferredLeaveDays <= 0) {
            return
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    updateMyPageLeaveDaysUseCase(
                        remainingLeaveDays = remainingLeaveDays,
                        preferredLeaveDays = preferredLeaveDays,
                    )
                }
            }.onSuccess { myPageInfo ->
                _uiState.update { currentUiState: MyPageUiState ->
                    currentUiState.copy(
                        myPageInfo = myPageInfo,
                        isEditLeaveDaysBottomSheetVisible = false,
                    )
                }
                _sideEffect.tryEmit(MyPageSideEffect.LeaveDaysUpdated)
            }
        }
    }

    fun showDeleteScheduleDialog(periodId: Long) {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(pendingDeleteScheduleId = periodId)
        }
    }

    fun dismissDeleteScheduleDialog() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(pendingDeleteScheduleId = null)
        }
    }

    fun confirmDeleteSchedule() {
        val pendingDeleteScheduleId = uiState.value.pendingDeleteScheduleId ?: return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    deleteMyPageSelectedPeriodUseCase(periodId = pendingDeleteScheduleId)
                }
            }.onSuccess { myPageInfo ->
                _uiState.update { currentUiState: MyPageUiState ->
                    currentUiState.copy(
                        myPageInfo = myPageInfo,
                        pendingDeleteScheduleId = null,
                    )
                }
                _sideEffect.tryEmit(MyPageSideEffect.SelectedPeriodDeleted)
            }
        }
    }

    fun showDeleteSavedPlaceDialog(placeId: Long) {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(pendingDeletePlaceId = placeId)
        }
    }

    fun dismissDeleteSavedPlaceDialog() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(pendingDeletePlaceId = null)
        }
    }

    fun confirmDeleteSavedPlace() {
        val pendingDeletePlaceId = uiState.value.pendingDeletePlaceId ?: return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    deleteMyPageSavedPlaceUseCase(placeId = pendingDeletePlaceId)
                }
            }.onSuccess { myPageInfo ->
                _uiState.update { currentUiState: MyPageUiState ->
                    currentUiState.copy(
                        myPageInfo = myPageInfo,
                        pendingDeletePlaceId = null,
                    )
                }
                _sideEffect.tryEmit(MyPageSideEffect.SavedPlaceDeleted)
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(isLogoutDialogVisible = true)
        }
    }

    fun hideLogoutDialog() {
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(isLogoutDialogVisible = false)
        }
    }

    fun confirmLogout() {
        logoutUseCase()
        _uiState.update { currentUiState: MyPageUiState ->
            currentUiState.copy(isLogoutDialogVisible = false)
        }
        _sideEffect.tryEmit(MyPageSideEffect.LoggedOut)
    }
}
