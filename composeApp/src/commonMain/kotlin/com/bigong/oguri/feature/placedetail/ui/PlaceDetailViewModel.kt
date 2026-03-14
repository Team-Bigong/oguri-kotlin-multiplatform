package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class PlaceDetailViewModel(
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase,
) : ViewModel() {
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var placeDetailUiState: PlaceDetailUiState by mutableStateOf(PlaceDetailUiState())
        private set

    fun loadPlaceDetail(placeId: Long) {
        viewModelScope.launch {
            placeDetailUiState = placeDetailUiState.copy(isLoading = true, isError = false)

            runCatching {
                withContext(Dispatchers.Default) {
                    getPlaceDetailUseCase(placeId = placeId)
                }
            }.onSuccess { placeDetail ->
                placeDetailUiState =
                    placeDetailUiState.copy(
                        isLoading = false,
                        isError = false,
                        placeDetail = placeDetail,
                        isSaved = placeDetail.isSaved,
                    )
            }.onFailure {
                placeDetailUiState =
                    placeDetailUiState.copy(
                        isLoading = false,
                        isError = true,
                        placeDetail = null,
                    )
            }
        }
    }

    fun toggleSaved() {
        placeDetailUiState = placeDetailUiState.copy(isSaved = !placeDetailUiState.isSaved)
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
