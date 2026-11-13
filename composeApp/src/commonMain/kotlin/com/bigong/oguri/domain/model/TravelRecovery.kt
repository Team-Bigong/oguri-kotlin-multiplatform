package com.bigong.oguri.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TravelDraft(
    val dayNumber: Int,
    val original: TravelVisit?,
    val structureRevision: Long,
    val name: String,
    val location: String,
    val localTime: String,
    val note: String,
)

@Serializable
sealed interface TravelPendingSave {
    val requestId: String

    @Serializable
    data class Create(
        override val requestId: String,
        val details: TravelPlanDetails,
    ) : TravelPendingSave

    @Serializable
    data class Visit(
        override val requestId: String,
        val change: TravelVisitChange,
        val selectedDay: Int,
    ) : TravelPendingSave
}

data class TravelRecovery(
    val confirmedPlan: TravelPlan? = null,
    val draft: TravelDraft? = null,
    val pending: TravelPendingSave? = null,
)
