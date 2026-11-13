package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class TravelMember(
    val id: String,
    val nickname: String?,
    val owner: Boolean,
)

data class TravelMembers(
    val currentMemberId: String,
    val members: List<TravelMember>,
    val ownershipTarget: String?,
) {
    val isOwner: Boolean get() = members.any { it.id == currentMemberId && it.owner }
}

data class TravelInvitePreview(
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val invitedBy: String?,
)

data class TravelChange(
    val revision: Long,
    val dayNumber: Int?,
    val kind: String,
)

data class TravelRecentChanges(
    val throughRevision: Long,
    val changes: List<TravelChange>,
)
