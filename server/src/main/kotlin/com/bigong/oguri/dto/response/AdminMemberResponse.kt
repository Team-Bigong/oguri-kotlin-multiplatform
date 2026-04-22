package com.bigong.oguri.dto.response

data class AdminMemberResponse(
    val id: String,
    val nickname: String?,
    val preferredDayOff: Int,
    val remainingDayOff: Int,
    val onboardingCompleted: Boolean,
    val updatedAt: String,
)
