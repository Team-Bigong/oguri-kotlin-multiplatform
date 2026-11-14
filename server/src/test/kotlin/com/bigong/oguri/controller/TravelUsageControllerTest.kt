package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.repository.TravelUsageActivity
import com.bigong.oguri.service.TravelPlacesService
import com.bigong.oguri.service.TravelPlanService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class TravelUsageControllerTest {
    private val service = mock(TravelPlanService::class.java)
    private val mvc = MockMvcBuilders.standaloneSetup(TravelPlanController(service, mock(TravelPlacesService::class.java))).build()
    private val planId = UUID.randomUUID()
    private val session = UUID.randomUUID()

    @Test
    fun `행동 종류와 세션만 받아 인증된 회원의 여행 서비스로 전달한다`() {
        mvc
            .perform(
                post("/api/v1/trips/$planId/activity")
                    .requestAttr(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "member")
                    .header("X-Travel-Session", session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"kind":"PLAN_DISPLAYED"}"""),
            ).andExpect(status().isNoContent)
        verify(service).recordActivity("member", planId, session, TravelUsageActivity.PLAN_DISPLAYED)
    }

    @Test
    fun `세션 누락 형식 오류와 허용하지 않은 행동은 기록하지 않는다`() {
        for ((header, kind) in listOf(null to "DAY_SELECTED", "invalid" to "DAY_SELECTED", session.toString() to "APP_OPENED")) {
            val request =
                post("/api/v1/trips/$planId/activity")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"kind":"$kind"}""")
            if (header != null) request.header("X-Travel-Session", header)
            mvc.perform(request).andExpect(status().isBadRequest)
        }
        verifyNoInteractions(service)
    }
}
