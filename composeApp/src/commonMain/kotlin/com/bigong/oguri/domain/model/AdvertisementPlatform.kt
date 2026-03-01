package com.bigong.oguri.domain.model

enum class AdvertisementPlatform {
    AGODA,
    SKYSCANNER,
    KLOOK,
    UNKNOWN,
    ;

    companion object {
        fun from(value: String): AdvertisementPlatform =
            when (value.lowercase()) {
                "agoda" -> AGODA
                "skyscanner" -> SKYSCANNER
                "klook" -> KLOOK
                else -> UNKNOWN
            }
    }
}
