package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateTravelPlanRequestTest {
    @Test
    fun sendsCitiesListEvenWhenOnlyOneCityIsSelected() {
        val request = CreateTravelPlanRequest(
            requestId = "123e4567-e89b-12d3-a456-426614174000",
            city = "시드니",
            timeZone = "Australia/Sydney",
            title = "시드니 여행",
            startDate = "2027-09-10",
            endDate = "2027-09-19",
            cities = listOf("시드니"),
        )

        assertEquals(JsonArray(listOf(JsonPrimitive("시드니"))), Json.encodeToJsonElement(request).jsonObject["cities"])
    }
}
