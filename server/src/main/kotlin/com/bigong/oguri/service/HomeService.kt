package com.bigong.oguri.service

import com.bigong.oguri.dto.AdvertisementResponse
import com.bigong.oguri.dto.PlaceResponse
import com.bigong.oguri.dto.RecommendPeriodResponse
import com.bigong.oguri.repository.DestinationImageRepository
import com.bigong.oguri.repository.DestinationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class HomeService(
    private val destinationRepository: DestinationRepository,
    private val destinationImageRepository: DestinationImageRepository
) {
    fun getHomeData(): RecommendPeriodResponse {
        val destinations = destinationRepository.findAll()

        val places = destinations.map { destination ->
            val thumbnail = destinationImageRepository.findByDestinationIdAndIsThumbnailTrue(destination.id)
            PlaceResponse(
                id = destination.id.toLong(),
                country = destination.country?.name ?: "Unknown",
                city = destination.name,
                summary = destination.summary ?: "",
                thumbnailUrl = thumbnail?.imageUrl ?: ""
            )
        }

        // 더미 데이터 생성
        val advertisements = listOf(
            AdvertisementResponse(platform = "google", url = "https://www.google.com"),
            AdvertisementResponse(platform = "naver", url = "https://www.naver.com")
        )

        return RecommendPeriodResponse(
            rank = 1,
            isSaved = false,
            startDate = LocalDate.of(2026, 3, 1),
            endDate = LocalDate.of(2026, 3, 10),
            holiday = listOf("삼일절", "어린이날"),
            dayOffCount = 3,
            totalTripCount = 5,
            places = places,
            advertisements = advertisements
        )
    }
}
