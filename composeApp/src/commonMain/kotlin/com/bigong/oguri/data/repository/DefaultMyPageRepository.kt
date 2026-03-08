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
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class DefaultMyPageRepository(
    private val myPageRemoteDataSource: MyPageRemoteDataSource,
) : MyPageRepository {
    override suspend fun getMyPageInfo(): MyPageInfo {
        return myPageRemoteDataSource.getMyPageResponse().toDomain()
    }

    override suspend fun updatePreferredLeaveDays(preferredLeaveDays: Int): MyPageInfo {
        return myPageRemoteDataSource
            .updateLeaveDays(
                request = UpdateMyPageLeaveDaysRequest(preferredLeaveDays = preferredLeaveDays),
            ).toDomain()
    }

    override suspend fun deleteSelectedPeriod(periodId: Long): MyPageInfo {
        return myPageRemoteDataSource
            .deleteSelectedPeriod(
                request = DeleteMyPageSelectedPeriodRequest(periodId = periodId),
            ).toDomain()
    }

    override suspend fun deleteSavedPlace(placeId: Long): MyPageInfo {
        return myPageRemoteDataSource
            .deleteSavedPlace(
                request = DeleteMyPageSavedPlaceRequest(placeId = placeId),
            ).toDomain()
    }
}

private fun MyPageResponse.toDomain(): MyPageInfo {
    return MyPageInfo(
        nickname = nickname,
        remainingLeaveDays = remainingLeaveDays,
        preferredLeaveDays = preferredLeaveDays,
        selectedPeriods = selectedPeriods.map { selectedPeriodResponse: MyPageSelectedPeriodResponse -> selectedPeriodResponse.toDomain() },
        savedPlaces = savedPlaces.map { placeResponse: PlaceResponse -> placeResponse.toDomain() },
    )
}

private fun MyPageSelectedPeriodResponse.toDomain(): MyPageSelectedPeriod {
    return MyPageSelectedPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        totalTripCount = totalTripCount,
        dayOffCount = dayOffCount,
    )
}

private fun PlaceResponse.toDomain(): Place {
    return Place(
        id = id,
        country = country,
        city = city,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
    )
}
