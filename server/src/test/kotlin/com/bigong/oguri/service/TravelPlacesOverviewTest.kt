package com.bigong.oguri.service

import com.bigong.oguri.dto.response.TravelPlaceAttribution
import com.bigong.oguri.dto.response.TravelPlaceResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.util.concurrent.CompletableFuture

class TravelPlacesOverviewTest {
    private val plans = mock(TravelPlanService::class.java)
    private val google = mock(GoogleTravelPlacesClient::class.java)
    private val service = TravelPlacesService(plans, google, 300)
    private val trip = UUID.randomUUID()

    @Test
    fun `접근이 종료되면 외부 장소를 조회하지 않는다`() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND)).`when`(plans).placeIdsForPlan("removed", trip)
        assertThrows(ResponseStatusException::class.java) { service.planPlaces("removed", trip, "ko") }
        verifyNoInteractions(google)
    }

    @Test
    fun `전체 날짜 조회는 일부 실패에도 성공한 장소를 출처와 함께 유지한다`() {
        val place =
            TravelPlaceResponse(
                "first",
                "Museum",
                "Address",
                37.5,
                127.0,
                listOf(TravelPlaceAttribution("Provider", "https://example.com/provider")),
            )
        `when`(plans.placeIdsForPlan("member", trip)).thenReturn(listOf("first", "failed", "missing"))
        `when`(google.details("first", "ko")).thenReturn(CompletableFuture.completedFuture(place))
        `when`(google.details("failed", "ko")).thenReturn(CompletableFuture.failedFuture(IllegalStateException()))
        `when`(google.details("missing", "ko")).thenReturn(CompletableFuture.completedFuture(null))
        val result = service.planPlaces("member", trip, "ko-KR")
        assertEquals(listOf(place), result.places)
        assertTrue(result.incomplete)
        verify(google).details("first", "ko")
    }

    @Test
    fun `장소 묶음은 상한을 넘기거나 여행 밖 장소를 포함하면 외부 호출 전에 거절한다`() {
        `when`(plans.placeIdsForPlan("member", trip)).thenReturn(listOf("first"))
        assertThrows(IllegalArgumentException::class.java) { service.planPlaceBatch("member", trip, List(31) { "first" }, "ko") }
        assertThrows(IllegalArgumentException::class.java) { service.planPlaceBatch("member", trip, listOf("outside"), "ko") }
        assertThrows(IllegalArgumentException::class.java) { service.planPlaceBatch("member", trip, emptyList(), "ko") }
        verifyNoInteractions(google)
    }

    @Test
    fun `장소 묶음은 동일 장소를 한 번만 조회한다`() {
        val place = TravelPlaceResponse("first", "Museum", null, 37.5, 127.0, emptyList())
        `when`(plans.placeIdsForPlan("member", trip)).thenReturn(listOf("first"))
        `when`(google.details("first", "ko")).thenReturn(CompletableFuture.completedFuture(place))
        assertEquals(listOf(place), service.planPlaceBatch("member", trip, listOf("first", "first"), "ko").places)
        verify(google).details("first", "ko")
    }

    @Test
    fun `묶음 조회도 전체 외부 API 예산 제한을 우회하지 않는다`() {
        `when`(plans.placeIdsForPlan("member", trip)).thenReturn(listOf("first"))
        val limited = TravelPlacesService(plans, google, 0)
        val failure = assertThrows(ResponseStatusException::class.java) { limited.planPlaceBatch("member", trip, listOf("first"), "ko") }
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, failure.statusCode)
        verifyNoInteractions(google)
    }

    @Test
    fun `묶음 조회도 접근 종료 시 외부 호출 전에 차단한다`() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND)).`when`(plans).placeIdsForPlan("removed", trip)
        assertThrows(ResponseStatusException::class.java) { service.planPlaceBatch("removed", trip, listOf("first"), "ko") }
        verifyNoInteractions(google)
    }

    @Test
    fun `외부 장소 없는 계획은 외부 호출 없이 빈 지도를 반환한다`() {
        `when`(plans.placeIdsForPlan("member", trip)).thenReturn(emptyList())
        val result = service.planPlaces("member", trip, "ko")
        assertTrue(result.places.isEmpty())
        assertFalse(result.incomplete)
        verifyNoInteractions(google)
    }
}
