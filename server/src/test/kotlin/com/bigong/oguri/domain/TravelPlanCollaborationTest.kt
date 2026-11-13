package com.bigong.oguri.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class TravelPlanCollaborationTest {
    private fun plan(): TravelPlan =
        TravelPlan(
            UUID.randomUUID(),
            "owner",
            UUID.randomUUID(),
            "fingerprint",
            "Seoul",
            "Asia/Seoul",
            "Trip",
            LocalDate.of(2027, 4, 1),
            LocalDate.of(2027, 4, 3),
        )

    private val details = TravelVisitDetails("Museum", "Address", null, "Original")

    @Test
    fun `같은 방문의 독립 필드 수정은 서로 덮어쓰지 않는다`() {
        val plan = plan()
        plan.addVisit(1, details)
        val visit = plan.orderedVisits().single()
        val baseRevision = visit.revision
        plan.editVisit(visit.id, baseRevision, 1, details.copy(note = "Shared note"), details, 1)
        plan.editVisit(visit.id, baseRevision, 1, details.copy(localTime = LocalTime.of(10, 30)), details, 1)
        assertEquals("Shared note", visit.note)
        assertEquals(LocalTime.of(10, 30), visit.localTime)
    }

    @Test
    fun `같은 메모 충돌은 확정된 내용과 버전을 유지한다`() {
        val plan = plan()
        plan.addVisit(1, details)
        val visit = plan.orderedVisits().single()
        val baseRevision = visit.revision
        plan.editVisit(visit.id, baseRevision, 1, details.copy(note = "First edit"), details, 1)
        val revision = plan.revision
        assertThrows(TravelPlanConflict::class.java) {
            plan.editVisit(visit.id, baseRevision, 1, details.copy(note = "Second edit"), details, 1)
        }
        assertEquals("First edit", visit.note)
        assertEquals(revision, plan.revision)
    }

    @Test
    fun `다른 멤버가 옮긴 방문의 독립 수정은 현재 날짜를 유지한다`() {
        val plan = plan()
        plan.addVisit(1, details)
        val visit = plan.orderedVisits().single()
        val baseRevision = visit.revision
        plan.editVisit(visit.id, baseRevision, 2, details, details, 1)
        plan.editVisit(visit.id, baseRevision, 1, details.copy(note = "New note"), details, 1)
        assertEquals(2, visit.dayNumber)
        assertEquals("New note", visit.note)
    }

    @Test
    fun `기간 단축은 기존 미배정 뒤에 장소를 보존한다`() {
        val plan = plan()
        plan.addVisit(0, details.copy(name = "Undated"))
        plan.addVisit(2, details.copy(name = "Second day"))
        plan.addVisit(3, details.copy(name = "Third day"))
        val originalIds = plan.orderedVisits().map { it.id }.toSet()
        plan.updateDetails("Seoul", "Trip", "Asia/Seoul", plan.startDate, plan.startDate, plan.revision, 2)
        assertEquals(originalIds, plan.orderedVisits().map { it.id }.toSet())
        assertEquals(listOf("Undated", "Second day", "Third day"), plan.orderedVisits().map { it.name })
        assertTrue(plan.orderedVisits().all { it.dayNumber == 0 })
        assertEquals(
            3,
            plan
                .orderedVisits()
                .map { it.position }
                .distinct()
                .size,
        )
    }

    @Test
    fun `기간 변경 영향 확인이 오래되면 아무 항목도 옮기지 않는다`() {
        val plan = plan()
        plan.addVisit(3, details)
        val revision = plan.revision
        assertThrows(TravelPlanConflict::class.java) {
            plan.updateDetails("Seoul", "Trip", "Asia/Seoul", plan.startDate, plan.startDate, revision, 0)
        }
        assertEquals(3, plan.orderedVisits().single().dayNumber)
        assertEquals(revision, plan.revision)
    }

    @Test
    fun `순서 충돌과 삭제 후 수정은 기존 계획을 보존한다`() {
        val plan = plan()
        plan.addVisit(1, details)
        val first = plan.orderedVisits().single()
        val originalOrderRevision = plan.dayOrderRevisions().getValue(1)
        plan.addVisit(1, details.copy(name = "Second"))
        assertThrows(TravelPlanConflict::class.java) {
            plan.reorderDay(1, originalOrderRevision, plan.orderedVisits().map { it.id }.reversed())
        }
        plan.removeVisit(first.id, first.revision)
        assertThrows(TravelPlanConflict::class.java) { plan.editVisit(first.id, first.revision, 1, details) }
        assertEquals(listOf("Second"), plan.orderedVisits().map { it.name })
    }

    @Test
    fun `다른 수정 이후 삭제 되돌리기는 새 장소를 덮어쓰지 않는다`() {
        val plan = plan()
        plan.addVisit(1, details)
        val visit = plan.orderedVisits().single()
        val snapshot = TravelVisitUndo(visit.id, 1, visit.position, plan.structureRevision, null, visit.details())
        plan.removeVisit(visit.id, visit.revision)
        val removalRevision = plan.revision
        plan.addVisit(1, details.copy(name = "Later visit"))
        assertThrows(TravelPlanConflict::class.java) { plan.undoVisit(snapshot, removalRevision) }
        assertEquals(listOf("Later visit"), plan.orderedVisits().map { it.name })
    }
}
