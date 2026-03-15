package com.bigong.oguri.dto

import java.time.LocalDate

data class MemberMeResponse(
    val id: String,
    val nickname: String,
    val preferredDayOff: Int,
    val remainingDayOff: Int,
    val savedPeriods: List<SavedPeriodDto>,
    val savedPlaces: List<SavedPlaceDto>
)

data class SavedPeriodDto(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dayOffCount: Int,
    val totalTripCount: Int
)

data class SavedPlaceDto(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrl: String
)
