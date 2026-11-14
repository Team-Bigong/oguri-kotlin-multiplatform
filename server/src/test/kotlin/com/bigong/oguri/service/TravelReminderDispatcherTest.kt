package com.bigong.oguri.service

import com.bigong.oguri.repository.TravelReminderRepository
import com.bigong.oguri.repository.TravelReminderRow
import com.bigong.oguri.repository.TravelReminderStatus
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.util.UUID

class TravelReminderDispatcherTest {
    private val reminders = mock(TravelReminderRepository::class.java)
    private val service = mock(TravelReminderService::class.java)
    private val delivery = mock(TravelReminderDelivery::class.java)
    private val metrics = SimpleMeterRegistry()
    private val dispatcher = TravelReminderDispatcher(reminders, service, delivery, metrics)
    private val candidate =
        TravelReminderRow(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "member",
            UUID.randomUUID(),
            OffsetDateTime.parse("2027-04-01T00:00:00Z"),
            "UTC",
            TravelReminderStatus.PENDING,
        )

    @BeforeEach
    fun prepare() {
        `when`(delivery.configured).thenReturn(true)
        `when`(reminders.due()).thenReturn(listOf(candidate))
        `when`(delivery.prepare()).thenReturn("authorization")
        `when`(service.beginHandoff(candidate)).thenReturn("push-token")
    }

    @Test
    fun `발송 설정이 없으면 예약을 확보하거나 공급자에 요청하지 않는다`() {
        `when`(delivery.configured).thenReturn(false)
        dispatcher.sendDue()
        verify(reminders).expire()
        verify(reminders, never()).due()
        verify(delivery, never()).prepare()
        verifyNoInteractions(service)
    }

    @Test
    fun `공급자 인증 준비 실패는 예약을 인계 상태로 바꾸지 않는다`() {
        `when`(delivery.prepare()).thenThrow(IllegalStateException("Unavailable"))
        dispatcher.sendDue()
        verifyNoInteractions(service)
        verifyNoHandoffResult()
    }

    @Test
    fun `큐에서 읽은 뒤 취소된 예약은 공급자에 전달하지 않는다`() {
        `when`(service.beginHandoff(candidate)).thenReturn(null)
        dispatcher.sendDue()
        verify(delivery, never()).send(candidate.id, "push-token", "authorization")
        verifyNoHandoffResult()
    }

    @Test
    fun `공급자 수락은 인계 시작 뒤 한 번 기록한다`() {
        `when`(delivery.send(candidate.id, "push-token", "authorization")).thenReturn(true)
        dispatcher.sendDue()
        val order = inOrder(service, delivery, reminders)
        order.verify(service).beginHandoff(candidate)
        order.verify(delivery).send(candidate.id, "push-token", "authorization")
        order.verify(reminders).finishHandoff(candidate.id, true)
        assertEquals(1.0, metrics.get("oguri.travel.reminder.handoff").tag("outcome", "accepted").counter().count())
    }

    @Test
    fun `공급자 거절은 실패로 기록하고 즉시 재전송하지 않는다`() {
        `when`(delivery.send(candidate.id, "push-token", "authorization")).thenReturn(false)
        dispatcher.sendDue()
        verify(delivery, times(1)).send(candidate.id, "push-token", "authorization")
        verify(reminders).finishHandoff(candidate.id, false)
        assertEquals(1.0, metrics.get("oguri.travel.reminder.handoff").tag("outcome", "rejected").counter().count())
    }

    @Test
    fun `공급자 응답 불명확도 자동 재전송하지 않는다`() {
        `when`(delivery.send(candidate.id, "push-token", "authorization")).thenThrow(IllegalStateException("Unknown result"))
        dispatcher.sendDue()
        verify(delivery, times(1)).send(candidate.id, "push-token", "authorization")
        verify(reminders).finishHandoff(candidate.id, false)
    }

    @Test
    fun `발송 중단은 인계 상태를 유지하고 스레드 중단을 전달한다`() {
        `when`(delivery.send(candidate.id, "push-token", "authorization")).thenAnswer { throw InterruptedException() }
        try {
            dispatcher.sendDue()
            assertTrue(Thread.currentThread().isInterrupted)
            verifyNoHandoffResult()
        } finally {
            Thread.interrupted()
        }
        assertFalse(Thread.currentThread().isInterrupted)
    }

    private fun verifyNoHandoffResult() {
        verify(reminders, never()).finishHandoff(candidate.id, true)
        verify(reminders, never()).finishHandoff(candidate.id, false)
    }
}
