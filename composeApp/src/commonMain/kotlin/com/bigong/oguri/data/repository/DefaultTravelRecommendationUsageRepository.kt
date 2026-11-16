package com.bigong.oguri.data.repository

import com.bigong.oguri.core.analytics.travelUsageSession
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.core.network.OmitAuthentication
import com.bigong.oguri.core.network.RequestAccessToken
import com.bigong.oguri.data.remote.AuthRequestExecutor
import com.bigong.oguri.domain.repository.TravelRecommendationEntry
import com.bigong.oguri.domain.repository.TravelRecommendationSource
import com.bigong.oguri.domain.repository.TravelRecommendationUsageRepository
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Inject
class DefaultTravelRecommendationUsageRepository(
    private val httpClient: HttpClient,
    private val auth: AuthRequestExecutor,
) : TravelRecommendationUsageRepository {
    override suspend fun record(
        source: TravelRecommendationSource,
        entry: TravelRecommendationEntry,
    ) {
        val session = AuthTokenStore.currentSession()
        val id = travelUsageSession.current(session.version) ?: return
        if (AuthTokenStore.currentSession().version != session.version) return
        if (session.tokens == null) {
            if (entry != TravelRecommendationEntry.LOGIN_RESUMED) send(id, source, entry, token = null)
        } else {
            auth.executeForSession(session.version) { token -> send(id, source, entry, token) }
        }
    }

    private suspend fun send(
        sessionId: String,
        source: TravelRecommendationSource,
        entry: TravelRecommendationEntry,
        token: String?,
    ) {
        httpClient.post("$BASE_URL/api/v1/travel-recommendation-entries") {
            if (token == null) attributes.put(OmitAuthentication, true) else attributes.put(RequestAccessToken, token)
            header("X-Travel-Session", sessionId)
            contentType(ContentType.Application.Json)
            setBody(EntryRequest(source.name, entry.name))
        }
    }

    @Serializable
    private data class EntryRequest(
        val source: String,
        val kind: String,
    )
}
