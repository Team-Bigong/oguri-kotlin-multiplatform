package com.bigong.oguri.repository

import com.bigong.oguri.domain.TravelPlan
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface TravelPlanRepository : JpaRepository<TravelPlan, UUID> {
    fun findAllByOwnerIdOrderByStartDateAscIdAsc(ownerId: String): List<TravelPlan>

    fun findByCreatorIdAndCreationRequestId(
        creatorId: String,
        creationRequestId: UUID,
    ): TravelPlan?

    @Query(
        """select plan from TravelPlan plan
            where plan.ownerId = :memberId or exists (
                select membership from TravelPlanMembership membership
                where membership.planId = plan.id and membership.memberId = :memberId
            ) order by plan.startDate, plan.id""",
    )
    fun findAccessible(memberId: String): List<TravelPlan>

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select plan from TravelPlan plan where plan.id = :id")
    fun findForRead(id: UUID): TravelPlan?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select plan from TravelPlan plan where plan.id = :id")
    fun findForUpdate(id: UUID): TravelPlan?
}
