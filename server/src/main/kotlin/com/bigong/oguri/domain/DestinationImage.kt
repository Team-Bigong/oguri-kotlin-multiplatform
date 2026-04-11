package com.bigong.oguri.domain

import jakarta.persistence.*

@Entity
@Table(name = "destination_images")
class DestinationImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    var destination: Destination? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    var imageUrl: String,
    @Column(name = "is_thumbnail")
    var isThumbnail: Boolean = false,
    @Column(name = "sort_order")
    var sortOrder: Int = 1,
)
