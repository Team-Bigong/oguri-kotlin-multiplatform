package com.bigong.oguri.repository

import com.bigong.oguri.domain.SavedRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SavedRecommendationRepository : JpaRepository<SavedRecommendation, Int> {
    fun findByUserIdAndStartDateAndEndDate(userId: String, startDate: LocalDate, endDate: LocalDate): SavedRecommendation?
    fun deleteByUserIdAndStartDateAndEndDate(userId: String, startDate: LocalDate, endDate: LocalDate)
    fun findAllByUserId(userId: String): List<SavedRecommendation>
}
