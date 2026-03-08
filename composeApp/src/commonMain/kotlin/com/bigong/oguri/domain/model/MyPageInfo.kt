package com.bigong.oguri.domain.model

data class MyPageInfo(
    val nickname: String,
    val remainingLeaveDays: Int,
    val preferredLeaveDays: Int,
    val selectedPeriods: List<MyPageSelectedPeriod>,
    val savedPlaces: List<Place>,
)
