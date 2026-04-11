package com.bigong.oguri.repository

import com.bigong.oguri.domain.Destination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DestinationRepository : JpaRepository<Destination, Int> {
    @Query("SELECT DISTINCT d FROM Destination d JOIN FETCH d.country LEFT JOIN FETCH d.images")
    fun findAllWithCountryAndImages(): List<Destination>

    @Query("SELECT DISTINCT d FROM Destination d JOIN FETCH d.country LEFT JOIN FETCH d.images WHERE d.id = :destinationId")
    fun findByIdWithCountryAndImages(destinationId: Int): Destination?

    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Int,
    ): Boolean
}
