package com.bigong.oguri.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class TravelCollaborationRepository(
    private val jdbc: JdbcClient,
) {
    fun lockMemberPlans(memberId: String): List<UUID> =
        jdbc
            .sql(
                """SELECT id FROM travel_plans
               WHERE owner_id = :member OR id IN (SELECT travel_plan_id FROM travel_plan_members WHERE member_id = :member)
               ORDER BY id FOR UPDATE""",
            ).param("member", memberId)
            .query(UUID::class.java)
            .list()

    fun isMember(
        planId: UUID,
        memberId: String,
    ): Boolean =
        jdbc
            .sql("SELECT EXISTS(SELECT 1 FROM travel_plan_members WHERE travel_plan_id = :plan AND member_id = :member)")
            .param("plan", planId)
            .param("member", memberId)
            .query(Boolean::class.javaObjectType)
            .single()

    fun join(
        planId: UUID,
        memberId: String,
    ) {
        jdbc
            .sql("INSERT INTO travel_plan_members (travel_plan_id, member_id) VALUES (:plan, :member) ON CONFLICT DO NOTHING")
            .param("plan", planId)
            .param("member", memberId)
            .update()
    }

    fun memberCounts(planIds: List<UUID>): Map<UUID, Int> {
        if (planIds.isEmpty()) return emptyMap()
        return jdbc.sql(
            """SELECT plan.id, 1 + count(membership.member_id) AS member_count
               FROM travel_plans plan
               LEFT JOIN travel_plan_members membership
                 ON membership.travel_plan_id = plan.id AND membership.member_id <> plan.owner_id
               WHERE plan.id IN (:plans)
               GROUP BY plan.id""",
        ).param("plans", planIds)
            .query { row, _ -> row.getObject("id", UUID::class.java) to row.getInt("member_count") }
            .list()
            .toMap()
    }

    fun members(
        planId: UUID,
        ownerId: String,
    ): List<TravelMemberRow> =
        jdbc
            .sql(
                """
            SELECT id, nickname, id = :owner AS owner FROM members
            WHERE id = :owner OR id IN (SELECT member_id FROM travel_plan_members WHERE travel_plan_id = :plan)
            ORDER BY owner DESC, nickname, id
            """,
            ).param("plan", planId)
            .param("owner", ownerId)
            .query { row, _ -> TravelMemberRow(row.getString("id"), row.getString("nickname"), row.getBoolean("owner")) }
            .list()

    fun remove(
        planId: UUID,
        memberId: String,
    ): Boolean =
        jdbc
            .sql("DELETE FROM travel_plan_members WHERE travel_plan_id = :plan AND member_id = :member")
            .param("plan", planId)
            .param("member", memberId)
            .update() != 0

    fun removeView(
        planId: UUID,
        memberId: String,
    ) {
        jdbc
            .sql("DELETE FROM travel_plan_views WHERE travel_plan_id = :plan AND member_id = :member")
            .param("plan", planId)
            .param("member", memberId)
            .update()
    }

    fun issue(
        planId: UUID,
        memberId: String,
        hash: String,
        expiresAt: OffsetDateTime,
    ) {
        jdbc
            .sql(
                "INSERT INTO travel_plan_invites (token_hash, travel_plan_id, issued_by, expires_at) VALUES (:hash, :plan, :member, :expiry)",
            ).param("hash", hash)
            .param("plan", planId)
            .param("member", memberId)
            .param("expiry", expiresAt)
            .update()
    }

    fun invite(hash: String): TravelInviteRow? =
        jdbc
            .sql("SELECT travel_plan_id, issued_by, expires_at, revoked_at FROM travel_plan_invites WHERE token_hash = :hash")
            .param("hash", hash)
            .query { row, _ ->
                TravelInviteRow(
                    row.getObject("travel_plan_id", UUID::class.java),
                    row.getString("issued_by"),
                    row.getObject("expires_at", OffsetDateTime::class.java),
                    row.getObject("revoked_at", OffsetDateTime::class.java),
                )
            }.optional()
            .orElse(null)

    fun revokeInvites(planId: UUID) {
        jdbc
            .sql("UPDATE travel_plan_invites SET revoked_at = statement_timestamp() WHERE travel_plan_id = :plan AND revoked_at IS NULL")
            .param("plan", planId)
            .update()
    }

    fun memberCommand(
        planId: UUID,
        memberId: String,
        requestId: UUID,
    ): String? =
        jdbc
            .sql(
                "SELECT fingerprint FROM travel_plan_member_commands WHERE travel_plan_id = :plan AND member_id = :member AND request_id = :request",
            ).param("plan", planId)
            .param("member", memberId)
            .param("request", requestId)
            .query(String::class.java)
            .optional()
            .orElse(null)

    fun recordMemberCommand(
        planId: UUID,
        memberId: String,
        requestId: UUID,
        fingerprint: String,
    ) {
        jdbc
            .sql("INSERT INTO travel_plan_member_commands VALUES (:plan, :member, :request, :fingerprint)")
            .param("plan", planId)
            .param("member", memberId)
            .param("request", requestId)
            .param("fingerprint", fingerprint)
            .update()
    }

    fun offerOwnership(
        planId: UUID,
        memberId: String,
    ) {
        jdbc
            .sql(
                """INSERT INTO travel_ownership_offers (travel_plan_id, target_id) VALUES (:plan, :member)
               ON CONFLICT (travel_plan_id) DO UPDATE SET target_id = excluded.target_id""",
            ).param("plan", planId)
            .param("member", memberId)
            .update()
    }

    fun ownershipTarget(planId: UUID): String? =
        jdbc
            .sql("SELECT target_id FROM travel_ownership_offers WHERE travel_plan_id = :plan")
            .param("plan", planId)
            .query(String::class.java)
            .optional()
            .orElse(null)

    fun clearOwnershipOffer(planId: UUID) {
        jdbc.sql("DELETE FROM travel_ownership_offers WHERE travel_plan_id = :plan").param("plan", planId).update()
    }

    fun lastModifiedAt(planId: UUID): OffsetDateTime? =
        jdbc
            .sql("SELECT occurred_at FROM travel_plan_changes WHERE travel_plan_id = :plan ORDER BY revision DESC LIMIT 1")
            .param("plan", planId)
            .query(OffsetDateTime::class.java)
            .optional()
            .orElse(null)

    fun recentChanges(
        planId: UUID,
        memberId: String,
        throughRevision: Long,
    ): List<TravelChangeRow> =
        jdbc
            .sql(
                """SELECT change.revision, change.day_number, change.kind, change.visit_id, change.occurred_at,
                   visit.day_number AS current_day_number, visit.name AS visit_name
               FROM travel_plan_changes change
               LEFT JOIN travel_visits visit ON visit.travel_plan_id = change.travel_plan_id AND visit.id = change.visit_id
               WHERE change.travel_plan_id = :plan AND change.actor_id IS DISTINCT FROM :member AND change.revision <= :through
               AND change.revision > COALESCE((SELECT seen_revision FROM travel_plan_views
                   WHERE travel_plan_id = :plan AND member_id = :member), 0)
               ORDER BY change.revision DESC LIMIT 100""",
            ).param("plan", planId)
            .param("member", memberId)
            .param("through", throughRevision)
            .query {
                row,
                _,
                ->
                TravelChangeRow(
                    revision = row.getLong("revision"),
                    dayNumber = row.getObject("day_number", Int::class.javaObjectType),
                    kind = row.getString("kind"),
                    visitId = row.getObject("visit_id", UUID::class.java),
                    occurredAt = row.getObject("occurred_at", OffsetDateTime::class.java),
                    currentDayNumber = row.getObject("current_day_number", Int::class.javaObjectType),
                    visitName = row.getString("visit_name"),
                )
            }.list()

    fun markChangesSeen(
        planId: UUID,
        memberId: String,
        revision: Long,
        defaultDay: Int,
    ) {
        jdbc
            .sql(
                """INSERT INTO travel_plan_views (member_id, travel_plan_id, day_number, opened_at, seen_revision)
               VALUES (:member, :plan, :day, '-infinity', :revision)
               ON CONFLICT (member_id, travel_plan_id) DO UPDATE SET seen_revision =
               greatest(travel_plan_views.seen_revision, excluded.seen_revision)""",
            ).param("plan", planId)
            .param("member", memberId)
            .param("revision", revision)
            .param("day", defaultDay)
            .update()
    }

    @Scheduled(fixedDelayString = "PT1M")
    fun purgeExpiredVisitUndo() {
        jdbc.sql("DELETE FROM travel_visit_undo WHERE expires_at <= statement_timestamp()").update()
    }

    fun storeVisitUndo(
        planId: UUID,
        memberId: String,
        requestId: UUID,
        revision: Long,
        payload: String,
    ) {
        jdbc
            .sql(
                """INSERT INTO travel_visit_undo (travel_plan_id, member_id, request_id, revision, payload, expires_at) VALUES (:plan, :member, :request, :revision, :payload,
                statement_timestamp() + interval '10 minutes')""",
            ).param("plan", planId)
            .param("member", memberId)
            .param("request", requestId)
            .param("revision", revision)
            .param("payload", payload)
            .update()
    }

    fun takeVisitUndo(
        planId: UUID,
        memberId: String,
        requestId: UUID,
    ): Pair<Long, String>? =
        jdbc
            .sql(
                """DELETE FROM travel_visit_undo WHERE travel_plan_id = :plan AND member_id = :member
               AND request_id = :request AND expires_at > statement_timestamp() RETURNING revision, payload""",
            ).param("plan", planId)
            .param("member", memberId)
            .param("request", requestId)
            .query { row, _ -> row.getLong("revision") to row.getString("payload") }
            .optional()
            .orElse(null)

    fun recordChange(
        planId: UUID,
        memberId: String,
        revision: Long,
        dayNumber: Int?,
        kind: String,
        visitId: UUID? = null,
    ) {
        jdbc
            .sql(
                """INSERT INTO travel_plan_changes (travel_plan_id, revision, actor_id, day_number, kind, visit_id)
                   VALUES (:plan, :revision, :actor, :day, :kind, :visit)""",
            ).param("plan", planId)
            .param("revision", revision)
            .param("actor", memberId)
            .param("day", dayNumber)
            .param("kind", kind)
            .param("visit", visitId, java.sql.Types.OTHER)
            .update()
    }
}

data class TravelMemberRow(
    val id: String,
    val nickname: String?,
    val owner: Boolean,
)

data class TravelInviteRow(
    val planId: UUID?,
    val issuedBy: String?,
    val expiresAt: OffsetDateTime,
    val revokedAt: OffsetDateTime?,
)

data class TravelChangeRow(
    val revision: Long,
    val dayNumber: Int?,
    val kind: String,
    val visitId: UUID? = null,
    val occurredAt: OffsetDateTime? = null,
    val currentDayNumber: Int? = null,
    val visitName: String? = null,
)
