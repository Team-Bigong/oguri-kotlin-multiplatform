package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.domain.model.TravelUsageActivity
import com.bigong.oguri.domain.model.TravelVisitChange

interface TravelPlanRepository {
    suspend fun recordActivity(
        planId: String,
        kind: TravelUsageActivity,
    )

    suspend fun list(): List<TravelPlanSummary>

    suspend fun resume(): TravelPlanSummary?

    suspend fun recordView(
        planId: String,
        dayNumber: Int,
    )

    suspend fun create(
        requestId: String,
        details: TravelPlanDetails,
    ): TravelPlanSummary

    suspend fun get(planId: String): TravelPlan

    suspend fun changeVisit(
        planId: String,
        requestId: String,
        change: TravelVisitChange,
    ): TravelPlan
}
