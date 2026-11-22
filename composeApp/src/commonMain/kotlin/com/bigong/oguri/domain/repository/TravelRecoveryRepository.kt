package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.TravelDraft
import com.bigong.oguri.domain.model.TravelPendingSave
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelRecovery
import com.bigong.oguri.domain.model.TravelVisitChange

interface TravelRecoveryRepository {
    suspend fun load(planId: String?): TravelRecovery

    suspend fun rememberPlan(plan: TravelPlan)

    suspend fun saveDraft(
        planId: String,
        draft: TravelDraft?,
    )

    suspend fun saveRequest(
        planId: String?,
        request: TravelPendingSave,
        draft: TravelDraft?,
    )

    suspend fun finishRequest(
        planId: String?,
        clearDraft: Boolean,
        conflictedOrder: TravelVisitChange.Reorder? = null,
    )

    suspend fun discardUnappliedOrder(planId: String)

    suspend fun forget(planId: String)
}
