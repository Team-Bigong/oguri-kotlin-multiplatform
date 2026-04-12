package com.bigong.oguri.service

import com.bigong.oguri.domain.SavedDestination
import com.bigong.oguri.dto.response.ExperienceResponse
import com.bigong.oguri.dto.response.PlaceDetailResponse
import com.bigong.oguri.repository.DestinationExperienceRepository
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.SavedDestinationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
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
    private val exchangeService: ExchangeService,
    private val countryRepository: com.bigong.oguri.repository.CountryRepository,
) {
    // 대한민국의 빅맥 지수를 캐싱 (성능 최적화)
    private var koreaBigMacIndex: BigDecimal? = null

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

        // 7. 실시간 환율 정보 (해당 국가의 통화 코드 사용)
        val exchangeRateInfo = exchangeService.getExchangeRateInfo(target.country?.currencyCode)

        // 8. 체감 물가 계산 (해당 국가 BMI / 대한민국 BMI)
        val relativeCostIndex = calculateRelativeCostIndex(target.country?.bigMacIndex)

        return PlaceDetailResponse(
            id = target.id.toLong(),
            country = target.country?.name ?: "Unknown",
            city = target.name,
            thumbnailUrls = thumbnailUrls,
            isSaved = isSaved,
            exchangeRateInfo = exchangeRateInfo,
            relativeCostIndex = relativeCostIndex,
            averageTemperature = target.weatherTemp,
            averagePrecipitation = target.weatherPrecipitationMm,
            description = description,
            experiences = experiences,
            flightUrl = flightUrl,
            relevantPlaces = relevantPlaces,
        )
    }

    /**
     * 대한민국의 빅맥 지수를 기준으로 상대적인 물가 지수를 계산합니다.
     */
    private fun calculateRelativeCostIndex(targetBmi: BigDecimal?): Double? {
        if (targetBmi == null) return null

        // 캐싱된 대한민국 BMI가 없으면 조회 (최초 1회)
        if (koreaBigMacIndex == null) {
            koreaBigMacIndex = countryRepository.findByName("대한민국")?.bigMacIndex
        }

        val baseBmi = koreaBigMacIndex ?: return null
        if (baseBmi.compareTo(BigDecimal.ZERO) == 0) return null

        // 상대 지수 = 대상 국가 BMI / 대한민국 BMI (소수점 둘째자리까지)
        return targetBmi.divide(baseBmi, 2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * 설명문 마크다운 변환 (볼드 처리)
     */
    private fun processDescription(text: String): String = if (text.contains("**")) text else text.replace("추천", "**추천**")
}
