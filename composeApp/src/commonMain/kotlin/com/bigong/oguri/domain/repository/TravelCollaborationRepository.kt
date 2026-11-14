package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.TravelInvitePreview
import com.bigong.oguri.domain.model.TravelMembers
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelRecentChanges

interface TravelCollaborationRepository {
    suspend fun members(planId: String): TravelMembers

    suspend fun issue(planId: String): String

    suspend fun revoke(planId: String)

    suspend fun remove(
        planId: String,
        memberId: String,
    )

    suspend fun leave(planId: String)

    suspend fun delete(planId: String)

    suspend fun updateDetails(
        planId: String,
        details: TravelPlanDetails,
        revision: Long,
        unassignedCount: Int,
    )

    suspend fun offerOwnership(
        planId: String,
        targetId: String,
    )

    suspend fun acceptOwnership(planId: String)

    suspend fun recentChanges(planId: String): TravelRecentChanges

    suspend fun markChangesSeen(
        planId: String,
        revision: Long,
    )

    suspend fun preview(token: String): TravelInvitePreview

    suspend fun join(token: String): String
}
