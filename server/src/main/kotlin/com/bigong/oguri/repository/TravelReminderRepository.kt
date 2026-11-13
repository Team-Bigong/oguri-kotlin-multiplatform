package com.bigong.oguri.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class TravelReminderRepository(private val jdbc: JdbcClient) {
    fun bindDevice(
        deviceId: UUID,
        memberId: String,
        revocationHash: String,
        pushToken: String,
    ): Boolean {
        // Serialize claims of the same provider token across accounts.
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:token, 0))")
            .param("token", pushToken).query(Int::class.java).single()
        val device = jdbc.sql(
            """SELECT member_id, revocation_hash, revoked_at FROM travel_reminder_devices WHERE id = :id FOR UPDATE""",
        ).param("id", deviceId).query { row, _ ->
            Triple(row.getString("member_id"), row.getString("revocation_hash"), row.getObject("revoked_at"))
        }.optional().orElse(null)
        if (device != null && (device.first != memberId || device.second != revocationHash || device.third != null)) return false
        jdbc.sql(
            """UPDATE travel_reminder_devices SET revoked_at = statement_timestamp(), push_token = NULL
               WHERE md5(push_token) = md5(:token) AND push_token = :token AND id <> :id AND revoked_at IS NULL""",
        ).param("token", pushToken).param("id", deviceId).update()
        jdbc.sql(
            """INSERT INTO travel_reminder_devices (id, member_id, revocation_hash, push_token)
               VALUES (:id, :member, :hash, :token)
               ON CONFLICT (id) DO UPDATE SET push_token = excluded.push_token
               WHERE travel_reminder_devices.member_id = excluded.member_id
                 AND travel_reminder_devices.revocation_hash = excluded.revocation_hash
                 AND travel_reminder_devices.revoked_at IS NULL""",
        ).param("id", deviceId).param("member", memberId).param("hash", revocationHash).param("token", pushToken)
            .update().let { return it == 1 }
    }

    fun revokeDevice(deviceId: UUID, revocationHash: String) {
        jdbc.sql(
            """UPDATE travel_reminder_devices SET revoked_at = statement_timestamp(), push_token = NULL
               WHERE id = :id AND revocation_hash = :hash AND revoked_at IS NULL""",
        ).param("id", deviceId).param("hash", revocationHash).update()
    }

    fun lockDeviceIdentity(deviceId: UUID) {
        jdbc.sql("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(cast(:id as text), 1))")
            .param("id", deviceId).query(Int::class.java).single()
    }

    fun lockActiveDevice(deviceId: UUID, memberId: String): String? =
        jdbc.sql(
            """SELECT push_token FROM travel_reminder_devices
               WHERE id = :id AND member_id = :member AND revoked_at IS NULL FOR UPDATE""",
        ).param("id", deviceId).param("member", memberId).query(String::class.java).optional().orElse(null)

    fun current(planId: UUID, memberId: String): TravelReminderRow? =
        jdbc.sql(
            """SELECT r.* FROM travel_reminders r JOIN travel_reminder_devices d ON d.id = r.device_id
               WHERE r.travel_plan_id = :plan AND r.member_id = :member AND r.status <> 'CANCELLED'
                 AND d.revoked_at IS NULL""",
        ).param("plan", planId).param("member", memberId).query { row, _ -> row.toReminder() }.optional().orElse(null)

    fun find(id: UUID): TravelReminderRow? =
        jdbc.sql("SELECT * FROM travel_reminders WHERE id = :id")
            .param("id", id).query { row, _ -> row.toReminder() }.optional().orElse(null)

    fun insert(
        id: UUID,
        planId: UUID,
        memberId: String,
        deviceId: UUID,
        scheduledAt: OffsetDateTime,
        timeZone: String,
    ) {
        jdbc.sql(
            """INSERT INTO travel_reminders (id, travel_plan_id, member_id, device_id, scheduled_at, time_zone, status)
               VALUES (:id, :plan, :member, :device, :scheduled, :zone, 'PENDING')""",
        ).param("id", id).param("plan", planId).param("member", memberId).param("device", deviceId)
            .param("scheduled", scheduledAt).param("zone", timeZone).update()
    }

    fun cancelForMember(planId: UUID, memberId: String) {
        jdbc.sql(
            """UPDATE travel_reminders SET status = 'CANCELLED', updated_at = statement_timestamp()
               WHERE travel_plan_id = :plan AND member_id = :member AND status <> 'CANCELLED'""",
        ).param("plan", planId).param("member", memberId).update()
    }

    fun cancelVersion(planId: UUID, memberId: String, reminderId: UUID) {
        jdbc.sql(
            """UPDATE travel_reminders SET status = 'CANCELLED', updated_at = statement_timestamp()
               WHERE travel_plan_id = :plan AND member_id = :member AND id = :id AND status <> 'CANCELLED'""",
        ).param("plan", planId).param("member", memberId).param("id", reminderId).update()
    }

    fun cancelForPlan(planId: UUID) {
        jdbc.sql(
            """UPDATE travel_reminders SET status = 'CANCELLED', updated_at = statement_timestamp()
               WHERE travel_plan_id = :plan AND status <> 'CANCELLED'""",
        ).param("plan", planId).update()
    }

    fun due(): List<TravelReminderRow> =
        jdbc.sql(
            """SELECT r.* FROM travel_reminders r JOIN travel_reminder_devices d ON d.id = r.device_id
               WHERE r.status = 'PENDING' AND d.revoked_at IS NULL
                 AND r.scheduled_at BETWEEN statement_timestamp() - interval '15 minutes' AND statement_timestamp()
               ORDER BY r.scheduled_at LIMIT 50""",
        ).query { row, _ -> row.toReminder() }.list()

    fun beginHandoff(id: UUID): Boolean =
        jdbc.sql(
            """UPDATE travel_reminders SET status = 'HANDOFF_STARTED',
                 handoff_started_at = statement_timestamp(), updated_at = statement_timestamp()
               WHERE id = :id AND status = 'PENDING'
                 AND scheduled_at BETWEEN statement_timestamp() - interval '15 minutes' AND statement_timestamp()""",
        ).param("id", id).update() == 1

    fun finishHandoff(id: UUID, accepted: Boolean) {
        jdbc.sql(
            """UPDATE travel_reminders SET status = :status, updated_at = statement_timestamp()
               WHERE id = :id AND status = 'HANDOFF_STARTED'""",
        ).param("id", id).param("status", if (accepted) "SENT" else "FAILED").update()
    }

    fun expire() {
        jdbc.sql(
            """UPDATE travel_reminders SET status = 'EXPIRED', updated_at = statement_timestamp()
               WHERE (status = 'PENDING' AND scheduled_at < statement_timestamp() - interval '15 minutes')
                  OR (status = 'HANDOFF_STARTED' AND handoff_started_at < statement_timestamp() - interval '15 minutes')""",
        ).update()
    }

    private fun ResultSet.toReminder(): TravelReminderRow = TravelReminderRow(
        getObject("id", UUID::class.java),
        getObject("travel_plan_id", UUID::class.java),
        getString("member_id"),
        getObject("device_id", UUID::class.java),
        getObject("scheduled_at", OffsetDateTime::class.java),
        getString("time_zone"),
        TravelReminderStatus.valueOf(getString("status")),
    )
}

data class TravelReminderRow(
    val id: UUID,
    val planId: UUID,
    val memberId: String,
    val deviceId: UUID,
    val scheduledAt: OffsetDateTime,
    val timeZone: String,
    val status: TravelReminderStatus,
)

enum class TravelReminderStatus { PENDING, HANDOFF_STARTED, SENT, FAILED, CANCELLED, EXPIRED }
