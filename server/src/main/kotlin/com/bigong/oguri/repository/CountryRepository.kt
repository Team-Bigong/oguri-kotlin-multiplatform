package com.bigong.oguri.repository

import com.bigong.oguri.domain.Country
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CountryRepository : JpaRepository<Country, Int> {
    fun findByName(name: String): Country?
    fun findAllByOrderByNameAsc(): List<Country>
}
