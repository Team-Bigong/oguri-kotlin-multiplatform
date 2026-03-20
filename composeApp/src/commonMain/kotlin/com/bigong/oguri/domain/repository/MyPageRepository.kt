package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.model.PreferredLeaveDaysChange
import kotlinx.coroutines.flow.Flow

interface MyPageRepository {
    suspend fun getMyPageInfo(): MyPageInfo

    fun observePreferredLeaveDaysChanges(): Flow<PreferredLeaveDaysChange>

    suspend fun updateLeaveDays(
        remainingLeaveDays: Int,
        preferredLeaveDays: Int,
    ): MyPageInfo

    suspend fun deleteSelectedPeriod(periodId: Long): MyPageInfo

    suspend fun deleteSavedPlace(placeId: Long): MyPageInfo

    suspend fun withdraw()
}
