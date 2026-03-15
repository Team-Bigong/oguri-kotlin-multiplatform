package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberMeResponse(
    @SerialName("id")
    val id: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("preferredDayOff")
    val preferredDayOff: Int,
    @SerialName("remainingDayOff")
    val remainingDayOff: Int,
    @SerialName("savedPeriods")
    val savedPeriods: List<MemberSavedPeriodResponse> = emptyList(),
    @SerialName("savedPlaces")
    val savedPlaces: List<PlaceResponse> = emptyList(),
)
