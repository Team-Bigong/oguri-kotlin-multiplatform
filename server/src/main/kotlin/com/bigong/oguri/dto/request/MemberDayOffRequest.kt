package com.bigong.oguri.dto.request

data class MemberDayOffRequest(
    val preferredDayOff: Int,
    val remainingDayOff: Int,
)
