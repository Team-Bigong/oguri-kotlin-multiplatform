package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberMeResponse(
    @SerialName("id")
    val id: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
)
