package com.bigong.oguri.service

import com.bigong.oguri.dto.ExperienceResponse
import com.bigong.oguri.dto.PlaceDetailResponse
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class DestinationService(
    private val destinationRepository: DestinationRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val homeService: HomeService,
    private val memberService: MemberService
) {
    /**
     * 장소 상세 정보 조회
     * @param id 장소 ID
     * @param startDate 당시 추천받았던 시작일 (관련 장소 계산용)
     * @param userCountry 사용자 국가 (정렬용)
     * @param memberId 멤버 ID (저장 여부 확인용)
     */
    fun getDestinationDetail(
        id: Int,
        startDate: LocalDate?,
        endDate: LocalDate?,
        userCountry: String,
        memberId: String
    ): PlaceDetailResponse {
        // 1. 장소 기본 정보 조회 (N+1 최적화 쿼리 사용)
        val destinations = destinationRepository.findAllWithCountryAndImages()
        val target = destinations.find { it.id == id } 
            ?: throw IllegalArgumentException("장소를 찾을 수 없습니다. ID: $id")

        // 2. 이미지 URL 리스트 정렬 (sortOrder 기준)
        val thumbnailUrls = target.images.sortedBy { it.sortOrder }.map { it.imageUrl }

        // 3. 사용자 연차 설정 조회
        val dayOffCount = memberService.getRemainingDayOff(memberId)

        // 4. 저장 여부 확인 (해당 기간에 대해 저장했는지 여부)
        val isSaved = if (startDate != null && endDate != null) {
            savedRecommendationRepository.findByMemberIdAndStartDateAndEndDate(memberId, startDate, endDate) != null
        } else false

        // 5. 설명문 가공 (**굵게** 표시 적용)
        val description = target.description?.let { processDescription(it) } ?: ""

        // 6. 더미 경험 데이터 생성
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

        // 7. 관련 장소(relevantPlaces) 계산
        // 당시 1순위(또는 현재 선택된 기간) 내의 다른 추천 장소들을 가져옵니다.
        val relevantPlaces = if (startDate != null && endDate != null) {
            val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
            homeService.calculateRecommendedPlaces(startDate, destinations, userCountry, totalDays)
                .filter { it.id != id.toLong() } // 현재 보고 있는 장소는 제외
        } else emptyList()

        // 8. 항공권 검색 URL (Skyscanner 예시)
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
     * 설명문 마크다운 처리 (예시: 특정 키워드 굵게 만들기)
     * 실제로는 DB에 이미 **텍스트** 형태로 들어있다면 그대로 반환해도 되지만,
     * 여기서는 예시로 "추천" 단어를 굵게 만들어 보겠습니다.
     */
    private fun processDescription(text: String): String {
        // 이미 ** 가 포함되어 있다면 그대로 반환, 없으면 특정 로직 수행
        return if (text.contains("**")) text else text.replace("추천", "**추천**")
    }
}
