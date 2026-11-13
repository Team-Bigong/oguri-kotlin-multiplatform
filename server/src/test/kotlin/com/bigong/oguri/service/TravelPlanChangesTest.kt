package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelPlan
import com.bigong.oguri.domain.TravelVisitDetails
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.request.TravelVisitRequest
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.TravelCollaborationRepository
import com.bigong.oguri.repository.TravelPlanRepository
import com.bigong.oguri.repository.TravelPlanViewRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.UUID

class TravelPlanChangesTest {
    private val plans = mock(TravelPlanRepository::class.java)
    private val members = mock(MemberRepository::class.java)
    private val collaboration = mock(TravelCollaborationRepository::class.java)
    private val service =
        TravelPlanService(
            plans,
            members,
            ObjectMapper().findAndRegisterModules(),
            mock(DestinationRepository::class.java),
            mock(TravelPlanViewRepository::class.java),
            collaboration,
        )
    private val member = "change-author"
    private val plan =
        TravelPlan(
            UUID.randomUUID(),
            member,
            UUID.randomUUID(),
            "fingerprint",
            "Seoul",
            "Asia/Seoul",
            "Trip",
            LocalDate.of(2027, 4, 1),
            LocalDate.of(2027, 4, 3),
        )

    @BeforeEach
    fun prepare() {
        `when`(members.existsById(member)).thenReturn(true)
        `when`(plans.findForUpdate(plan.id)).thenReturn(plan)
    }

    @Test
    fun `장소 추가 기록에 생성된 방문 식별자를 남긴다`() {
        service.execute(
            member,
            plan.id,
            TravelPlanCommand.AddVisit(
                UUID.randomUUID(),
                1,
                TravelVisitRequest("Museum", null, null, null, null),
                0,
            ),
        )
        val visit = plan.orderedVisits().single()
        verify(collaboration).recordChange(plan.id, member, plan.revision, 1, "VISIT_ADDED", visit.id)
    }

    @Test
    fun `날짜를 옮겨도 같은 방문 식별자를 남긴다`() {
        plan.addVisit(1, TravelVisitDetails("Museum", null, null, null, null))
        val visit = plan.orderedVisits().single()
        service.execute(
            member,
            plan.id,
            TravelPlanCommand.EditVisit(
                UUID.randomUUID(),
                visit.id,
                visit.revision,
                2,
                TravelVisitRequest("Museum", null, null, null, null),
                null,
                null,
                0,
            ),
        )
        verify(collaboration).recordChange(plan.id, member, plan.revision, 2, "VISIT_EDITED", visit.id)
    }

    @Test
    fun `삭제 기록은 삭제된 방문 식별자와 원래 날짜를 유지한다`() {
        plan.addVisit(1, TravelVisitDetails("Museum", null, null, null, null))
        val visit = plan.orderedVisits().single()
        service.execute(member, plan.id, TravelPlanCommand.RemoveVisit(UUID.randomUUID(), visit.id, visit.revision, 0))
        verify(collaboration).recordChange(plan.id, member, plan.revision, 1, "VISIT_REMOVED", visit.id)
    }
}
