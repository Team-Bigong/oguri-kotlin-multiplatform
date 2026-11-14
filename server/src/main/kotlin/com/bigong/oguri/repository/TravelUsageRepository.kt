package com.bigong.oguri.repository

import com.bigong.oguri.domain.TravelPlan
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.ZoneId
import java.util.UUID

enum class TravelUsageEntry { CREATED, JOINED, EXISTING }

enum class TravelUsageActivity { PLAN_DISPLAYED, OVERVIEW_VIEWED, DAY_SELECTED, PIN_SELECTED, CHANGES_VIEWED, DIRECTIONS_OPENED }

enum class TravelUsageEdit { VISIT_ADDED, VISIT_EDITED, VISIT_REMOVED, DAY_REORDERED, VISIT_UNDONE, TRIP_UPDATED }

/** Called under the plan/member write lock, in the same transaction as the confirmed action. */
@Repository
class TravelUsageRepository(
    private val jdbc: JdbcClient,
) {
    fun start(
        plan: TravelPlan,
        memberId: String,
        entry: TravelUsageEntry,
        sessionId: UUID? = null,
    ) {
        val planKey =
            jdbc
                .sql("SELECT plan_key FROM travel_usage_cohorts WHERE travel_plan_id = :plan LIMIT 1")
                .param("plan", plan.id)
                .query(UUID::class.java)
                .optional()
                .orElseGet(UUID::randomUUID)
        val zone = ZoneId.of(plan.timeZone)
        jdbc
            .sql(
                """
                INSERT INTO travel_usage_cohorts
                    (id, plan_key, travel_plan_id, member_id, entry_kind, entry_source, days_until_start, initial_end_at, start_session_id)
                VALUES (:id, :key, :plan, :member, :entry, :source,
                    CASE WHEN :entry = 'EXISTING' THEN NULL
                         ELSE CAST(:start AS date) - CAST(statement_timestamp() AT TIME ZONE :zone AS date) END,
                    :end, :session)
                ON CONFLICT (plan_key, member_id) DO NOTHING
                """,
            ).param("id", UUID.randomUUID())
            .param("key", planKey)
            .param("plan", plan.id)
            .param("member", memberId)
            .param("entry", entry.name)
            .param("source", if (plan.sourceDestinationId == null && plan.sourceStartDate == null) "DIRECT" else "RECOMMENDATION")
            .param("session", sessionId, java.sql.Types.OTHER)
            .param("start", plan.startDate)
            .param("zone", plan.timeZone)
            .param(
                "end",
                plan.endDate
                    .plusDays(1)
                    .atStartOfDay(zone)
                    .toOffsetDateTime(),
            ).update()
    }

    fun recordEdit(
        plan: TravelPlan,
        memberId: String,
        kind: TravelUsageEdit,
        sessionId: UUID? = null,
    ) {
        // Existing installations have no trustworthy historical start/session data.
        // Do not turn the first observed edit into a new creation/participation.
        start(plan, memberId, TravelUsageEntry.EXISTING)
        jdbc
            .sql(
                """
                INSERT INTO travel_usage_edits
                    (cohort_id, plan_key, revision, kind, has_dated_visit, session_id, after_participation)
                SELECT c.id, c.plan_key, :revision, :kind, :dated, :session,
                    (:owner <> :member OR
                     EXISTS (SELECT 1 FROM travel_plan_members m
                             WHERE m.travel_plan_id = :plan AND m.member_id <> :member) OR
                     EXISTS (SELECT 1 FROM travel_usage_cohorts other
                             WHERE other.plan_key = c.plan_key AND other.id <> c.id))
                FROM travel_usage_cohorts c WHERE c.travel_plan_id = :plan AND c.member_id = :member
                ON CONFLICT (plan_key, revision) DO NOTHING
                """,
            ).param("session", sessionId, java.sql.Types.OTHER)
            .param("owner", plan.ownerId)
            .param("revision", plan.revision)
            .param("kind", kind.name)
            .param("dated", plan.orderedVisits().any { it.dayNumber > 0 })
            .param("plan", plan.id)
            .param("member", memberId)
            .update()
    }

    fun recordActivity(
        plan: TravelPlan,
        memberId: String,
        sessionId: UUID,
        kind: TravelUsageActivity,
    ) {
        start(plan, memberId, TravelUsageEntry.EXISTING)
        jdbc
            .sql(
                """
            INSERT INTO travel_usage_activity (cohort_id, session_id, kind)
            SELECT id, :session, :kind FROM travel_usage_cohorts
            WHERE travel_plan_id = :plan AND member_id = :member
            ON CONFLICT (cohort_id, session_id, kind) DO NOTHING
            """,
            ).param("session", sessionId)
            .param("kind", kind.name)
            .param("plan", plan.id)
            .param("member", memberId)
            .update()
    }
}
