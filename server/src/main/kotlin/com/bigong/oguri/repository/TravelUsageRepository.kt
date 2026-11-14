package com.bigong.oguri.repository

import com.bigong.oguri.domain.TravelPlan
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.ZoneId
import java.util.UUID

enum class TravelRecommendationSource { CITY, PERIOD }

enum class TravelRecommendationEntryKind { EXPOSED, CLICKED, LOGIN_RESUMED }

enum class TravelUsageEntry { CREATED, JOINED, EXISTING }

enum class TravelUsageActivity {
    PLAN_DISPLAYED,
    OVERVIEW_VIEWED,
    DAY_SELECTED,
    PIN_SELECTED,
    CHANGES_VIEWED,
    CHANGE_DAY_OPENED,
    DIRECTIONS_OPENED,
    NEXT_HOLIDAY_OPENED,
    VISIT_SAVE_FAILED,
    VISIT_SAVE_CONFLICT,
    VISIT_CONFLICT_RECOVERED,
}

enum class TravelUsageEdit { VISIT_ADDED, VISIT_EDITED, VISIT_REMOVED, DAY_REORDERED, VISIT_UNDONE, TRIP_UPDATED }

/** Called under the plan/member write lock, in the same transaction as the confirmed action. */
@Repository
class TravelUsageRepository(
    private val jdbc: JdbcClient,
) {
    fun recordRecommendationEntry(
        sessionId: UUID,
        source: TravelRecommendationSource,
        kind: TravelRecommendationEntryKind,
        signedIn: Boolean,
    ) {
        jdbc
            .sql(
                """
                INSERT INTO travel_recommendation_entries (session_id, source, kind, signed_in)
                VALUES (:session, :source, :kind, :signedIn)
                ON CONFLICT (session_id, source, kind, signed_in) DO NOTHING
                """,
            ).param("session", sessionId)
            .param("source", source.name)
            .param("kind", kind.name)
            .param("signedIn", signedIn)
            .update()
    }

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
        val recommendationSource =
            when {
                plan.sourceDestinationId != null -> TravelRecommendationSource.CITY
                plan.sourceStartDate != null -> TravelRecommendationSource.PERIOD
                else -> null
            }
        jdbc
            .sql(
                """
                INSERT INTO travel_usage_cohorts
                    (id, plan_key, travel_plan_id, member_id, entry_kind, entry_source,
                     days_until_start, initial_end_at, start_session_id, recommendation_source)
                VALUES (:id, :key, :plan, :member, :entry, :source,
                    CASE WHEN :entry = 'EXISTING' THEN NULL
                         ELSE CAST(:start AS date) - CAST(statement_timestamp() AT TIME ZONE :zone AS date) END,
                    :end, :session, :recommendationSource)
                ON CONFLICT (plan_key, member_id) DO NOTHING
                """,
            ).param("id", UUID.randomUUID())
            .param("key", planKey)
            .param("plan", plan.id)
            .param("member", memberId)
            .param("entry", entry.name)
            .param("source", if (recommendationSource == null) "DIRECT" else "RECOMMENDATION")
            .param("recommendationSource", recommendationSource?.name, java.sql.Types.VARCHAR)
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

    fun recordReminderOptOut(
        plan: TravelPlan,
        memberId: String,
    ) {
        start(plan, memberId, TravelUsageEntry.EXISTING)
        jdbc
            .sql(
                """
                UPDATE travel_usage_cohorts SET first_reminder_opt_out_at = statement_timestamp()
                WHERE travel_plan_id = :plan AND member_id = :member AND first_reminder_opt_out_at IS NULL
                """,
            ).param("plan", plan.id)
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
