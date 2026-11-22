package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.service.TravelCityCatalog
import com.bigong.oguri.service.TravelPlanService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.server.ResponseStatusException

class TravelCityAccessTest {
    @Test
    fun `탈퇴한 회원은 유효한 토큰이 있어도 도시 검색과 시간대를 조회할 수 없다`() {
        val cities = mock(TravelCityCatalog::class.java)
        val plans = mock(TravelPlanService::class.java)
        doThrow(ResponseStatusException(HttpStatus.UNAUTHORIZED)).`when`(plans).requireMember("removed-member")
        val controller = TravelCityController(cities, plans)
        val request =
            MockHttpServletRequest().apply {
                setAttribute(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "removed-member")
            }
        assertThrows(ResponseStatusException::class.java) {
            controller.search(request, TravelCitySearchRequest("Tokyo", "ko"))
        }
        assertThrows(ResponseStatusException::class.java) {
            controller.timeZone(request, TravelCityTimeZoneRequest("place"))
        }
        verifyNoInteractions(cities)
    }
}
