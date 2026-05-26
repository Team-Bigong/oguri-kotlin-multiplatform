package com.bigong.oguri.repository

import com.bigong.oguri.domain.Destination
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DestinationRepository : JpaRepository<Destination, Int> {
    @Query("SELECT DISTINCT d FROM Destination d JOIN FETCH d.country LEFT JOIN FETCH d.images")
    fun findAllWithCountryAndImages(): List<Destination>

    @Query("SELECT DISTINCT d FROM Destination d JOIN FETCH d.country LEFT JOIN FETCH d.images WHERE d.id = :destinationId")
    fun findByIdWithCountryAndImages(destinationId: Int): Destination?

    @Query("SELECT d FROM Destination d JOIN FETCH d.country WHERE d.name LIKE %:query% OR d.country.name LIKE %:query% ORDER BY d.name ASC")
    fun findAutocompleteSuggestions(
        @Param("query") query: String,
        pageable: Pageable,
    ): List<Destination>

    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Int,
    ): Boolean
}
