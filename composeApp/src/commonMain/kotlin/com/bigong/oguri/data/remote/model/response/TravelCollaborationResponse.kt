package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
internal data class TravelMemberResponse(
    val id: String,
    val nickname: String?,
    val owner: Boolean,
)

@Serializable
internal data class TravelMembersResponse(
    val currentMemberId: String,
    val members: List<TravelMemberResponse>,
    val ownershipTarget: String? = null,
    val hasActiveInvite: Boolean = false,
    val inviteExpiresAt: String? = null,
    val inviteToken: String? = null,
)

@Serializable
internal data class IssuedTravelInviteResponse(
    val token: String,
    val expiresAt: String? = null,
)

@Serializable
internal data class JoinedTravelPlanResponse(
    val planId: String,
)

@Serializable
internal data class TravelInvitePreviewResponse(
    val title: String,
    val startDate: String,
    val endDate: String,
    val invitedBy: String?,
    val city: String? = null,
    val memberCount: Int? = null,
    val existingPlanId: String? = null,
    val cities: List<String> = emptyList(),
)

@Serializable
internal data class TravelChangeResponse(
    val revision: Long,
    val dayNumber: Int?,
    val kind: String,
    val visitId: String? = null,
    val occurredAt: String? = null,
    val currentDayNumber: Int? = null,
    val visitName: String? = null,
)

@Serializable
internal data class TravelRecentChangesResponse(
    val throughRevision: Long,
    val changes: List<TravelChangeResponse>,
    val startDate: String? = null,
    val endDate: String? = null,
    val timeZone: String? = null,
)
