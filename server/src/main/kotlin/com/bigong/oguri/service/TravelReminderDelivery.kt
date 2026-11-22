package com.bigong.oguri.service

import com.bigong.oguri.repository.TravelReminderRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Component
class TravelReminderDelivery(
    private val objectMapper: ObjectMapper,
    @param:Value("\${TRAVEL_REMINDERS_ENABLED:false}") private val remindersEnabled: Boolean,
    @param:Value("\${TRAVEL_REMINDER_FIREBASE_PROJECT_ID:}") private val projectId: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
    private val credentials: GoogleCredentials by lazy {
        GoogleCredentials.getApplicationDefault().createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }
    val configured: Boolean get() = remindersEnabled && PROJECT_ID.matches(projectId)

    fun prepare(): String {
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    fun send(id: UUID, token: String, authorization: String): Boolean {
        val body = mapOf(
            "message" to mapOf(
                "token" to token,
                "data" to mapOf("travelReminderId" to id.toString()),
                "android" to mapOf(
                    "ttl" to "900s",
                    "notification" to mapOf(
                        "title_loc_key" to "travel_reminder_push_title",
                        "body_loc_key" to "travel_reminder_push_body",
                        "channel_id" to "travel_reminders",
                        "tag" to id.toString(),
                    ),
                ),
                "apns" to mapOf(
                    "headers" to mapOf("apns-expiration" to Instant.now().plusSeconds(900).epochSecond.toString()),
                    "payload" to mapOf("aps" to mapOf(
                        "alert" to mapOf("title-loc-key" to "TRAVEL_REMINDER_PUSH_TITLE", "loc-key" to "TRAVEL_REMINDER_PUSH_BODY"),
                        "sound" to "default",
                    )),
                ),
            ),
        )
        val request = HttpRequest.newBuilder(URI.create("https://fcm.googleapis.com/v1/projects/$projectId/messages:send"))
            .timeout(Duration.ofSeconds(8))
            .header("Authorization", "Bearer $authorization")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() in 200..299
    }

    private companion object {
        val PROJECT_ID: Regex = Regex("[a-z][a-z0-9-]{4,28}[a-z0-9]")
    }
}

@Component
class TravelReminderDispatcher(
    private val reminders: TravelReminderRepository,
    private val service: TravelReminderService,
    private val delivery: TravelReminderDelivery,
    private val metrics: MeterRegistry,
) {
    @Scheduled(fixedDelayString = "PT15S")
    fun sendDue() {
        reminders.expire()
        if (!delivery.configured) return
        val due = reminders.due()
        if (due.isEmpty()) return
        val authorization = try {
            delivery.prepare()
        } catch (failure: Exception) {
            metrics.counter("oguri.travel.reminder.authorization.failures").increment()
            logger.warn("Could not authorize travel reminder delivery ({})", failure.javaClass.simpleName)
            return
        }
        for (candidate in due) {
            val token = service.beginHandoff(candidate) ?: continue
            val outcome =
                try {
                    if (delivery.send(candidate.id, token, authorization)) "accepted" else "rejected"
                } catch (interrupted: InterruptedException) {
                    recordHandoff("interrupted")
                    Thread.currentThread().interrupt()
                    return
                } catch (failure: Exception) {
                    logger.warn("Travel reminder delivery failed ({})", failure.javaClass.simpleName)
                    "failed"
                }
            recordHandoff(outcome)
            reminders.finishHandoff(candidate.id, outcome == "accepted")
        }
    }

    private fun recordHandoff(outcome: String) {
        metrics.counter("oguri.travel.reminder.handoff", "outcome", outcome).increment()
    }

    private companion object {
        val logger = LoggerFactory.getLogger(TravelReminderDispatcher::class.java)
    }
}
