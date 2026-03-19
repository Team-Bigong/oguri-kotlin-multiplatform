package com.bigong.oguri.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "saved_recommendations")
class SavedRecommendation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: String = "GUEST",

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(nullable = false)
    val dayOffCount: Int,

    @Column(nullable = false)
    val totalTripCount: Int,
)
