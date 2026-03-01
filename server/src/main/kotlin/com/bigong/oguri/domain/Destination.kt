package com.bigong.oguri.domain

import jakarta.persistence.*

@Entity
@Table(name = "destinations")
class Destination(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    var country: Country? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 200)
    var summary: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "recommend_start_month")
    var recommendStartMonth: Int? = null,

    @Column(name = "recommend_end_month")
    var recommendEndMonth: Int? = null,

    @Column(length = 50)
    var flightTime: String? = null
)
