package com.bigong.oguri.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "destination_experiences")
class DestinationExperience(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "destination_id", nullable = false)
    val destinationId: Int,

    @Column(nullable = false, length = 150)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,

    @Column(name = "thumbnail_url", nullable = false, columnDefinition = "TEXT")
    val thumbnailUrl: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val link: String,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 1
)
