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
    fun save(request: SaveRecommendationRequest, userId: String) {
        // 이미 저장되어 있는지 확인
        val existing = savedRecommendationRepository.findByUserIdAndStartDateAndEndDate(
            userId, request.startDate, request.endDate
        )
        if (existing != null) return

        val entity = SavedRecommendation(
            userId = userId,
            startDate = request.startDate,
            endDate = request.endDate,
            dayOffCount = request.dayOffCount
        )
        savedRecommendationRepository.save(entity)
    }

    /**
     * 연휴 기간 삭제 (저장 취소)
     */
    fun delete(request: SaveRecommendationRequest, userId: String) {
        savedRecommendationRepository.deleteByUserIdAndStartDateAndEndDate(
            userId, request.startDate, request.endDate
        )
    }
}
