package com.bigong.oguri.dto

data class MemberMeResponse(
    val id: String,
    val nickname: String,
    val preferredDayOff: Int,
    val remainingDayOff: Int
)
