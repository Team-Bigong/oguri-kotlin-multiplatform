package com.bigong.oguri.repository

import com.bigong.oguri.domain.SavedDestination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SavedDestinationRepository : JpaRepository<SavedDestination, Int> {
    fun findByMemberIdAndDestinationId(
        memberId: String,
        destinationId: Int,
    ): SavedDestination?

    fun deleteByMemberIdAndDestinationId(
        memberId: String,
        destinationId: Int,
    )

    fun findAllByMemberId(memberId: String): List<SavedDestination>

    fun deleteAllByMemberId(memberId: String)

    @Query(
        value = """
            SELECT sd.destination_id
            FROM saved_destinations sd
            JOIN destinations d ON sd.destination_id = d.id
            WHERE sd.created_at >= :since
            GROUP BY sd.destination_id, d.name
            ORDER BY COUNT(sd.id) DESC, d.name ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findTopDestinationIdsByCountAndNameAsc(
        since: java.time.LocalDateTime,
        limit: Int,
    ): List<Int>
}
