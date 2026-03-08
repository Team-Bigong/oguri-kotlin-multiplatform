package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyPageResponse(
    @SerialName("nickname")
    val nickname: String,
    @SerialName("remainingLeaveDays")
    val remainingLeaveDays: Int,
    @SerialName("preferredLeaveDays")
    val preferredLeaveDays: Int,
    @SerialName("selectedPeriods")
    val selectedPeriods: List<MyPageSelectedPeriodResponse>,
    @SerialName("savedPlaces")
    val savedPlaces: List<PlaceResponse>,
)
