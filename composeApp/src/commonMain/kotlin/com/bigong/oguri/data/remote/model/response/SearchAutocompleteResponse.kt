package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchAutocompleteResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("destinationName")
    val destinationName: String,
    @SerialName("countryName")
    val countryName: String,
)
