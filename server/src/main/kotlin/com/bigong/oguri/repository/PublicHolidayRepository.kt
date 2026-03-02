package com.bigong.oguri.repository

import com.bigong.oguri.domain.PublicHoliday
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PublicHolidayRepository : JpaRepository<PublicHoliday, Int>
