package com.bigong.oguri.repository

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.sql.SQLException
import java.util.Properties
import java.util.UUID

@EnabledIfEnvironmentVariable(named = "TRAVEL_TEST_DATABASE_URL", matches = "jdbc:postgresql:.*")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TravelPostgresIntegrationTest {
    private val schema = "travel_test_" + UUID.randomUUID().toString().replace("-", "")
    private lateinit var dataSource: DriverManagerDataSource
    private lateinit var jdbc: JdbcClient
    private lateinit var changes: TravelCollaborationRepository
    private lateinit var legacy: Fixture

    @BeforeAll
    fun migrateExistingAndNewTravelData() {
        dataSource =
            DriverManagerDataSource(
                System.getenv("TRAVEL_TEST_DATABASE_URL"),
                System.getenv("TRAVEL_TEST_DATABASE_USER") ?: "postgres",
                System.getenv("TRAVEL_TEST_DATABASE_PASSWORD") ?: "postgres",
            )
        // Every test run owns a separate schema; never clean the database's default schema.
        Flyway
            .configure()
            .dataSource(dataSource)
            .schemas(schema)
            .defaultSchema(schema)
            .target("39")
            .load()
            .migrate()
        dataSource.setConnectionProperties(Properties().apply { setProperty("currentSchema", schema) })
        jdbc = JdbcClient.create(dataSource)
        legacy = fixture()
        jdbc
            .sql(
                """INSERT INTO travel_plan_changes (travel_plan_id, revision, actor_id, day_number, kind)
            VALUES (:plan, 1, :actor, 1, 'VISIT_ADDED')""",
            ).param("plan", legacy.plan)
            .param("actor", legacy.other)
            .update()
        Flyway
            .configure()
            .dataSource(dataSource)
            .schemas(schema)
            .defaultSchema(schema)
            .load()
            .migrate()
        changes = TravelCollaborationRepository(jdbc)
    }

    @AfterAll
    fun removeOnlyThisRunsSchema() {
        if (::dataSource.isInitialized) {
            Flyway
                .configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .cleanDisabled(false)
                .load()
                .clean()
        }
    }

    @Test
    fun `V40 적용은 기존 변경 내역을 보존한다`() {
        val history = changes.recentChanges(legacy.plan, legacy.owner, 1).single()
        assertEquals("VISIT_ADDED", history.kind)
        assertNull(history.visitId)
        assertNotNull(history.occurredAt)
        assertEquals(history.occurredAt, changes.lastModifiedAt(legacy.plan))
    }

    @Test
    fun `최근 변경은 현재 방문과 삭제 상태를 연결하고 확인 범위를 전진시킨다`() {
        val trip = fixture()
        changes.recordChange(trip.plan, trip.other, 1, 1, "VISIT_ADDED", trip.first)
        changes.recordChange(trip.plan, trip.owner, 2, 1, "VISIT_EDITED", trip.second)
        changes.recordChange(trip.plan, trip.other, 3, 1, "VISIT_REMOVED", trip.second)
        jdbc.sql("UPDATE travel_visits SET day_number = 2 WHERE id = :id").param("id", trip.first).update()
        jdbc.sql("DELETE FROM travel_visits WHERE id = :id").param("id", trip.second).update()
        val moved = changes.recentChanges(trip.plan, trip.owner, 2).single()
        assertEquals(trip.first, moved.visitId)
        assertEquals(2, moved.currentDayNumber)
        assertEquals("First place", moved.visitName)
        changes.markChangesSeen(trip.plan, trip.owner, 1, 1)
        changes.markChangesSeen(trip.plan, trip.owner, 0, 1)
        changes.recordChange(trip.plan, trip.other, 4, 2, "DAY_REORDERED")
        val removed = changes.recentChanges(trip.plan, trip.owner, 3).single()
        assertEquals(3L, removed.revision)
        assertEquals(trip.second, removed.visitId)
        assertNull(removed.currentDayNumber)
        assertNull(removed.visitName)
    }

    @Test
    fun `순서 교환은 같은 트랜잭션에서 허용하고 중복 순서는 커밋하지 못한다`() {
        val trip = fixture()
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            connection.prepareStatement("UPDATE travel_visits SET position = 1 - position WHERE travel_plan_id = ?").use {
                it.setObject(1, trip.plan)
                assertEquals(2, it.executeUpdate())
            }
            connection.commit()
            connection.prepareStatement("UPDATE travel_visits SET position = 0 WHERE travel_plan_id = ?").use {
                it.setObject(1, trip.plan)
                it.executeUpdate()
            }
            assertThrows(SQLException::class.java) { connection.commit() }
            connection.rollback()
        }
        assertEquals(
            1,
            jdbc
                .sql("SELECT position FROM travel_visits WHERE id = :id")
                .param("id", trip.first)
                .query(Int::class.javaObjectType)
                .single(),
        )
    }

    private fun fixture(): Fixture {
        val trip =
            Fixture(UUID.randomUUID(), "owner-" + UUID.randomUUID(), "other-" + UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        for (member in listOf(trip.owner, trip.other)) jdbc.sql("INSERT INTO members (id) VALUES (:id)").param("id", member).update()
        jdbc
            .sql(
                """INSERT INTO travel_plans (
            id, owner_id, creation_request_id, creation_fingerprint, city, time_zone, title, start_date, end_date)
            VALUES (:plan, :owner, :request, 'fingerprint', 'Seoul', 'Asia/Seoul', 'Integration trip', '2027-04-01', '2027-04-03')""",
            ).param("plan", trip.plan)
            .param("owner", trip.owner)
            .param("request", UUID.randomUUID())
            .update()
        listOf(trip.first, trip.second).forEachIndexed { position, id ->
            jdbc
                .sql(
                    """INSERT INTO travel_visits (id, travel_plan_id, day_number, position, name, revision)
                VALUES (:id, :plan, 1, :position, :name, 1)""",
                ).param("id", id)
                .param("plan", trip.plan)
                .param("position", position)
                .param("name", if (position == 0) "First place" else "Second place")
                .update()
        }
        return trip
    }

    private data class Fixture(
        val plan: UUID,
        val owner: String,
        val other: String,
        val first: UUID,
        val second: UUID,
    )
}
