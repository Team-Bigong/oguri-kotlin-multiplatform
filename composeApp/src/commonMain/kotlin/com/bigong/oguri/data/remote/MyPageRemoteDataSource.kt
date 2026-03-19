package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.DeleteMyPageSavedPlaceRequest
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSelectedPeriodRequest
import com.bigong.oguri.data.remote.model.request.UpdateMyPageLeaveDaysRequest
import com.bigong.oguri.data.remote.model.response.MyPageResponse

interface MyPageRemoteDataSource {
    suspend fun getMyPageResponse(): MyPageResponse

    suspend fun updateLeaveDays(request: UpdateMyPageLeaveDaysRequest): MyPageResponse

    suspend fun deleteSelectedPeriod(request: DeleteMyPageSelectedPeriodRequest): MyPageResponse

    suspend fun deleteSavedPlace(request: DeleteMyPageSavedPlaceRequest): MyPageResponse

    suspend fun withdraw()
}
