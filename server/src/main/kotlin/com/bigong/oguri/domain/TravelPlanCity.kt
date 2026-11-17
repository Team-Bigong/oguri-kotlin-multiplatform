package com.bigong.oguri.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Embeddable
class TravelPlanCity(
    @Column(name = "city", nullable = false, length = 100)
    val label: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    val catalogCity: TravelCatalogCity? = null,
)
