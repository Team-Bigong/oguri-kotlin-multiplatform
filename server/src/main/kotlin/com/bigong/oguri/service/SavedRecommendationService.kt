package com.bigong.oguri.service

import com.bigong.oguri.domain.SavedRecommendation
import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.repository.SavedRecommendationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SavedRecommendationService(
    private val savedRecommendationRepository: SavedRecommendationRepository
) {
    /**
     * 연휴 기간 저장
     */
    fun save(request: SaveRecommendationRequest, memberId: String) {
        val existing = savedRecommendationRepository.findByMemberIdAndStartDateAndEndDate(
            memberId, request.startDate, request.endDate
        )
        if (existing != null) return

        val entity = SavedRecommendation(
            memberId = memberId,
            startDate = request.startDate,
            endDate = request.endDate,
            dayOffCount = request.dayOffCount,
            totalTripCount = request.totalTripCount
        )
        savedRecommendationRepository.save(entity)
    }

    /**
     * 연휴 기간 삭제 (저장 취소)
     */
    fun delete(request: SaveRecommendationRequest, memberId: String) {
        savedRecommendationRepository.deleteByMemberIdAndStartDateAndEndDate(
            memberId, request.startDate, request.endDate
        )
    }
}
