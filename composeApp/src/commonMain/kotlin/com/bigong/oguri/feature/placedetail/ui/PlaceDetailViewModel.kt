package com.bigong.oguri.feature.placedetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.core.util.extension.isUnauthorized
import com.bigong.oguri.domain.model.PlaceDetail
import com.bigong.oguri.domain.usecase.DeleteSavedDestinationUseCase
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase
import com.bigong.oguri.domain.usecase.SaveDestinationUseCase
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailSideEffect
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class PlaceDetailViewModel(
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    private val saveDestinationUseCase: SaveDestinationUseCase,
    private val deleteSavedDestinationUseCase: DeleteSavedDestinationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<PlaceDetailSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()
    private var currentRequestKey: PlaceDetailRequestKey? = null

    fun loadPlaceDetail(
        placeId: Long,
        startDate: String?,
        endDate: String?,
    ) {
        val requestKey = PlaceDetailRequestKey(placeId = placeId, startDate = startDate, endDate = endDate)
        currentRequestKey = requestKey
        val cachedState = PLACE_DETAIL_CACHE[requestKey]

        viewModelScope.launch {
            if (cachedState == null) {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = true,
                        isError = false,
                        placeDetail = null,
                        isSaved = false,
                    )
                }
            } else {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = false,
                        placeDetail = cachedState.placeDetail,
                        isSaved = cachedState.isSaved,
                    )
                }
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    getPlaceDetailUseCase(
                        placeId = placeId,
                        startDate = startDate,
                        endDate = endDate,
                    )
                }
            }.onSuccess { placeDetail ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        isError = false,
                        placeDetail = placeDetail,
                        isSaved = placeDetail.isSaved,
                    )
                }
                PLACE_DETAIL_CACHE[requestKey] =
                    CachedPlaceDetailState(
                        placeDetail = placeDetail,
                        isSaved = placeDetail.isSaved,
                    )
            }.onFailure {
                if (cachedState == null) {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            isLoading = false,
                            isError = true,
                            placeDetail = null,
                        )
                    }
                } else {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            isLoading = false,
                            isError = false,
                            placeDetail = cachedState.placeDetail,
                            isSaved = cachedState.isSaved,
                        )
                    }
                }
            }
        }
    }

    fun toggleSaved() {
        val currentPlaceDetail = uiState.value.placeDetail ?: return
        val previousSavedState = uiState.value.isSaved
        val nextSavedState = !previousSavedState
        _uiState.update { currentUiState ->
            currentUiState.copy(isSaved = nextSavedState)
        }
        val requestKey = currentRequestKey
        if (requestKey != null) {
            PLACE_DETAIL_CACHE[requestKey]?.let { cachedState ->
                PLACE_DETAIL_CACHE[requestKey] =
                    cachedState.copy(
                        isSaved = nextSavedState,
                    )
            }
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) {
                    if (nextSavedState) {
                        saveDestinationUseCase(placeId = currentPlaceDetail.id)
                    } else {
                        deleteSavedDestinationUseCase(placeId = currentPlaceDetail.id)
                    }
                }
            }.onSuccess {
                _sideEffect.tryEmit(
                    if (nextSavedState) {
                        PlaceDetailSideEffect.PlaceSaved
                    } else {
                        PlaceDetailSideEffect.PlaceDeleted
                    },
                )
            }.onFailure {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSaved = previousSavedState)
                }
                if (requestKey != null) {
                    PLACE_DETAIL_CACHE[requestKey]?.let { cachedState ->
                        PLACE_DETAIL_CACHE[requestKey] =
                            cachedState.copy(
                                isSaved = previousSavedState,
                            )
                    }
                }
                if (it.isUnauthorized()) {
                    _sideEffect.tryEmit(PlaceDetailSideEffect.LoginRequired)
                }
            }
        }
    }

    private data class PlaceDetailRequestKey(
        val placeId: Long,
        val startDate: String?,
        val endDate: String?,
    )

    private data class CachedPlaceDetailState(
        val placeDetail: PlaceDetail,
        val isSaved: Boolean,
    )

    private companion object {
        private val PLACE_DETAIL_CACHE: MutableMap<PlaceDetailRequestKey, CachedPlaceDetailState> = mutableMapOf()
    }
}
