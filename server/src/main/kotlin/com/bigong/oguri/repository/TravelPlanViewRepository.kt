package com.bigong.oguri.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class TravelPlanViewRepository(
    private val jdbc: JdbcClient,
) {
    fun record(
        memberId: String,
        planId: UUID,
        dayNumber: Int,
    ) {
        jdbc
            .sql(
                """
            INSERT INTO travel_plan_views (member_id, travel_plan_id, day_number, opened_at)
            VALUES (:memberId, :planId, :dayNumber, statement_timestamp())
            ON CONFLICT (member_id, travel_plan_id) DO UPDATE
            SET day_number = excluded.day_number, opened_at = excluded.opened_at
            WHERE travel_plan_views.opened_at <= excluded.opened_at
            """,
            ).param("memberId", memberId)
            .param("planId", planId)
            .param("dayNumber", dayNumber)
            .update()
    }

    fun lastDay(
        memberId: String,
        planId: UUID,
    ): Int? =
        jdbc
            .sql(
                "SELECT day_number FROM travel_plan_views WHERE member_id = :memberId AND travel_plan_id = :planId AND isfinite(opened_at)",
            ).param("memberId", memberId)
            .param("planId", planId)
            .query(Int::class.javaObjectType)
            .optional()
            .orElse(null)

    fun lastOpenedByPlan(memberId: String): Map<UUID, OffsetDateTime> =
        jdbc
            .sql("SELECT travel_plan_id, opened_at FROM travel_plan_views WHERE member_id = :memberId AND isfinite(opened_at)")
            .param("memberId", memberId)
            .query { row, _ -> row.getObject("travel_plan_id", UUID::class.java) to row.getObject("opened_at", OffsetDateTime::class.java) }
            .list()
            .toMap()
}
