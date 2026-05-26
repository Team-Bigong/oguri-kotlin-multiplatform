package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.HomeRemoteDataSource
import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.response.AdvertisementResponse
import com.bigong.oguri.data.remote.model.response.MonthlyTopPeriodResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.MonthlyTopPeriod
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.model.RecommendationSavedChange
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.LocalDate

@Inject
class DefaultHomeRepository(
    private val homeRemoteDataSource: HomeRemoteDataSource,
) : HomeRepository {
    private val recommendationSavedChangeFlow = MutableSharedFlow<RecommendationSavedChange>(extraBufferCapacity = 32)

    override suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod> =
        homeRemoteDataSource.getRecommendPeriodResponses(userCountry = userCountry).map { recommendPeriodResponse ->
            recommendPeriodResponse.toDomain()
        }

    override suspend fun getWeeklyTopPlaces(): List<Place> =
        homeRemoteDataSource
            .getWeeklyTopPlacesResponse()
            .weeklyTopPlaces
            .map { placeResponse -> placeResponse.toDomain() }

    override suspend fun getMonthlyTopPeriods(): List<MonthlyTopPeriod> =
        homeRemoteDataSource
            .getMonthlyTopPeriodResponses()
            .mapIndexed { index, monthlyTopPeriodResponse ->
                monthlyTopPeriodResponse.toDomain(rank = index + 1)
            }

    override fun observeRecommendationSavedChanges(): Flow<RecommendationSavedChange> = recommendationSavedChangeFlow.asSharedFlow()

    override suspend fun saveRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
        totalTripCount: Int,
    ) {
        homeRemoteDataSource.saveRecommendation(
            request =
                ManageSavedRecommendationRequest(
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                ),
        )
        recommendationSavedChangeFlow.tryEmit(
            RecommendationSavedChange(
                startDate = startDate,
                endDate = endDate,
                dayOffCount = dayOffCount,
                totalTripCount = totalTripCount,
                isSaved = true,
            ),
        )
    }

    override suspend fun deleteRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
        totalTripCount: Int,
    ) {
        homeRemoteDataSource.deleteRecommendation(
            request =
                ManageSavedRecommendationRequest(
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                ),
        )
        recommendationSavedChangeFlow.tryEmit(
            RecommendationSavedChange(
                startDate = startDate,
                endDate = endDate,
                dayOffCount = dayOffCount,
                totalTripCount = totalTripCount,
                isSaved = false,
            ),
        )
    }
}

private fun RecommendPeriodResponse.toDomain(): RecommendPeriod =
    RecommendPeriod(
        rank = rank,
        isSaved = saved,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        holiday = holiday,
        dayOffCount = dayOffCount,
        totalTripCount = totalTripCount,
        places = places.map { placeResponse -> placeResponse.toDomain() },
        advertisements = advertisements.map { advertisementResponse -> advertisementResponse.toDomain() },
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

private fun MonthlyTopPeriodResponse.toDomain(rank: Int): MonthlyTopPeriod =
    MonthlyTopPeriod(
        rank = rank,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        totalTripCount = totalTripCount,
        holidayCount = holidayCount,
        dayOffCount = dayOffCount,
    )

private fun AdvertisementResponse.toDomain(): Advertisement =
    Advertisement(
        platform = AdvertisementPlatform.from(value = platform),
        url = url,
    )
