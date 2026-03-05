package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.HomeRemoteDataSource
import com.bigong.oguri.data.remote.model.response.AdvertisementResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import dev.zacsweers.metro.Inject
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.repository.HomeRepository
import kotlinx.datetime.LocalDate

@Inject
class DefaultHomeRepository(
    private val homeRemoteDataSource: HomeRemoteDataSource,
) : HomeRepository {
    override suspend fun getRecommendPeriods(): List<RecommendPeriod> {
        return homeRemoteDataSource.getRecommendPeriodResponses().map { recommendPeriodResponse: RecommendPeriodResponse ->
            recommendPeriodResponse.toDomain()
        }
    }
}

private fun RecommendPeriodResponse.toDomain(): RecommendPeriod {
    return RecommendPeriod(
        rank = rank,
        isSaved = saved,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        holiday = holiday,
        dayOffCount = dayOffCount,
        totalTripCount = totalTripCount,
        places = places.map { placeResponse: PlaceResponse -> placeResponse.toDomain() },
        advertisements = advertisements.map { advertisementResponse: AdvertisementResponse -> advertisementResponse.toDomain() },
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

private fun AdvertisementResponse.toDomain(): Advertisement {
    return Advertisement(
        platform = AdvertisementPlatform.from(value = platform),
        url = url,
    )
}
