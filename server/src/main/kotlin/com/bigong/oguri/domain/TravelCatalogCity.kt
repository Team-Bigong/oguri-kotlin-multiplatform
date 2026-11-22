package com.bigong.oguri.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "cities")
@BatchSize(size = 32)
class TravelCatalogCity(
    @Id
    val id: Long,
    val name: String,
    @Column(name = "region_label")
    val regionLabel: String,
    @Column(name = "time_zone")
    val timeZone: String,
    val latitude: Double,
    val longitude: Double,
) {
    fun qualifiedLabel(): String = "$name · $regionLabel"
}
