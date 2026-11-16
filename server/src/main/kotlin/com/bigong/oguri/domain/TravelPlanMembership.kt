package com.bigong.oguri.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "travel_plan_members")
@IdClass(TravelPlanMembershipId::class)
class TravelPlanMembership(
    @Id
    @jakarta.persistence.Column(name = "travel_plan_id")
    val planId: UUID,
    @Id
    val memberId: String,
)

data class TravelPlanMembershipId(
    val planId: UUID? = null,
    val memberId: String? = null,
) : Serializable
