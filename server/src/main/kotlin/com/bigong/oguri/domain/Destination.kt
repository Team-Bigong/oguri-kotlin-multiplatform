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
    @Column(name = "recommend_start_month_1")
    var recommendStartMonth1: Int? = null,
    @Column(name = "recommend_end_month_1")
    var recommendEndMonth1: Int? = null,
    @Column(name = "recommend_start_month_2")
    var recommendStartMonth2: Int? = null,
    @Column(name = "recommend_end_month_2")
    var recommendEndMonth2: Int? = null,
    @Column(name = "flight_time_minutes")
    var flightTimeMinutes: Int? = null,
    @Column(name = "flight_url", columnDefinition = "TEXT")
    var flightUrl: String? = null,
    @Column(name = "weather_temp_1")
    var weatherTemp1: Int? = null,
    @Column(name = "weather_precipitation_mm_1")
    var weatherPrecipitationMm1: Double? = null,
    @Column(name = "weather_temp_2")
    var weatherTemp2: Int? = null,
    @Column(name = "weather_precipitation_mm_2")
    var weatherPrecipitationMm2: Double? = null,
    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    var images: List<DestinationImage> = mutableListOf(),
)
