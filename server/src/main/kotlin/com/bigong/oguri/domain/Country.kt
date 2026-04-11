package com.bigong.oguri.domain

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "countries")
class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(nullable = false, length = 50)
    var name: String,
    @Column(name = "big_mac_index", precision = 4, scale = 2)
    var bigMacIndex: BigDecimal? = null,
)
