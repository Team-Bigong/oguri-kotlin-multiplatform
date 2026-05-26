package com.bigong.oguri.repository

import com.bigong.oguri.domain.SavedRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SavedRecommendationRepository : JpaRepository<SavedRecommendation, Int> {
    fun findByMemberIdAndStartDateAndEndDate(
        memberId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): SavedRecommendation?

    fun deleteByMemberIdAndStartDateAndEndDate(
        memberId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    )

    fun findAllByMemberId(memberId: String): List<SavedRecommendation>

    fun deleteAllByMemberId(memberId: String)

    @Query(
        value = """
            SELECT start_date, end_date, day_off_count, total_trip_count 
            FROM saved_recommendations 
            WHERE created_at >= :since 
            GROUP BY start_date, end_date, day_off_count, total_trip_count 
            ORDER BY COUNT(id) DESC, start_date ASC 
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findTopSavedPeriodsByCount(
        since: java.time.LocalDateTime,
        limit: Int,
    ): List<Array<Any>>
}
