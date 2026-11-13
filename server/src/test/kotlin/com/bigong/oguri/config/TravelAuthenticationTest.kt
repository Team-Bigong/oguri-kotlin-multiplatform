package com.bigong.oguri.config

import com.bigong.oguri.util.JwtTokenProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.server.ResponseStatusException

class TravelAuthenticationTest {
    private val interceptor = AuthInterceptor(mock(JwtTokenProvider::class.java))

    @Test
    fun `비회원은 여행 조회 생성 편집 도시검색과 참여를 요청할 수 없다`() {
        val endpoints =
            listOf(
                "GET" to "/api/v1/trips",
                "POST" to "/api/v1/trips",
                "GET" to "/api/v1/trips/trip",
                "GET" to "/api/v1/trips/trip/places",
                "POST" to "/api/v1/trips/trip/commands",
                "POST" to "/api/v1/travel-places/cities/search",
                "POST" to "/api/v1/travel-places/cities/time-zone",
                "POST" to "/api/v1/trip-invites/join",
            )
        for ((method, path) in endpoints) {
            val request = MockHttpServletRequest(method, path)
            request.addHeader("X-USER-ID", "spoofed-member")
            val failure =
                assertThrows(ResponseStatusException::class.java) {
                    interceptor.preHandle(request, MockHttpServletResponse(), Any())
                }
            assertEquals(HttpStatus.UNAUTHORIZED, failure.statusCode, path)
        }
    }

    @Test
    fun `로그인한 회원은 여행 도시 검색을 요청할 수 있다`() {
        val tokens = mock(JwtTokenProvider::class.java)
        `when`(tokens.validateToken("valid-token")).thenReturn(true)
        `when`(tokens.getMemberId("valid-token")).thenReturn("member")
        val request = MockHttpServletRequest("POST", "/api/v1/travel-places/cities/search")
        request.addHeader("Authorization", "Bearer valid-token")
        assertTrue(AuthInterceptor(tokens).preHandle(request, MockHttpServletResponse(), Any()))
        assertEquals("member", request.getAttribute(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE))
    }

    @Test
    fun `추천 탐색 초대 안내와 기기 연결 해제는 비회원도 요청할 수 있다`() {
        for ((method, path) in listOf(
            "GET" to "/api/v1/destinations",
            "POST" to "/api/v1/trip-invites/preview",
            "POST" to "/api/v1/travel-reminder-devices/device/revoke",
            "OPTIONS" to "/api/v1/trips",
        )) {
            assertTrue(interceptor.preHandle(MockHttpServletRequest(method, path), MockHttpServletResponse(), Any()), path)
        }
    }
}
