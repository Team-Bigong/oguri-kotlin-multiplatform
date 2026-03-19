package com.bigong.oguri.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "saved_destinations")
class SavedDestination(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: String,

    @Column(name = "destination_id", nullable = false)
    val destinationId: Int,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
