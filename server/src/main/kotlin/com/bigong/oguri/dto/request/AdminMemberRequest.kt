package com.bigong.oguri.dto.request

data class AdminMemberCreateRequest(
    val id: String,
    val nickname: String?,
    val preferredDayOff: Int,
    val remainingDayOff: Int,
    val onboardingCompleted: Boolean,
)

data class AdminMemberUpdateRequest(
    val nickname: String?,
    val preferredDayOff: Int,
    val remainingDayOff: Int,
    val onboardingCompleted: Boolean,
)
