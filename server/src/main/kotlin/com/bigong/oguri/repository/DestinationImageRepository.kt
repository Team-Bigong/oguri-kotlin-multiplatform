package com.bigong.oguri.repository

import com.bigong.oguri.domain.DestinationImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DestinationImageRepository : JpaRepository<DestinationImage, Int> {
    fun findByDestinationIdAndIsThumbnailTrue(destinationId: Int): DestinationImage?

    fun findAllByDestinationIdOrderBySortOrderAscIdAsc(destinationId: Int): List<DestinationImage>

    fun deleteAllByDestinationId(destinationId: Int)
}
