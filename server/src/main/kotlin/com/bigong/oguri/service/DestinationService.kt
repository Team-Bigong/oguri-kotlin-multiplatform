package com.bigong.oguri.service

import com.bigong.oguri.domain.SavedDestination
import com.bigong.oguri.dto.response.ExperienceResponse
import com.bigong.oguri.dto.response.PlaceDetailResponse
import com.bigong.oguri.repository.DestinationExperienceRepository
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.SavedDestinationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 여행지 상세 정보 및 저장(찜) 관련 기능을 담당하는 서비스
 */
@Service
@Transactional(readOnly = true)
class DestinationService(
    private val destinationRepository: DestinationRepository,
    private val destinationExperienceRepository: DestinationExperienceRepository,
    private val savedDestinationRepository: SavedDestinationRepository,
    private val homeService: HomeService,
) {
    /**
     * 마음에 드는 여행지 저장하기
     */
    @Transactional
    fun saveDestination(
        id: Int,
        memberId: String,
    ) {
        val existing = savedDestinationRepository.findByMemberIdAndDestinationId(memberId, id)
        if (existing != null) return
        savedDestinationRepository.save(SavedDestination(memberId = memberId, destinationId = id))
    }

    /**
     * 저장했던 여행지 취소하기 (삭제)
     */
    @Transactional
    fun deleteDestination(
        id: Int,
        memberId: String,
    ) {
        savedDestinationRepository.deleteByMemberIdAndDestinationId(memberId, id)
    }

    /**
     * 여행지 상세 정보 및 맞춤형 관련 장소 목록 조회
     */
    fun getDestinationDetail(
        id: Int,
        startDate: LocalDate?,
        endDate: LocalDate?,
        userCountry: String,
        memberId: String,
    ): PlaceDetailResponse {
        // 1. 여행지 및 국가, 이미지 통합 조회
        val target =
            destinationRepository.findByIdWithCountryAndImages(id)
                ?: throw IllegalArgumentException("장소를 찾을 수 없습니다. ID: $id")
        val destinations = destinationRepository.findAllWithCountryAndImages()

        // 2. 이미지 리스트 및 찜 여부 확인
        val thumbnailUrls = target.images.sortedBy { it.sortOrder }.map { it.imageUrl }
        val isSaved = savedDestinationRepository.findByMemberIdAndDestinationId(memberId, id) != null

        // 3. 마크다운 처리된 상세 설명
        val description = target.description?.let { processDescription(it) } ?: ""

        // 4. 장소별 액티비티/즐길거리 조회
        val experiences =
            destinationExperienceRepository
                .findAllByDestinationIdOrderBySortOrderAscIdAsc(id)
                .map { experience ->
                    ExperienceResponse(
                        title = experience.title,
                        summary = experience.description,
                        thumbnailUrl = experience.thumbnailUrl,
                        advertisementUrl = experience.link,
                    )
                }

        // 5. 관련 장소 목록 계산 (당시 추천되었던 다른 도시들)
        val savedDestinationIds = savedDestinationRepository.findAllByMemberId(memberId).map { it.destinationId }.toSet()

        val relevantPlaces =
            if (startDate != null && endDate != null) {
                val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
                homeService
                    .calculateRecommendedPlaces(startDate, destinations, userCountry, totalDays)
                    .filter { it.id != id.toLong() }
                    .map { it.copy(isSaved = savedDestinationIds.contains(it.id.toInt())) }
            } else {
                emptyList()
            }

        // 6. 스카이스캐너 검색 링크 생성
        val flightUrl = target.flightUrl ?: "https://www.skyscanner.co.kr/transport/flights/sel/${target.name}"

        return PlaceDetailResponse(
            id = target.id.toLong(),
            country = target.country?.name ?: "Unknown",
            city = target.name,
            thumbnailUrls = thumbnailUrls,
            isSaved = isSaved,
            description = description,
            experiences = experiences,
            flightUrl = flightUrl,
            relevantPlaces = relevantPlaces,
        )
    }

    /**
     * 설명문 마크다운 변환 (볼드 처리)
     */
    private fun processDescription(text: String): String = if (text.contains("**")) text else text.replace("추천", "**추천**")
}
