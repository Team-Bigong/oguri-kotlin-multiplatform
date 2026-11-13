package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.TravelReminder
import com.bigong.oguri.domain.model.TravelReminderSettings
import com.bigong.oguri.domain.model.TravelReminderTime

interface TravelReminderRepository {
    suspend fun syncDevice()

    suspend fun settings(planId: String): TravelReminderSettings

    suspend fun schedule(
        planId: String,
        settings: TravelReminderSettings,
        time: TravelReminderTime,
    ): TravelReminder

    suspend fun cancel(
        planId: String,
        reminderId: String,
    )

    suspend fun open(reminderId: String): String
}
