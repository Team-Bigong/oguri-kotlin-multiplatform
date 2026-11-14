package com.bigong.oguri.controller

import com.bigong.oguri.dto.request.CreateTravelPlanRequest
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.request.TravelPlanSourceRequest
import com.bigong.oguri.dto.request.TravelVisitRequest
import com.bigong.oguri.util.JwtTokenProvider
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import javax.sql.DataSource

@EnabledIfEnvironmentVariable(named = "TRAVEL_TEST_DATABASE_URL", matches = "jdbc:postgresql:.*")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TravelCollaborationHttpTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired private lateinit var dataSource: DataSource
    @Autowired private lateinit var json: ObjectMapper
    @Autowired private lateinit var tokens: JwtTokenProvider

    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    @AfterAll
    fun cleanTestSchema() {
        Flyway.configure().dataSource(dataSource).schemas(schema).cleanDisabled(false).load().clean()
    }

    @Test
    fun `초대 참여부터 편집 충돌과 멤버 제외까지 실제 HTTP로 검증한다`() {
        val owner = memberToken()
        val member = memberToken()
        val day = LocalDate.of(2027, 4, 1)
        val destinationId =
            JdbcClient.create(dataSource)
                .sql("SELECT id FROM destinations WHERE flight_time_minutes = 80 AND name = '후쿠오카'")
                .query(Int::class.javaObjectType)
                .single()
        val source = TravelPlanSourceRequest(destinationId, day, day)
        val creation = CreateTravelPlanRequest(UUID.randomUUID(), "Fukuoka", "Asia/Tokyo", "Trip", day, day, source)
        val created = request("POST", "/trips", 200, owner, creation)
        val tripId = created.get("id").asText()
        assertEquals(destinationId, created.get("source").get("destinationId").asInt())
        val trip = "/trips/$tripId"
        val token = request("POST", "$trip/invites", 200, owner).get("token").asText()
        val invite = mapOf("token" to token)
        assertEquals("Trip", request("POST", "/trip-invites/preview", 200, body = invite).get("title").asText())
        request("POST", "/trip-invites/join", 401, body = invite)
        request("GET", trip, 404, member)
        assertEquals(tripId, request("POST", "/trip-invites/join", 200, member, invite).get("planId").asText())

        val original = TravelVisitRequest("Museum", null, null, null, null)
        val added = request("POST", "$trip/commands", 200, owner, TravelPlanCommand.AddVisit(UUID.randomUUID(), 1, original, 0))
        val visit = added.get("visits").single()
        val visitId = UUID.fromString(visit.get("id").asText())
        val baseRevision = visit.get("revision").asLong()
        request("GET", "$trip?knownRevision=1", 204, member)

        val edit =
            TravelPlanCommand.EditVisit(UUID.randomUUID(), visitId, baseRevision, 1, original.copy(note = "Shared note"), original, 1, 0)
        request("POST", "$trip/commands", 200, owner, edit)
        val opposingEdit = edit.copy(requestId = UUID.randomUUID(), details = original.copy(note = "Other note"))
        val conflict = request("POST", "$trip/commands", 409, member, opposingEdit)
        assertEquals("VISIT_CHANGED", conflict.get("code").asText())
        val updated = request("GET", "$trip?knownRevision=1", 200, member)
        assertEquals("Shared note", updated.get("visits").single().get("note").asText())

        val memberId = tokens.getMemberId(member)
        request("DELETE", "$trip/members/$memberId", 204, owner)
        request("GET", "$trip?knownRevision=${updated.get("revision").asLong()}", 404, member)
        request("POST", "$trip/commands", 404, member, edit.copy(requestId = UUID.randomUUID()))
        assertEquals("INVITE_REVOKED", request("POST", "/trip-invites/join", 410, member, invite).get("code").asText())
    }

    private fun memberToken(): String {
        val id = "http-test-${UUID.randomUUID()}"
        JdbcClient.create(dataSource).sql("INSERT INTO members (id) VALUES (:id)").param("id", id).update()
        return tokens.createAccessToken(id)
    }

    private fun request(
        method: String,
        path: String,
        expected: Int,
        token: String? = null,
        body: Any? = null,
    ): JsonNode {
        val builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/api/v1$path")).timeout(Duration.ofSeconds(10))
        if (token != null) builder.header("Authorization", "Bearer $token")
        val publisher =
            if (body == null) {
                HttpRequest.BodyPublishers.noBody()
            } else {
                builder.header("Content-Type", "application/json")
                HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))
            }
        val response = client.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString())
        assertEquals(expected, response.statusCode(), "$method $path")
        return if (response.body().isBlank()) json.nullNode() else json.readTree(response.body())
    }

    companion object {
        private val schema = "travel_http_" + UUID.randomUUID().toString().replace("-", "")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("server.address") { "127.0.0.1" }
            registry.add("spring.datasource.url") { requireNotNull(System.getenv("TRAVEL_TEST_DATABASE_URL")) }
            registry.add("spring.datasource.username") { System.getenv("TRAVEL_TEST_DATABASE_USER") ?: "postgres" }
            registry.add("spring.datasource.password") { System.getenv("TRAVEL_TEST_DATABASE_PASSWORD") ?: "postgres" }
            registry.add("spring.datasource.hikari.schema") { schema }
            registry.add("spring.flyway.schemas") { schema }
            registry.add("spring.jpa.properties.hibernate.default_schema") { schema }
            registry.add("TRAVEL_WRITES_ENABLED") { "true" }
            registry.add("TRAVEL_INVITES_ENABLED") { "true" }
            registry.add("TRAVEL_REMINDERS_ENABLED") { "false" }
        }
    }
}
