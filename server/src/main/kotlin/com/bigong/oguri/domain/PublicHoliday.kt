package com.bigong.oguri.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "public_holidays")
class PublicHoliday(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    val holidayDate: LocalDate,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false)
    val isActualHoliday: Boolean = true
)
