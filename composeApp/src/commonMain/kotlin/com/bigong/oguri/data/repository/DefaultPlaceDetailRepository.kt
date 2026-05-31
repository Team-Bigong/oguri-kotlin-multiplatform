package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.PlaceDetailRemoteDataSource
import com.bigong.oguri.data.remote.model.response.ExperienceResponse
import com.bigong.oguri.data.remote.model.response.PlaceDetailExchangeRateInfoResponse
import com.bigong.oguri.data.remote.model.response.PlaceDetailRecommendPeriodResponse
import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.domain.model.Experience
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.PlaceDetail
import com.bigong.oguri.domain.model.PlaceDetailExchangeRateInformation
import com.bigong.oguri.domain.model.PlaceDetailRecommendPeriod
import com.bigong.oguri.domain.model.PlaceDetailTravelInformation
import com.bigong.oguri.domain.repository.PlaceDetailRepository
import dev.zacsweers.metro.Inject

@Inject
class DefaultPlaceDetailRepository(
    private val placeDetailRemoteDataSource: PlaceDetailRemoteDataSource,
) : PlaceDetailRepository {
    override suspend fun getPlaceDetail(
        placeId: Long,
        startDate: String?,
        endDate: String?,
        userCountry: String,
    ): PlaceDetail =
        placeDetailRemoteDataSource
            .getPlaceDetailResponse(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
                userCountry = userCountry,
            ).toDomain()

    override suspend fun saveDestination(placeId: Long) {
        placeDetailRemoteDataSource.saveDestination(placeId = placeId)
    }

    override suspend fun deleteSavedDestination(placeId: Long) {
        placeDetailRemoteDataSource.deleteSavedDestination(placeId = placeId)
    }
}

private fun PlaceDetailResponse.toDomain(): PlaceDetail =
    PlaceDetail(
        id = id,
        country = country,
        city = city,
        thumbnailUrls = thumbnailUrls,
        isSaved = isSaved,
        travelInformation =
            PlaceDetailTravelInformation(
                exchangeRateInformation = exchangeRateInfo.toDomain(),
                relativeCostIndex = relativeCostIndex ?: DEFAULT_DECIMAL,
                averageTemperature = averageTemperature ?: DEFAULT_NUMBER,
                averagePrecipitation = averagePrecipitation ?: DEFAULT_DECIMAL,
                recommendPeriod = recommendPeriod.toDomain(),
            ),
        description = description,
        experiences = experiences.map { experienceResponse -> experienceResponse.toDomain() },
        flightUrl = flightUrl,
        relevantPlaces = relevantPlaces.map { placeResponse -> placeResponse.toDomain() },
    )

private fun PlaceDetailExchangeRateInfoResponse?.toDomain(): PlaceDetailExchangeRateInformation =
    PlaceDetailExchangeRateInformation(
        koreanWonAmount = this?.krwAmount ?: DEFAULT_NUMBER,
        currencyUnit = this?.currencyUnit ?: DEFAULT_NUMBER,
        currencyCode = this?.currencyCode.orEmpty(),
        date = this?.date.orEmpty(),
    )

private fun PlaceDetailRecommendPeriodResponse?.toDomain(): PlaceDetailRecommendPeriod =
    PlaceDetailRecommendPeriod(
        startMonth = this?.startMonth ?: DEFAULT_NUMBER,
        endMonth = this?.endMonth ?: DEFAULT_NUMBER,
    )

private fun ExperienceResponse.toDomain(): Experience =
    Experience(
        title = title,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        advertisementUrl = advertisementUrl,
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

private const val DEFAULT_NUMBER = 0
private const val DEFAULT_DECIMAL = 0.0
