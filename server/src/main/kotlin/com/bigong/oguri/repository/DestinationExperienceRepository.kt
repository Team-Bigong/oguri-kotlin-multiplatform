package com.bigong.oguri.repository

import com.bigong.oguri.domain.DestinationExperience
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DestinationExperienceRepository : JpaRepository<DestinationExperience, Int> {
    fun findAllByDestinationIdOrderBySortOrderAscIdAsc(destinationId: Int): List<DestinationExperience>
    fun findAllByDestinationIdInOrderByDestinationIdAscSortOrderAscIdAsc(destinationIds: Collection<Int>): List<DestinationExperience>
    fun findAllByOrderByDestinationIdAscSortOrderAscIdAsc(): List<DestinationExperience>
    fun deleteAllByDestinationId(destinationId: Int)
}
