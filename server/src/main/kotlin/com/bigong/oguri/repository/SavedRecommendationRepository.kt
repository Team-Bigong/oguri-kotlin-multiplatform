package com.bigong.oguri.repository

import com.bigong.oguri.domain.SavedRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SavedRecommendationRepository : JpaRepository<SavedRecommendation, Int> {
    fun findByMemberIdAndStartDateAndEndDate(memberId: String, startDate: LocalDate, endDate: LocalDate): SavedRecommendation?
    fun deleteByMemberIdAndStartDateAndEndDate(memberId: String, startDate: LocalDate, endDate: LocalDate)
    fun findAllByMemberId(memberId: String): List<SavedRecommendation>
    fun deleteAllByMemberId(memberId: String)
}
