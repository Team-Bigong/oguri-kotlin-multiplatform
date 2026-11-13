package com.bigong.oguri.service

import com.bigong.oguri.dto.response.TravelCitySuggestion
import com.bigong.oguri.dto.response.TravelPlaceAttribution
import com.bigong.oguri.dto.response.TravelPlaceResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore

@Component
class GoogleTravelPlacesClient(
    private val objectMapper: ObjectMapper,
    @param:Value("\${GOOGLE_PLACES_API_KEY:}") private val apiKey: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
    private val concurrentRequests: Semaphore = Semaphore(32)

    fun search(
        query: String,
        language: String,
    ): List<TravelPlaceResponse> {
        val body =
            objectMapper.writeValueAsString(
                mapOf("textQuery" to query, "languageCode" to language, "pageSize" to SEARCH_PAGE_SIZE),
            )
        return request(
            requestBuilder("/places:searchText", SEARCH_FIELDS)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
        ).join()?.path("places")?.mapNotNull(::toPlace).orEmpty()
    }

    fun cities(
        query: String,
        language: String,
    ): List<TravelCitySuggestion> {
        val body =
            objectMapper.writeValueAsString(
                mapOf("input" to query, "languageCode" to language, "includedPrimaryTypes" to listOf("(cities)")),
            )
        return request(
            requestBuilder("/places:autocomplete", CITY_FIELDS)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
        ).join()
            ?.path("suggestions")
            ?.mapNotNull { suggestion ->
                val prediction = suggestion.path("placePrediction")
                val id = prediction.path("placeId").asText()
                val label = prediction.path("text").path("text").asText()
                if (id.isBlank() || label.isBlank()) null else TravelCitySuggestion(id, label)
            }.orEmpty()
    }

    fun cityTimeZone(placeId: String): String? =
        request(requestBuilder("/places/$placeId", "timeZone").GET().build())
            .join()
            ?.path("timeZone")
            ?.path("id")
            ?.asText()
            ?.takeIf { it in java.time.ZoneId.getAvailableZoneIds() }

    fun details(
        placeId: String,
        language: String,
    ): CompletableFuture<TravelPlaceResponse?> =
        request(requestBuilder("/places/$placeId?languageCode=$language", PLACE_FIELDS).GET().build())
            .thenApply { node -> node?.let(::toPlace) }

    private fun requestBuilder(
        path: String,
        fields: String,
    ): HttpRequest.Builder {
        if (apiKey.isBlank()) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)
        return HttpRequest
            .newBuilder(URI.create("$BASE_URL$path"))
            .timeout(Duration.ofSeconds(5))
            .header("X-Goog-Api-Key", apiKey)
            .header("X-Goog-FieldMask", fields)
    }

    private fun request(request: HttpRequest): CompletableFuture<JsonNode?> {
        if (!concurrentRequests.tryAcquire()) {
            return CompletableFuture.failedFuture(ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS))
        }
        return client
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .whenComplete { _, _ ->
                concurrentRequests.release()
            }.thenApply { response ->
                when (response.statusCode()) {
                    200 -> objectMapper.readTree(response.body())
                    404 -> null
                    429 -> throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)
                    else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY)
                }
            }
    }

    private fun toPlace(node: JsonNode): TravelPlaceResponse? {
        val id = node.path("id").asText()
        val name = node.path("displayName").path("text").asText()
        if (id.isBlank() || name.isBlank()) return null
        val location = node.path("location")
        return TravelPlaceResponse(
            id = id,
            name = name,
            address = node.path("formattedAddress").asText().takeIf(String::isNotBlank),
            latitude = location.path("latitude").takeIf(JsonNode::isNumber)?.asDouble(),
            longitude = location.path("longitude").takeIf(JsonNode::isNumber)?.asDouble(),
            attributions =
                node.path("attributions").mapNotNull {
                    val provider = it.path("provider").asText()
                    if (provider.isBlank()) {
                        null
                    } else {
                        TravelPlaceAttribution(
                            provider,
                            it.path("providerUri").asText().takeIf { uri -> uri.startsWith("https://") || uri.startsWith("http://") },
                        )
                    }
                },
        )
    }

    private companion object {
        const val BASE_URL: String = "https://places.googleapis.com/v1"
        const val SEARCH_PAGE_SIZE: Int = 12
        const val CITY_FIELDS: String = "suggestions.placePrediction.placeId,suggestions.placePrediction.text.text"
        const val PLACE_FIELDS: String = "id,displayName,formattedAddress,location,attributions"
        val SEARCH_FIELDS: String = PLACE_FIELDS.split(',').joinToString(",") { "places.$it" }
    }
}
