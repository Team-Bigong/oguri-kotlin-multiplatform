package com.bigong.oguri.service

import com.bigong.oguri.domain.SavedDestination
import com.bigong.oguri.dto.ExperienceResponse
import com.bigong.oguri.dto.PlaceDetailResponse
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
    private val savedDestinationRepository: SavedDestinationRepository,
    private val homeService: HomeService,
) {
    /**
     * 마음에 드는 여행지 저장하기
     */
    @Transactional
    fun saveDestination(id: Int, memberId: String) {
        val existing = savedDestinationRepository.findByMemberIdAndDestinationId(memberId, id)
        if (existing != null) return
        savedDestinationRepository.save(SavedDestination(memberId = memberId, destinationId = id))
    }

    /**
     * 저장했던 여행지 취소하기 (삭제)
     */
    @Transactional
    fun deleteDestination(id: Int, memberId: String) {
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
        memberId: String
    ): PlaceDetailResponse {
        // 1. 여행지 및 국가, 이미지 통합 조회 (성능 최적화)
        val destinations = destinationRepository.findAllWithCountryAndImages()
        val target = destinations.find { it.id == id }
            ?: throw IllegalArgumentException("장소를 찾을 수 없습니다. ID: $id")

        // 2. 이미지 리스트 및 찜 여부 확인
        val thumbnailUrls = target.images.sortedBy { it.sortOrder }.map { it.imageUrl }
        val isSaved = savedDestinationRepository.findByMemberIdAndDestinationId(memberId, id) != null

        // 3. 마크다운 처리된 상세 설명
        val description = target.description?.let { processDescription(it) } ?: ""

        // 4. 추천 경험 데이터 (현재 더미)
        val experiences = listOf(
            ExperienceResponse(
                title = "현지인들만 아는 숨은 카페 투어",
                summary = "관광객 없는 조용한 골목의 커피 맛집을 찾아 떠납니다.",
                thumbnailUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=800&q=80",
                advertisementUrl = "https://www.klook.com"
            ),
            ExperienceResponse(
                title = "야경과 함께하는 루프탑 바",
                summary = "도시의 화려한 불빛을 한눈에 담을 수 있는 칵테일 바 추천.",
                thumbnailUrl = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?auto=format&fit=crop&w=800&q=80",
                advertisementUrl = "https://www.agoda.com"
            )
        )

        // 5. 관련 장소 목록 계산 (당시 추천되었던 다른 도시들)
        val savedDestinationIds = savedDestinationRepository.findAllByMemberId(memberId).map { it.destinationId }.toSet()

        val relevantPlaces = if (startDate != null && endDate != null) {
            val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
            homeService.calculateRecommendedPlaces(startDate, destinations, userCountry, totalDays)
                .filter { it.id != id.toLong() }
                .map { it.copy(isSaved = savedDestinationIds.contains(it.id.toInt())) }
        } else emptyList()

        // 6. 스카이스캐너 검색 링크 생성
        val flightUrl = "https://www.skyscanner.co.kr/transport/flights/sel/${target.name}"

        return PlaceDetailResponse(
            id = target.id.toLong(),
            country = target.country?.name ?: "Unknown",
            city = target.name,
            thumbnailUrls = thumbnailUrls,
            isSaved = isSaved,
            description = description,
            experiences = experiences,
            flightUrl = flightUrl,
            relevantPlaces = relevantPlaces
        )
    }

    /**
     * 설명문 마크다운 변환 (볼드 처리)
     */
    private fun processDescription(text: String): String {
        return if (text.contains("**")) text else text.replace("추천", "**추천**")
    }
}
