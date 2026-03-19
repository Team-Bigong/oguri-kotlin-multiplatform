package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.HomeRemoteDataSource
import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.response.AdvertisementResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class DefaultHomeRepository(
    private val homeRemoteDataSource: HomeRemoteDataSource,
) : HomeRepository {
    override suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod> =
        homeRemoteDataSource.getRecommendPeriodResponses(userCountry = userCountry).map { recommendPeriodResponse ->
            recommendPeriodResponse.toDomain()
        }

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
                    dayOffCount = dayOffCount,
                    totalTripCount = totalTripCount,
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
                    dayOffCount = dayOffCount,
                    totalTripCount = totalTripCount,
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

private fun AdvertisementResponse.toDomain(): Advertisement =
    Advertisement(
        platform = AdvertisementPlatform.from(value = platform),
        url = url,
    )
