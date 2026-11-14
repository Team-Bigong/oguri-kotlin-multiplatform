package com.bigong.oguri.repository

import com.bigong.oguri.domain.Member
import com.bigong.oguri.domain.TravelPlan
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.domain.TravelPlanMembership
import com.bigong.oguri.domain.TravelVisit
import com.bigong.oguri.dto.request.CreateTravelPlanRequest
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.request.TravelVisitRequest
import com.bigong.oguri.dto.response.TravelPlanResponse
import com.bigong.oguri.dto.response.TravelVisitResponse
import com.bigong.oguri.service.TravelCollaborationService
import com.bigong.oguri.service.TravelPlanService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManagerFactory
import org.flywaydb.core.Flyway
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.Mockito.mock
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.SharedEntityManagerCreator
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalTime
import java.util.Properties
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@EnabledIfEnvironmentVariable(named = "TRAVEL_TEST_DATABASE_URL", matches = "jdbc:postgresql:.*")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TravelTransactionIntegrationTest {
    private val schema = "travel_tx_" + UUID.randomUUID().toString().replace("-", "")
    private lateinit var dataSource: DriverManagerDataSource
    private lateinit var entityManagerFactory: EntityManagerFactory
    private lateinit var transaction: TransactionTemplate
    private lateinit var jdbc: JdbcClient
    private lateinit var service: TravelPlanService
    private lateinit var collaboration: TravelCollaborationService
    private lateinit var changes: TravelCollaborationRepository

    @BeforeAll
    fun prepareRealRepositoriesAndTransactions() {
        dataSource =
            DriverManagerDataSource(
                System.getenv("TRAVEL_TEST_DATABASE_URL"),
                System.getenv("TRAVEL_TEST_DATABASE_USER") ?: "postgres",
                System.getenv("TRAVEL_TEST_DATABASE_PASSWORD") ?: "postgres",
            )
        Flyway
            .configure()
            .dataSource(dataSource)
            .schemas(schema)
            .defaultSchema(schema)
            .load()
            .migrate()
        dataSource.setConnectionProperties(Properties().apply { setProperty("currentSchema", schema) })
        jdbc = JdbcClient.create(dataSource)
        val factory =
            LocalContainerEntityManagerFactoryBean().apply {
                setDataSource(this@TravelTransactionIntegrationTest.dataSource)
                setJpaVendorAdapter(HibernateJpaVendorAdapter())
                setManagedTypes(
                    PersistenceManagedTypes.of(
                        Member::class.java.name,
                        TravelPlan::class.java.name,
                        TravelVisit::class.java.name,
                        TravelPlanMembership::class.java.name,
                    ),
                )
                setJpaPropertyMap(
                    mapOf(
                        "hibernate.hbm2ddl.auto" to "validate",
                        "hibernate.physical_naming_strategy" to CamelCaseToUnderscoresNamingStrategy::class.java.name,
                    ),
                )
                afterPropertiesSet()
            }
        entityManagerFactory = requireNotNull(factory.`object`)
        transaction =
            TransactionTemplate(
                JpaTransactionManager(entityManagerFactory).apply {
                    setDataSource(this@TravelTransactionIntegrationTest.dataSource)
                    afterPropertiesSet()
                },
            ).apply { timeout = 10 }
        val repositories = JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory))
        val plans = repositories.getRepository(TravelPlanRepository::class.java)
        val members = repositories.getRepository(MemberRepository::class.java)
        changes = TravelCollaborationRepository(jdbc)
        service =
            TravelPlanService(
                plans,
                members,
                ObjectMapper().findAndRegisterModules(),
                mock(DestinationRepository::class.java),
                TravelPlanViewRepository(jdbc),
                changes,
            )
        collaboration = TravelCollaborationService(service, plans, members, changes, TravelReminderRepository(jdbc), 7)
    }

    @AfterAll
    fun closeOnlyOwnedResources() {
        if (::entityManagerFactory.isInitialized) entityManagerFactory.close()
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
    fun `동시에 재시도한 생성 요청은 하나의 여행만 커밋한다`() {
        val member = member()
        assertFalse(
            jdbc
                .sql("SELECT onboarding_completed FROM members WHERE id = :id")
                .param("id", member)
                .query(Boolean::class.javaObjectType)
                .single(),
        )
        val request = creation()
        val first = AtomicReference<UUID>()
        val second = AtomicReference<UUID>()
        assertNull(
            overlap(
                { first.set(service.create(member, request).id) },
                { second.set(service.create(member, request).id) },
            ),
        )
        assertEquals(first.get(), second.get())
        assertEquals(
            1L,
            jdbc
                .sql("SELECT count(*) FROM travel_plans WHERE creator_id = :member AND creation_request_id = :request")
                .param("member", member)
                .param("request", request.requestId)
                .query(Long::class.javaObjectType)
                .single(),
        )
    }

    @Test
    fun `두 멤버의 동시 추가는 방문과 순서와 기록을 모두 보존한다`() {
        val trip = fixture()
        assertNull(
            overlap(
                { service.execute(trip.owner, trip.id, add("First")) },
                { service.execute(trip.other, trip.id, add("Second")) },
            ),
        )
        val saved = read(trip)
        assertEquals(listOf("First", "Second"), saved.visits.map { it.name })
        assertEquals(
            2,
            saved.visits
                .map { it.id }
                .distinct()
                .size,
        )
        assertEquals(
            listOf(0, 1),
            jdbc
                .sql("SELECT position FROM travel_visits WHERE travel_plan_id = :id ORDER BY position")
                .param("id", trip.id)
                .query(Int::class.javaObjectType)
                .list(),
        )
        assertEquals(2L, count("travel_plan_changes", trip.id))
        assertEquals(2L, count("travel_plan_member_commands", trip.id))
    }

    @Test
    fun `저장 응답을 기다리다 재전송한 명령은 방문과 기록을 복제하지 않는다`() {
        val trip = fixture()
        val command = add("Only once")
        assertNull(
            overlap(
                { service.execute(trip.owner, trip.id, command) },
                { service.execute(trip.owner, trip.id, command) },
            ),
        )
        assertEquals(listOf("Only once"), read(trip).visits.map { it.name })
        assertEquals(1L, count("travel_plan_changes", trip.id))
        assertEquals(1L, count("travel_plan_member_commands", trip.id))
    }

    @Test
    fun `같은 메모의 동시 수정은 충돌한 요청의 기록까지 롤백한다`() {
        val trip = fixture(1)
        val visit = read(trip).visits.single()
        val rejected = edit(visit, note = "Second")
        val failure =
            overlap(
                { service.execute(trip.owner, trip.id, edit(visit, note = "First")) },
                { service.execute(trip.other, trip.id, rejected) },
            )
        assertInstanceOf(TravelPlanConflict::class.java, failure)
        assertEquals("First", read(trip).visits.single().note)
        assertNull(changes.memberCommand(trip.id, trip.other, rejected.requestId))
        assertEquals(2L, count("travel_plan_changes", trip.id))
    }

    @Test
    fun `동시 독립 필드 수정은 최신 메모와 새 시각을 함께 저장한다`() {
        val trip = fixture(1)
        val visit = read(trip).visits.single()
        assertNull(
            overlap(
                { service.execute(trip.owner, trip.id, edit(visit, note = "Shared note")) },
                { service.execute(trip.other, trip.id, edit(visit, time = LocalTime.of(10, 30))) },
            ),
        )
        val saved = read(trip).visits.single()
        assertEquals("Shared note", saved.note)
        assertEquals("10:30", saved.localTime)
        assertEquals(3L, count("travel_plan_changes", trip.id))
    }

    @Test
    fun `같은 기준의 동시 재정렬은 첫 순서를 보존하고 두 번째를 거절한다`() {
        val trip = fixture(3)
        val before = read(trip)
        val reversed = before.visits.map { it.id }.reversed()

        fun reorder(ids: List<UUID>) = TravelPlanCommand.ReorderDay(UUID.randomUUID(), 1, before.dayOrderRevisions.getValue(1), ids, 0)
        assertInstanceOf(
            TravelPlanConflict::class.java,
            overlap(
                { service.execute(trip.owner, trip.id, reorder(reversed)) },
                { service.execute(trip.other, trip.id, reorder(before.visits.map { it.id })) },
            ),
        )
        assertEquals(reversed, read(trip).visits.map { it.id })
        assertEquals(4L, count("travel_plan_changes", trip.id))
    }

    @Test
    fun `삭제와 겹친 수정은 방문을 되살리거나 새 기록을 만들지 않는다`() {
        val trip = fixture(1)
        val visit = read(trip).visits.single()
        val rejected = edit(visit, note = "Too late")
        assertInstanceOf(
            TravelPlanConflict::class.java,
            overlap(
                { service.execute(trip.owner, trip.id, TravelPlanCommand.RemoveVisit(UUID.randomUUID(), visit.id, visit.revision, 0)) },
                { service.execute(trip.other, trip.id, rejected) },
            ),
        )
        assertTrue(read(trip).visits.isEmpty())
        assertNull(changes.memberCommand(trip.id, trip.other, rejected.requestId))
        assertEquals(2L, count("travel_plan_changes", trip.id))
    }

    @Test
    fun `멤버 제외가 커밋된 뒤 대기하던 저장은 권한을 다시 확인한다`() {
        val trip = fixture()
        val rejected = add("Not allowed")
        val failure =
            overlap(
                { collaboration.remove(trip.owner, trip.id, trip.other) },
                { service.execute(trip.other, trip.id, rejected) },
            )
        assertEquals(HttpStatus.NOT_FOUND, assertInstanceOf(ResponseStatusException::class.java, failure).statusCode)
        assertTrue(read(trip).visits.isEmpty())
        assertNull(changes.memberCommand(trip.id, trip.other, rejected.requestId))
        assertEquals(0L, count("travel_plan_changes", trip.id))
    }

    @Test
    fun `커밋 전 실패는 JPA 변경과 JDBC 요청 기록을 함께 취소한다`() {
        val trip = fixture()
        val command = add("Rolled back")
        assertThrows(IllegalStateException::class.java) {
            tx {
                service.execute(trip.owner, trip.id, command)
                throw IllegalStateException("Abort after writing history")
            }
        }
        assertTrue(read(trip).visits.isEmpty())
        assertNull(changes.memberCommand(trip.id, trip.owner, command.requestId))
        assertEquals(0L, count("travel_plan_changes", trip.id))
        tx { service.execute(trip.owner, trip.id, command) }
        assertEquals(listOf("Rolled back"), read(trip).visits.map { it.name })
    }

    private fun fixture(visits: Int = 0): Fixture {
        val owner = member()
        val other = member()
        val id = tx { service.create(owner, creation()).id }
        jdbc
            .sql("INSERT INTO travel_plan_members (travel_plan_id, member_id) VALUES (:id, :member)")
            .param("id", id)
            .param("member", other)
            .update()
        repeat(visits) { index -> tx { service.execute(owner, id, add("Visit $index")) } }
        return Fixture(id, owner, other)
    }

    private fun member(): String =
        ("tx-" + UUID.randomUUID()).also { id ->
            jdbc.sql("INSERT INTO members (id) VALUES (:id)").param("id", id).update()
        }

    private fun creation(): CreateTravelPlanRequest =
        CreateTravelPlanRequest(
            UUID.randomUUID(),
            "Seoul",
            "Asia/Seoul",
            "Trip",
            LocalDate.of(2027, 4, 1),
            LocalDate.of(2027, 4, 3),
            null,
        )

    private fun add(name: String): TravelPlanCommand.AddVisit =
        TravelPlanCommand.AddVisit(
            UUID.randomUUID(),
            1,
            TravelVisitRequest(name, null, null, null, null),
            0,
        )

    private fun edit(
        visit: TravelVisitResponse,
        note: String? = visit.note,
        time: LocalTime? = null,
    ): TravelPlanCommand.EditVisit {
        val base = TravelVisitRequest(visit.name, visit.location, visit.localTime?.let(LocalTime::parse), visit.note, visit.googlePlaceId)
        return TravelPlanCommand.EditVisit(
            UUID.randomUUID(),
            visit.id,
            visit.revision,
            visit.dayNumber,
            base.copy(note = note, localTime = time ?: base.localTime),
            base,
            visit.dayNumber,
            0,
        )
    }

    private fun read(trip: Fixture): TravelPlanResponse = tx { service.get(trip.owner, trip.id) }

    private fun count(
        table: String,
        plan: UUID,
    ): Long {
        require(table in setOf("travel_plans", "travel_plan_changes", "travel_plan_member_commands"))
        val column = if (table == "travel_plans") "id" else "travel_plan_id"
        return jdbc
            .sql("SELECT count(*) FROM $table WHERE $column = :id")
            .param("id", plan)
            .query(Long::class.javaObjectType)
            .single()
    }

    private fun <T : Any> tx(action: () -> T): T =
        requireNotNull(
            transaction.execute {
                jdbc.sql("SET LOCAL statement_timeout = '8s'").update()
                action()
            },
        )

    // Hold the first commit, then prove PostgreSQL has blocked the second request on a lock.
    // Merely starting two threads is insufficient evidence that the requests overlapped.
    private fun overlap(
        firstAction: () -> Unit,
        secondAction: () -> Unit,
    ): Throwable? {
        val executor = Executors.newFixedThreadPool(2)
        val firstApplied = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondPid = ArrayBlockingQueue<Int>(1)
        try {
            val first =
                executor.submit(
                    Callable {
                        tx {
                            firstAction()
                            firstApplied.countDown()
                            check(releaseFirst.await(10, TimeUnit.SECONDS))
                        }
                    },
                )
            if (!firstApplied.await(5, TimeUnit.SECONDS)) {
                first.get(1, TimeUnit.SECONDS)
                fail<Unit>("First request did not reach its commit barrier")
            }
            val second =
                executor.submit(
                    Callable {
                        runCatching {
                            tx {
                                secondPid.put(jdbc.sql("SELECT pg_backend_pid()").query(Int::class.javaObjectType).single())
                                secondAction()
                            }
                        }.exceptionOrNull()
                    },
                )
            val pid = requireNotNull(secondPid.poll(5, TimeUnit.SECONDS))
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            var blocked = false
            while (System.nanoTime() < deadline) {
                blocked =
                    jdbc
                        .sql("SELECT EXISTS(SELECT 1 FROM pg_locks WHERE pid = :pid AND NOT granted)")
                        .param("pid", pid)
                        .query(Boolean::class.javaObjectType)
                        .single()
                if (blocked) break
                Thread.sleep(10)
            }
            assertTrue(blocked, "Second request must wait for the first transaction's PostgreSQL lock")
            releaseFirst.countDown()
            first.get(10, TimeUnit.SECONDS)
            return second.get(10, TimeUnit.SECONDS)
        } finally {
            releaseFirst.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))
        }
    }

    private data class Fixture(
        val id: UUID,
        val owner: String,
        val other: String,
    )
}
