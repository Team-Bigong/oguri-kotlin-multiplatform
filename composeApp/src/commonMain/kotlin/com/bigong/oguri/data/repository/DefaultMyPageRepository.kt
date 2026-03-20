package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.MyPageRemoteDataSource
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSavedPlaceRequest
import com.bigong.oguri.data.remote.model.request.DeleteMyPageSelectedPeriodRequest
import com.bigong.oguri.data.remote.model.request.UpdateMyPageLeaveDaysRequest
import com.bigong.oguri.data.remote.model.response.MyPageResponse
import com.bigong.oguri.data.remote.model.response.MyPageSelectedPeriodResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.PreferredLeaveDaysChange
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.LocalDate

@Inject
class DefaultMyPageRepository(
    private val myPageRemoteDataSource: MyPageRemoteDataSource,
) : MyPageRepository {
    private val preferredLeaveDaysChangeFlow = MutableSharedFlow<PreferredLeaveDaysChange>(extraBufferCapacity = 8)

    override suspend fun getMyPageInfo(): MyPageInfo = myPageRemoteDataSource.getMyPageResponse().toDomain()

    override fun observePreferredLeaveDaysChanges(): Flow<PreferredLeaveDaysChange> = preferredLeaveDaysChangeFlow.asSharedFlow()

    override suspend fun updateLeaveDays(
        remainingLeaveDays: Int,
        preferredLeaveDays: Int,
    ): MyPageInfo {
        val myPageInfo =
            myPageRemoteDataSource
                .updateLeaveDays(
                    request =
                        UpdateMyPageLeaveDaysRequest(
                            remainingLeaveDays = remainingLeaveDays,
                            preferredLeaveDays = preferredLeaveDays,
                        ),
                ).toDomain()
        preferredLeaveDaysChangeFlow.tryEmit(
            PreferredLeaveDaysChange(
                preferredLeaveDays = myPageInfo.preferredLeaveDays,
            ),
        )
        return myPageInfo
    }

    override suspend fun deleteSelectedPeriod(periodId: Long): MyPageInfo =
        myPageRemoteDataSource
            .deleteSelectedPeriod(
                request = DeleteMyPageSelectedPeriodRequest(periodId = periodId),
            ).toDomain()

    override suspend fun deleteSavedPlace(placeId: Long): MyPageInfo =
        myPageRemoteDataSource
            .deleteSavedPlace(
                request = DeleteMyPageSavedPlaceRequest(placeId = placeId),
            ).toDomain()

    override suspend fun withdraw() {
        myPageRemoteDataSource.withdraw()
    }
}

private fun MyPageResponse.toDomain(): MyPageInfo =
    MyPageInfo(
        nickname = nickname,
        remainingLeaveDays = remainingLeaveDays,
        preferredLeaveDays = preferredLeaveDays,
        selectedPeriods = selectedPeriods.map { selectedPeriodResponse -> selectedPeriodResponse.toDomain() },
        savedPlaces = savedPlaces.map { placeResponse -> placeResponse.toDomain() },
    )

private fun MyPageSelectedPeriodResponse.toDomain(): MyPageSelectedPeriod =
    MyPageSelectedPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        totalTripCount = totalTripCount,
        dayOffCount = dayOffCount,
    )

private fun PlaceResponse.toDomain(): Place =
    Place(
        id = id,
        country = country,
        city = city,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        isSaved = saved,
    )
