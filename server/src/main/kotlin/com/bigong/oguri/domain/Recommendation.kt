package com.bigong.oguri.domain

import jakarta.persistence.*

@Entity
@Table(name = "recommendations")
class Recommendation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    var destination: Destination? = null,

    @Column(name = "period_text", length = 50)
    var periodText: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null
)
