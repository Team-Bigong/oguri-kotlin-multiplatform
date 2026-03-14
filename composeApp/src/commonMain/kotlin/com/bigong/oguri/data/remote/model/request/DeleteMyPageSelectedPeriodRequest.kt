package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteMyPageSelectedPeriodRequest(
    @SerialName("periodId")
    val periodId: Long,
)
