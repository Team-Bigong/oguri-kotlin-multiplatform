package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemberDayOffRequest(
    @SerialName("preferredDayOff")
    val preferredDayOff: Int,
    @SerialName("remainingDayOff")
    val remainingDayOff: Int,
)
