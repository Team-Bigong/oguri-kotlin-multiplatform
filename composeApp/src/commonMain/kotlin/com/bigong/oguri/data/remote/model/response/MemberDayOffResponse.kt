package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberDayOffResponse(
    @SerialName("preferredDayOff")
    val preferredDayOff: Int,
    @SerialName("remainingDayOff")
    val remainingDayOff: Int,
)
