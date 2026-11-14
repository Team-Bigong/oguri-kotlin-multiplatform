package com.bigong.oguri.domain

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "travel_visits")
class TravelVisit(
    @Id
    val id: UUID,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    val plan: TravelPlan,
    dayNumber: Int,
    position: Int,
    details: TravelVisitDetails,
    revision: Long,
) {
    @Column(nullable = false)
    var dayNumber: Int = dayNumber
        private set

    @Column(nullable = false)
    var position: Int = position
        private set

    @Column(length = 150)
    var name: String? = details.name
        private set

    @Column(length = 250)
    var location: String? = details.location
        private set

    @Column(length = 512)
    val googlePlaceId: String? = details.googlePlaceId

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var category: TravelVisitCategory? = details.category
        private set

    var localTime: LocalTime? = details.localTime
        private set

    @Column(length = 1000)
    var note: String? = details.note
        private set

    @Column(nullable = false)
    var revision: Long = revision
        private set

    fun details(): TravelVisitDetails = TravelVisitDetails(name, location, localTime, note, googlePlaceId, category)

    internal fun merge(
        base: TravelVisitDetails,
        proposed: TravelVisitDetails,
    ): TravelVisitDetails {
        require(googlePlaceId == base.googlePlaceId && googlePlaceId == proposed.googlePlaceId)

        fun <Field> changed(
            previous: Field,
            next: Field,
            current: Field,
        ): Field {
            if (previous == next) return current
            if (previous != current) throw TravelPlanConflict("VISIT_CHANGED")
            return next
        }
        return TravelVisitDetails(
            name = changed(base.name, proposed.name, name),
            location = changed(base.location, proposed.location, location),
            localTime = changed(base.localTime, proposed.localTime, localTime),
            note = changed(base.note, proposed.note, note),
            googlePlaceId = googlePlaceId,
            category = changed(base.category, proposed.category, category),
        )
    }

    internal fun edit(
        details: TravelVisitDetails,
        revision: Long,
    ) {
        require(googlePlaceId == details.googlePlaceId)
        name = details.name
        location = details.location
        localTime = details.localTime
        note = details.note
        category = details.category
        this.revision = revision
    }

    internal fun moveTo(
        dayNumber: Int,
        position: Int,
    ) {
        this.dayNumber = dayNumber
        this.position = position
    }
}

data class TravelVisitDetails(
    val name: String?,
    val location: String?,
    val localTime: LocalTime?,
    val note: String?,
    val googlePlaceId: String? = null,
    val category: TravelVisitCategory? = null,
) {
    init {
        if (googlePlaceId == null) {
            require(!name.isNullOrBlank() && name.length <= MAX_NAME_LENGTH)
        } else {
            require(GOOGLE_PLACE_ID.matches(googlePlaceId))
            require(name == null || (name.isNotBlank() && name.length <= MAX_NAME_LENGTH))
        }
        require(location == null || location.length <= MAX_LOCATION_LENGTH)
        require(note == null || note.length <= MAX_NOTE_LENGTH)
        require(localTime == null || (localTime.second == 0 && localTime.nano == 0))
    }

    private companion object {
        val GOOGLE_PLACE_ID: Regex = Regex("[A-Za-z0-9_-]{1,512}")
        const val MAX_NAME_LENGTH: Int = 150
        const val MAX_LOCATION_LENGTH: Int = 250
        const val MAX_NOTE_LENGTH: Int = 1000
    }
}
