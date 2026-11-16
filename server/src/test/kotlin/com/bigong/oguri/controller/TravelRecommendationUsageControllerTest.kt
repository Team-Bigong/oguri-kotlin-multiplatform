package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.repository.TravelRecommendationEntryKind
import com.bigong.oguri.repository.TravelRecommendationSource
import com.bigong.oguri.repository.TravelUsageRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class TravelRecommendationUsageControllerTest {
    private val usage = mock(TravelUsageRepository::class.java)
    private val mvc = MockMvcBuilders.standaloneSetup(TravelRecommendationUsageController(usage)).build()
    private val session = UUID.randomUUID()

    @Test
    fun `회원 여부는 서버 인증 결과로 분류하고 로그인 복귀는 회원만 기록한다`() {
        mvc.perform(request("CITY", "EXPOSED").header("X-Travel-Session", session)).andExpect(status().isNoContent)
        mvc
            .perform(
                request("PERIOD", "LOGIN_RESUMED")
                    .header("X-Travel-Session", session)
                    .requestAttr(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "member"),
            ).andExpect(status().isNoContent)
        mvc.perform(request("CITY", "LOGIN_RESUMED").header("X-Travel-Session", session)).andExpect(status().isBadRequest)
        mvc.perform(request("CITY", "CLICKED").header("X-Travel-Session", "invalid")).andExpect(status().isBadRequest)
        mvc.perform(request("CITY", "CLICKED")).andExpect(status().isBadRequest)
        mvc.perform(request("UNKNOWN", "CLICKED").header("X-Travel-Session", session)).andExpect(status().isBadRequest)
        verify(usage).recordRecommendationEntry(session, TravelRecommendationSource.CITY, TravelRecommendationEntryKind.EXPOSED, false)
        verify(usage).recordRecommendationEntry(session, TravelRecommendationSource.PERIOD, TravelRecommendationEntryKind.LOGIN_RESUMED, true)
        verifyNoMoreInteractions(usage)
    }

    private fun request(
        source: String,
        kind: String,
    ): MockHttpServletRequestBuilder =
        post("/api/v1/travel-recommendation-entries")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"source":"$source","kind":"$kind"}""")
}
