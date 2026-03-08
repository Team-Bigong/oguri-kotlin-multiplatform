package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.MyPageInfo

interface MyPageRepository {
    suspend fun getMyPageInfo(): MyPageInfo

    suspend fun updatePreferredLeaveDays(preferredLeaveDays: Int): MyPageInfo

    suspend fun deleteSelectedPeriod(periodId: Long): MyPageInfo

    suspend fun deleteSavedPlace(placeId: Long): MyPageInfo
}
