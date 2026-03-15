package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.PlaceDetailRemoteDataSource
import com.bigong.oguri.data.remote.model.response.ExperienceResponse
import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import com.bigong.oguri.domain.model.Experience
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.PlaceDetail
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
    ): PlaceDetail {
        return placeDetailRemoteDataSource
            .getPlaceDetailResponse(
                placeId = placeId,
                startDate = startDate,
                endDate = endDate,
                userCountry = userCountry,
            ).toDomain()
    }

    override suspend fun saveDestination(placeId: Long) {
        placeDetailRemoteDataSource.saveDestination(placeId = placeId)
    }

    override suspend fun deleteSavedDestination(placeId: Long) {
        placeDetailRemoteDataSource.deleteSavedDestination(placeId = placeId)
    }
}

private fun PlaceDetailResponse.toDomain(): PlaceDetail {
    return PlaceDetail(
        id = id,
        country = country,
        city = city,
        thumbnailUrls = thumbnailUrls,
        isSaved = isSaved,
        description = description,
        experiences = experiences.map { experienceResponse: ExperienceResponse -> experienceResponse.toDomain() },
        flightUrl = flightUrl,
        relevantPlaces = relevantPlaces.map { placeResponse: PlaceResponse -> placeResponse.toDomain() },
    )
}

private fun ExperienceResponse.toDomain(): Experience {
    return Experience(
        title = title,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        advertisementUrl = advertisementUrl,
    )
}

private fun PlaceResponse.toDomain(): Place {
    return Place(
        id = id,
        country = country,
        city = city,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        isSaved = saved,
    )
}
