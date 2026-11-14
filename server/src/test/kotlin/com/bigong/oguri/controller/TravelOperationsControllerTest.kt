package com.bigong.oguri.controller

import com.bigong.oguri.config.AuthContext
import com.bigong.oguri.config.TravelOperationInterceptor
import com.bigong.oguri.service.TravelCollaborationService
import com.bigong.oguri.service.TravelPlacesService
import com.bigong.oguri.service.TravelPlanService
import com.bigong.oguri.service.TravelReminderService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class TravelOperationsControllerTest {
    private val plans = mock(TravelPlanService::class.java)
    private val collaboration = mock(TravelCollaborationService::class.java)
    private val reminders = mock(TravelReminderService::class.java)
    private val tripId = UUID.randomUUID()
    private val reminderId = UUID.randomUUID()
    private val tripPath = "/api/v1/trips/$tripId"

    @Test
    fun `새 쓰기와 초대를 중지해도 읽기와 접근 정리를 허용한다`() {
        val client = mvc(writesEnabled = false, invitesEnabled = false)
        client.perform(post("/api/v1/trips")).andExpect(status().isServiceUnavailable).andExpect(header().string("Cache-Control", "no-store"))
        client.perform(post("$tripPath/commands")).andExpect(status().isServiceUnavailable)
        client.perform(put("$tripPath/details")).andExpect(status().isServiceUnavailable)
        client.perform(post("$tripPath/ownership")).andExpect(status().isServiceUnavailable)
        client.perform(post("$tripPath/ownership/accept")).andExpect(status().isServiceUnavailable)
        client.perform(post("$tripPath/invites")).andExpect(status().isServiceUnavailable)
        client.perform(post("/api/v1/trip-invites/join")).andExpect(status().isServiceUnavailable)
        verifyNoInteractions(plans, collaboration, reminders)

        client.perform(get("/api/v1/trips/resume").requestAttr(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "member")).andExpect(status().isOk)
        client.perform(delete("$tripPath/invites").requestAttr(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "member")).andExpect(status().isNoContent)
        client.perform(delete("$tripPath/reminder/$reminderId").requestAttr(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, "member"))
            .andExpect(status().isNoContent)
        verify(plans).resume("member")
        verify(collaboration).revoke("member", tripId)
        verify(reminders).cancel("member", tripId, reminderId)
    }

    @Test
    fun `초대를 닫아도 일정 저장 요청의 본문은 정상 검증한다`() {
        val client = mvc(writesEnabled = true, invitesEnabled = false)
        client.perform(post("$tripPath/commands")).andExpect(status().isBadRequest)
        client.perform(post("$tripPath/invites")).andExpect(status().isServiceUnavailable)
        verifyNoInteractions(plans, collaboration)
    }

    private fun mvc(
        writesEnabled: Boolean,
        invitesEnabled: Boolean,
    ): MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                TravelPlanController(plans, mock(TravelPlacesService::class.java)),
                TravelCollaborationController(collaboration),
                TravelReminderController(reminders),
            ).addInterceptors(TravelOperationInterceptor(writesEnabled, invitesEnabled))
            .build()
}
