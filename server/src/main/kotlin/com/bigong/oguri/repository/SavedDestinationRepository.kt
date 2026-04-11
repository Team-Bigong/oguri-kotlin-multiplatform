package com.bigong.oguri.repository

import com.bigong.oguri.domain.SavedDestination
import org.springframework.data.jpa.repository.JpaRepository
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
}
