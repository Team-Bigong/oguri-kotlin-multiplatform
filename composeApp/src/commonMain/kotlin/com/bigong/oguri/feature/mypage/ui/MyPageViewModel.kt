package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.usecase.DeleteMyPageSavedPlaceUseCase
import com.bigong.oguri.domain.usecase.DeleteMyPageSelectedPeriodUseCase
import com.bigong.oguri.domain.usecase.GetMyPageInfoUseCase
import com.bigong.oguri.domain.usecase.LogoutUseCase
import com.bigong.oguri.domain.usecase.UpdateMyPageLeaveDaysUseCase
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var myPageUiState: MyPageUiState by mutableStateOf(MyPageUiState())
        private set

    init {
        loadMyPageInfo()
    }

    fun loadMyPageInfo() {
        viewModelScope.launch {
            myPageUiState = myPageUiState.copy(isLoading = true, isError = false)

            runCatching {
                withContext(Dispatchers.Default) {
                    getMyPageInfoUseCase()
                }
            }.onSuccess { myPageInfo ->
                myPageUiState =
                    myPageUiState.copy(
                        isLoading = false,
                        isError = false,
                        myPageInfo = myPageInfo,
                    )
            }.onFailure {
                myPageUiState =
                    myPageUiState.copy(
                        isLoading = false,
                        isError = true,
                        myPageInfo = null,
                    )
            }
        }
    }

    fun showEditLeaveDaysBottomSheet() {
        myPageUiState = myPageUiState.copy(isEditLeaveDaysBottomSheetVisible = true)
    }

    fun hideEditLeaveDaysBottomSheet() {
        myPageUiState = myPageUiState.copy(isEditLeaveDaysBottomSheetVisible = false)
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
                myPageUiState =
                    myPageUiState.copy(
                        myPageInfo = myPageInfo,
                        isEditLeaveDaysBottomSheetVisible = false,
                    )
            }
        }
    }

    fun showDeleteScheduleDialog(periodId: Long) {
        myPageUiState = myPageUiState.copy(pendingDeleteScheduleId = periodId)
    }

    fun dismissDeleteScheduleDialog() {
        myPageUiState = myPageUiState.copy(pendingDeleteScheduleId = null)
    }

    fun confirmDeleteSchedule() {
        val pendingDeleteScheduleId = myPageUiState.pendingDeleteScheduleId ?: return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    deleteMyPageSelectedPeriodUseCase(periodId = pendingDeleteScheduleId)
                }
            }.onSuccess { myPageInfo ->
                myPageUiState =
                    myPageUiState.copy(
                        myPageInfo = myPageInfo,
                        pendingDeleteScheduleId = null,
                    )
            }
        }
    }

    fun showDeleteSavedPlaceDialog(placeId: Long) {
        myPageUiState = myPageUiState.copy(pendingDeletePlaceId = placeId)
    }

    fun dismissDeleteSavedPlaceDialog() {
        myPageUiState = myPageUiState.copy(pendingDeletePlaceId = null)
    }

    fun confirmDeleteSavedPlace() {
        val pendingDeletePlaceId = myPageUiState.pendingDeletePlaceId ?: return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    deleteMyPageSavedPlaceUseCase(placeId = pendingDeletePlaceId)
                }
            }.onSuccess { myPageInfo ->
                myPageUiState =
                    myPageUiState.copy(
                        myPageInfo = myPageInfo,
                        pendingDeletePlaceId = null,
                    )
            }
        }
    }

    fun showLogoutDialog() {
        myPageUiState = myPageUiState.copy(isLogoutDialogVisible = true)
    }

    fun hideLogoutDialog() {
        myPageUiState = myPageUiState.copy(isLogoutDialogVisible = false)
    }

    fun confirmLogout() {
        logoutUseCase()
        myPageUiState = myPageUiState.copy(isLogoutDialogVisible = false)
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
