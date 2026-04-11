package com.bigong.oguri.dto

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

data class AdminMemberResponse(
    val id: String,
    val nickname: String?,
    val preferredDayOff: Int,
    val remainingDayOff: Int,
    val onboardingCompleted: Boolean,
    val updatedAt: String,
)
