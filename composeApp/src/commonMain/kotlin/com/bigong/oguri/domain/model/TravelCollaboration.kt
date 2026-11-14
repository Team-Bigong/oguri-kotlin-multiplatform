package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

data class TravelMember(
    val id: String,
    val nickname: String?,
    val owner: Boolean,
)

data class TravelMembers(
    val currentMemberId: String,
    val members: List<TravelMember>,
    val ownershipTarget: String?,
    val hasActiveInvite: Boolean = false,
) {
    val isOwner: Boolean get() = members.any { it.id == currentMemberId && it.owner }
}

data class TravelInvitePreview(
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val invitedBy: String?,
    val city: String? = null,
    val memberCount: Int? = null,
)

data class TravelChange(
    val revision: Long,
    val dayNumber: Int?,
    val kind: String,
    val visitId: String? = null,
    val occurredAt: Instant? = null,
    val currentDayNumber: Int? = null,
    val visitName: String? = null,
) {
    val isVisitRemoved: Boolean get() = visitId != null && currentDayNumber == null

    fun destinationDay(dates: TravelDates): Int? =
        (if (visitId != null) currentDayNumber else dayNumber)?.takeIf { it in 0..dates.dayCount }
}

data class TravelRecentChanges(
    val throughRevision: Long,
    val changes: List<TravelChange>,
    val dates: TravelDates? = null,
    val timeZone: TimeZone? = null,
)
