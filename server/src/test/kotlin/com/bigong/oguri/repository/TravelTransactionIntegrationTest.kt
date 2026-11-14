package com.bigong.oguri.repository

import com.bigong.oguri.domain.Member
import com.bigong.oguri.domain.TravelPlan
import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.domain.TravelPlanMembership
import com.bigong.oguri.domain.TravelVisit
import com.bigong.oguri.dto.request.CreateTravelPlanRequest
import com.bigong.oguri.dto.request.TravelPlanCommand
import com.bigong.oguri.dto.request.TravelPlanSourceRequest
import com.bigong.oguri.dto.request.TravelVisitRequest
import com.bigong.oguri.dto.response.TravelPlanResponse
import com.bigong.oguri.dto.response.TravelVisitResponse
import com.bigong.oguri.service.RegisterTravelReminderDevice
import com.bigong.oguri.service.ScheduleTravelReminder
import com.bigong.oguri.service.TravelCollaborationService
import com.bigong.oguri.service.TravelDetailsChange
import com.bigong.oguri.service.TravelPlanService
import com.bigong.oguri.service.TravelReminderDelivery
import com.bigong.oguri.service.TravelReminderService
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
import org.mockito.Mockito.`when`
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
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
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
    private lateinit var reminders: TravelReminderRepository
    private lateinit var reminderService: TravelReminderService
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
        val usage = TravelUsageRepository(jdbc)
        service =
            TravelPlanService(
                plans,
                members,
                ObjectMapper().findAndRegisterModules(),
                mock(DestinationRepository::class.java),
                TravelPlanViewRepository(jdbc),
                changes,
                usage,
            )
        reminders = TravelReminderRepository(jdbc)
        collaboration = TravelCollaborationService(service, plans, members, changes, reminders, usage, 7)
        val delivery = mock(TravelReminderDelivery::class.java)
        `when`(delivery.configured).thenReturn(true)
        reminderService = TravelReminderService(service, reminders, delivery, usage)
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
        assertEquals(1L, count("travel_usage_cohorts", first.get()))
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
        assertEquals(2L, usageEditCount(trip.id))
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
        assertEquals(1L, usageEditCount(trip.id))
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

    @Test
    fun `홈 이어보기는 가까운 예정 여행보다 내가 연 여행을 우선하고 친구 수정으로 바뀌지 않는다`() {
        val owner = member()
        val friend = member()
        val today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        val near = tx { service.create(owner, creation(today.plusDays(10))).id }
        val far = tx { service.create(owner, creation(today.plusDays(20))).id }
        val past = tx { service.create(owner, creation(today.minusDays(10))).id }
        assertEquals(near, tx { service.resume(owner) }?.id)
        tx { service.recordView(owner, past, 1) }
        assertEquals(near, tx { service.resume(owner) }?.id)
        tx { service.recordView(owner, far, 1) }
        jdbc
            .sql("INSERT INTO travel_plan_members (travel_plan_id, member_id) VALUES (:id, :member)")
            .param("id", near)
            .param("member", friend)
            .update()
        tx { service.execute(friend, near, add("Friend edit")) }
        assertEquals(far, tx { service.resume(owner) }?.id)
    }

    @Test
    fun `진행 중 여행은 최근에 연 예정 여행보다 우선하고 진행 중 안에서 내 열람으로 고른다`() {
        val owner = member()
        val today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        tx { service.create(owner, creation(today.minusDays(1))) }
        val active = tx { service.create(owner, creation(today.minusDays(1))).id }
        val upcoming = tx { service.create(owner, creation(today.plusDays(10))).id }
        tx { service.recordView(owner, active, 1) }
        tx { service.recordView(owner, upcoming, 1) }
        assertEquals(active, tx { service.resume(owner) }?.id)
    }

    @Test
    fun `지난 여행만 있으면 목록은 보존하고 홈 이어보기는 비운다`() {
        val owner = member()
        val past = tx { service.create(owner, creation(LocalDate.now().minusDays(10))).id }
        tx { service.recordView(owner, past, 1) }
        assertNull(tx { service.resume(owner) })
        assertEquals(listOf(past), tx { service.list(owner) }.map { it.id })
    }

    @Test
    fun `제외된 멤버의 홈 이어보기는 이전 열람 기록으로 여행을 다시 선택하지 않는다`() {
        val trip = fixture(startDate = LocalDate.now().plusDays(10))
        tx { service.recordView(trip.other, trip.id, 1) }
        assertEquals(trip.id, tx { service.resume(trip.other) }?.id)
        tx { collaboration.remove(trip.owner, trip.id, trip.other) }
        assertNull(tx { service.resume(trip.other) })
        assertTrue(tx { service.list(trip.other) }.isEmpty())
    }

    @Test
    fun `예약 취소가 먼저 확정되면 대기 중인 발송 작업을 인계하지 않는다`() {
        val alarm = reminderFixture()
        assertNull(
            overlap(
                { reminderService.cancel(alarm.row.memberId, alarm.trip.id, alarm.row.id) },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        assertEquals(TravelReminderStatus.CANCELLED, reminders.find(alarm.row.id)?.status)
        assertFalse(reminders.due().any { it.id == alarm.row.id })
    }

    @Test
    fun `인계 시작 뒤 취소된 예약은 늦은 성공 응답으로 되살아나지 않는다`() {
        val alarm = reminderFixture()
        assertNull(
            overlap(
                { assertEquals(alarm.token, reminderService.beginHandoff(alarm.row)) },
                { reminderService.cancel(alarm.row.memberId, alarm.trip.id, alarm.row.id) },
            ),
        )
        reminders.finishHandoff(alarm.row.id, true)
        assertEquals(TravelReminderStatus.CANCELLED, reminders.find(alarm.row.id)?.status)
        assertEquals(
            HttpStatus.NOT_FOUND,
            assertThrows(ResponseStatusException::class.java) {
                tx { reminderService.open(alarm.row.memberId, alarm.row.id) }
            }.statusCode,
        )
    }

    @Test
    fun `예약 변경은 큐의 이전 버전을 막고 늦은 이전 취소는 새 예약을 취소하지 않는다`() {
        val alarm = reminderFixture()
        val summary = read(alarm.trip).summary
        val request =
            ScheduleTravelReminder(
                UUID.randomUUID(),
                LocalDateTime
                    .now(ZoneOffset.UTC)
                    .plusHours(1)
                    .withSecond(0)
                    .withNano(0),
                "UTC",
                summary.startDate,
                summary.endDate,
                summary.timeZone,
                alarm.row.deviceId,
            )
        assertNull(
            overlap(
                { reminderService.schedule(alarm.row.memberId, alarm.trip.id, request) },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        tx { reminderService.cancel(alarm.row.memberId, alarm.trip.id, alarm.row.id) }
        assertEquals(request.requestId, reminders.current(alarm.trip.id, alarm.row.memberId)?.id)
        assertEquals(TravelReminderStatus.PENDING, reminders.find(request.requestId)?.status)
        assertEquals(request.requestId, tx { reminderService.schedule(alarm.row.memberId, alarm.trip.id, request) }.id)
        assertEquals(
            2L,
            jdbc
                .sql("SELECT count(*) FROM travel_reminders WHERE travel_plan_id = :id")
                .param("id", alarm.trip.id)
                .query(Long::class.javaObjectType)
                .single(),
        )
    }

    @Test
    fun `로그아웃이 먼저 확정되면 이전 기기의 발송 인계를 차단한다`() {
        val alarm = reminderFixture()
        assertNull(
            overlap(
                { reminderService.revokeDevice(alarm.row.deviceId, alarm.secret) },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        assertNull(reminders.current(alarm.trip.id, alarm.row.memberId))
        assertFalse(reminders.due().any { it.id == alarm.row.id })
    }

    @Test
    fun `멤버 제외가 먼저 확정되면 큐의 개인 알림도 인계하지 않는다`() {
        val alarm = reminderFixture(forOwner = false)
        assertNull(
            overlap(
                { collaboration.remove(alarm.trip.owner, alarm.trip.id, alarm.trip.other) },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        assertEquals(TravelReminderStatus.CANCELLED, reminders.find(alarm.row.id)?.status)
    }

    @Test
    fun `두 발송 작업은 같은 예약을 한 번만 인계하고 알림 열기는 본인 권한을 확인한다`() {
        val alarm = reminderFixture()
        assertNull(
            overlap(
                { assertEquals(alarm.token, reminderService.beginHandoff(alarm.row)) },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        assertEquals(TravelReminderStatus.HANDOFF_STARTED, reminders.find(alarm.row.id)?.status)
        reminders.finishHandoff(alarm.row.id, true)
        assertEquals(alarm.trip.id, tx { reminderService.open(alarm.row.memberId, alarm.row.id) })
        assertEquals(
            HttpStatus.NOT_FOUND,
            assertThrows(ResponseStatusException::class.java) {
                tx { reminderService.open(alarm.trip.other, alarm.row.id) }
            }.statusCode,
        )
    }

    @Test
    fun `기간 변경이 먼저 확정되면 큐의 이전 예약을 인계하지 않는다`() {
        val alarm = reminderFixture()
        val before = read(alarm.trip)
        val summary = before.summary
        assertNull(
            overlap(
                {
                    collaboration.updateDetails(
                        alarm.trip.owner,
                        alarm.trip.id,
                        TravelDetailsChange(
                            summary.city,
                            summary.title,
                            summary.timeZone,
                            summary.startDate.plusDays(1),
                            summary.endDate.plusDays(1),
                            before.revision,
                            0,
                        ),
                    )
                },
                { assertNull(reminderService.beginHandoff(alarm.row)) },
            ),
        )
        assertEquals(TravelReminderStatus.CANCELLED, reminders.find(alarm.row.id)?.status)
    }

    @Test
    fun `생성 참여 재시도와 재참여는 관찰 시작을 중복 기록하지 않는다`() {
        val owner = member()
        val friend = member()
        val request = creation(LocalDate.now().plusDays(20))
        val plan = tx { service.create(owner, request) }
        tx { service.create(owner, request) }
        val token = tx { collaboration.issue(owner, plan.id).token }
        tx { collaboration.join(friend, token) }
        tx { collaboration.join(friend, token) }
        tx { collaboration.leave(friend, plan.id) }
        val nextToken = tx { collaboration.issue(owner, plan.id).token }
        tx { collaboration.join(friend, nextToken) }
        val entries =
            jdbc
                .sql("SELECT entry_kind FROM travel_usage_cohorts WHERE travel_plan_id = :id ORDER BY entry_kind")
                .param("id", plan.id)
                .query(String::class.java)
                .list()
        assertEquals(listOf("CREATED", "JOINED"), entries)
        assertEquals(
            1L,
            jdbc
                .sql("SELECT count(DISTINCT plan_key) FROM travel_usage_cohorts WHERE travel_plan_id = :id")
                .param("id", plan.id)
                .query(Long::class.javaObjectType)
                .single(),
        )
    }

    @Test
    fun `날짜 없는 저장은 활성화로 세지 않고 날짜 배정 재전송은 한번만 기록한다`() {
        val owner = member()
        val plan = tx { service.create(owner, creation()) }
        val unassigned = add("Private name").copy(dayNumber = 0)
        val visit = tx { service.execute(owner, plan.id, unassigned) }.visits.single()
        tx { service.execute(owner, plan.id, unassigned) }
        val assigned = edit(visit).copy(dayNumber = 1)
        tx { service.execute(owner, plan.id, assigned) }
        tx { service.execute(owner, plan.id, assigned) }
        val dated =
            jdbc
                .sql(
                    "SELECT e.has_dated_visit FROM travel_usage_edits e JOIN travel_usage_cohorts c ON c.id = e.cohort_id WHERE c.travel_plan_id = :id ORDER BY e.revision",
                ).param("id", plan.id)
                .query(Boolean::class.javaObjectType)
                .list()
        assertEquals(listOf(false, true), dated)
        val kinds =
            jdbc
                .sql(
                    "SELECT e.kind FROM travel_usage_edits e JOIN travel_usage_cohorts c ON c.id = e.cohort_id WHERE c.travel_plan_id = :id ORDER BY e.revision",
                ).param("id", plan.id)
                .query(String::class.java)
                .list()
        assertEquals(listOf("VISIT_ADDED", "VISIT_EDITED"), kinds)
    }

    @Test
    fun `협업 편집은 링크 발급과 구분하고 참여 이후에만 기록한다`() {
        val owner = member()
        val friend = member()
        val plan = tx { service.create(owner, creation()) }
        val token = tx { collaboration.issue(owner, plan.id).token }
        tx { service.execute(owner, plan.id, add("Before join")) }
        tx { collaboration.join(friend, token) }
        tx { service.execute(friend, plan.id, add("After join")) }
        val collaborative =
            jdbc
                .sql(
                    "SELECT e.after_participation FROM travel_usage_edits e JOIN travel_usage_cohorts c ON c.id = e.cohort_id WHERE c.travel_plan_id = :id ORDER BY e.revision",
                ).param("id", plan.id)
                .query(Boolean::class.javaObjectType)
                .list()
        assertEquals(listOf(false, true), collaborative)
    }

    @Test
    fun `기간 변경 나가기 삭제와 탈퇴로 관찰 분모를 유리하게 바꾸지 않는다`() {
        val owner = member()
        val friend = member()
        val start = LocalDate.now().plusDays(20)
        val plan = tx { service.create(owner, creation(start)) }
        val token = tx { collaboration.issue(owner, plan.id).token }
        tx { collaboration.join(friend, token) }
        val before =
            jdbc
                .sql(
                    "SELECT id, started_at, days_until_start, initial_end_at FROM travel_usage_cohorts WHERE travel_plan_id = :id ORDER BY id",
                ).param("id", plan.id)
                .query()
                .listOfRows()
        tx {
            collaboration.updateDetails(
                owner,
                plan.id,
                TravelDetailsChange("Seoul", "Private title", "Asia/Seoul", start.plusDays(3), start.plusDays(5), 0, 0),
            )
        }
        tx { collaboration.leave(friend, plan.id) }
        val after =
            jdbc
                .sql(
                    "SELECT id, started_at, days_until_start, initial_end_at FROM travel_usage_cohorts WHERE travel_plan_id = :id ORDER BY id",
                ).param("id", plan.id)
                .query()
                .listOfRows()
        assertEquals(before, after)
        tx { collaboration.delete(owner, plan.id) }
        jdbc
            .sql("DELETE FROM members WHERE id IN (:owner, :friend)")
            .param("owner", owner)
            .param("friend", friend)
            .update()
        before.forEach { row ->
            val saved =
                jdbc
                    .sql("SELECT travel_plan_id, member_id, started_at, days_until_start FROM travel_usage_cohorts WHERE id = :id")
                    .param("id", row.getValue("id"))
                    .query()
                    .singleRow()
            assertNull(saved["travel_plan_id"])
            assertNull(saved["member_id"])
            assertEquals(row["started_at"], saved["started_at"])
            assertEquals(row["days_until_start"], saved["days_until_start"])
        }
    }

    @Test
    fun `취소된 생성과 편집은 지표에도 남지 않는다`() {
        val owner = member()
        val request = creation()
        assertThrows(IllegalStateException::class.java) {
            tx {
                service.create(owner, request)
                throw IllegalStateException("abort creation")
            }
        }
        assertEquals(
            0L,
            jdbc
                .sql("SELECT count(*) FROM travel_usage_cohorts WHERE member_id = :id")
                .param("id", owner)
                .query(Long::class.javaObjectType)
                .single(),
        )
        val plan = tx { service.create(owner, request) }
        assertThrows(IllegalStateException::class.java) {
            tx {
                service.execute(owner, plan.id, add("Aborted edit"))
                throw IllegalStateException("abort edit")
            }
        }
        assertEquals(
            0L,
            jdbc
                .sql(
                    "SELECT count(*) FROM travel_usage_edits e JOIN travel_usage_cohorts c ON c.id = e.cohort_id WHERE c.travel_plan_id = :id",
                ).param("id", plan.id)
                .query(Long::class.javaObjectType)
                .single(),
        )
    }

    @Test
    fun `기초 지표 집계는 추천 출처와 활성화와 관찰 미완료를 구분한다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_usage.sql"))

        fun report() =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .associateBy { it.getValue("entry_source") }
        val before = report()["RECOMMENDATION"].orEmpty()
        val owner = member()
        val start = LocalDate.now().plusDays(20)
        val request = creation(start).copy(source = TravelPlanSourceRequest(null, start, start.plusDays(2)))
        val plan = tx { service.create(owner, request) }
        tx { service.execute(owner, plan.id, add("Private name")) }
        val pending = report().getValue("RECOMMENDATION")
        for (key in listOf("created_plans", "activated_plans", "pending_memberships")) {
            assertEquals((before[key] as? Number)?.toLong().orZero() + 1L, (pending.getValue(key) as Number).toLong())
        }
        assertEquals(
            (before["eligible_matured_memberships"] as? Number)?.toLong().orZero(),
            (pending.getValue("eligible_matured_memberships") as Number).toLong(),
        )
        jdbc
            .sql("UPDATE travel_usage_cohorts SET started_at = started_at - interval '8 days' WHERE travel_plan_id = :id")
            .param("id", plan.id)
            .update()
        val matured = report().getValue("RECOMMENDATION")
        assertEquals(
            (before["eligible_matured_memberships"] as? Number)?.toLong().orZero() + 1L,
            (matured.getValue("eligible_matured_memberships") as Number).toLong(),
        )
        assertEquals(
            (before["pending_memberships"] as? Number)?.toLong().orZero(),
            (matured.getValue("pending_memberships") as Number).toLong(),
        )
    }

    @Test
    fun `기존 여행의 첫 관측 편집을 신규 생성이나 참여로 바꾸지 않는다`() {
        val owner = member()
        val plan = tx { service.create(owner, creation()) }
        // Model a plan that predates usage collection, without fabricating historical events.
        jdbc.sql("DELETE FROM travel_usage_cohorts WHERE travel_plan_id = :id").param("id", plan.id).update()
        tx { service.execute(owner, plan.id, add("Existing trip edit")) }
        val cohort =
            jdbc
                .sql("SELECT entry_kind, days_until_start FROM travel_usage_cohorts WHERE travel_plan_id = :id")
                .param("id", plan.id)
                .query()
                .singleRow()
        assertEquals("EXISTING", cohort["entry_kind"])
        assertNull(cohort["days_until_start"])
        assertEquals(1L, usageEditCount(plan.id))
    }

    private fun Long?.orZero(): Long = this ?: 0L

    private fun usageEditCount(planId: UUID): Long =
        jdbc
            .sql("SELECT count(*) FROM travel_usage_edits e JOIN travel_usage_cohorts c ON c.id = e.cohort_id WHERE c.travel_plan_id = :id")
            .param("id", planId)
            .query(Long::class.javaObjectType)
            .single()

    @Test
    fun `사용 행동은 세션과 종류별 한번만 기록하고 제외된 멤버와 비회원은 거부한다`() {
        val trip = fixture()
        val session = UUID.randomUUID()
        repeat(2) { tx { service.recordActivity(trip.other, trip.id, session, TravelUsageActivity.PLAN_DISPLAYED) } }
        tx { service.recordActivity(trip.other, trip.id, session, TravelUsageActivity.DAY_SELECTED) }
        assertEquals(
            2L,
            jdbc
                .sql(
                    "SELECT count(*) FROM travel_usage_activity a JOIN travel_usage_cohorts c ON c.id = a.cohort_id WHERE c.travel_plan_id = :id",
                ).param("id", trip.id)
                .query(Long::class.javaObjectType)
                .single(),
        )
        tx { collaboration.remove(trip.owner, trip.id, trip.other) }
        val denied =
            assertThrows(ResponseStatusException::class.java) {
                tx { service.recordActivity(trip.other, trip.id, UUID.randomUUID(), TravelUsageActivity.PIN_SELECTED) }
            }
        assertEquals(HttpStatus.NOT_FOUND, denied.statusCode)
        val guest =
            assertThrows(ResponseStatusException::class.java) {
                tx { service.recordActivity("GUEST", trip.id, UUID.randomUUID(), TravelUsageActivity.PIN_SELECTED) }
            }
        assertEquals(HttpStatus.UNAUTHORIZED, guest.statusCode)
    }

    @Test
    fun `저장 재전송의 새 세션은 처음 확정된 세션을 덮어쓰지 않는다`() {
        val owner = member()
        val initialSession = UUID.randomUUID()
        val retrySession = UUID.randomUUID()
        val request = creation()
        val plan = tx { service.create(owner, request, initialSession) }
        tx { service.create(owner, request, retrySession) }
        val command = add("Private visit")
        tx { service.execute(owner, plan.id, command, initialSession) }
        tx { service.execute(owner, plan.id, command, retrySession) }
        val saved =
            jdbc
                .sql(
                    "SELECT c.start_session_id, e.session_id FROM travel_usage_cohorts c JOIN travel_usage_edits e ON e.cohort_id = c.id WHERE c.travel_plan_id = :id",
                ).param("id", plan.id)
                .query()
                .singleRow()
        assertEquals(initialSession, saved["start_session_id"])
        assertEquals(initialSession, saved["session_id"])
        assertEquals(1L, usageEditCount(plan.id))
    }

    @Test
    fun `재사용 집계는 표시와 목적 행동을 나누고 다른 세션의 24시간부터 7일만 센다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_preparation_reuse.sql"))

        fun report(): Map<String, Any> =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .firstOrNull { it["entry_source"] == "DIRECT" }
                .orEmpty()

        fun value(
            row: Map<String, Any>,
            key: String,
        ): Long = (row[key] as? Number)?.toLong() ?: 0L
        val before = report()
        val owner = member()
        val initialSession = UUID.randomUUID()
        val laterSession = UUID.randomUUID()
        val plan = tx { service.create(owner, creation(LocalDate.now().plusDays(20)), initialSession) }
        jdbc
            .sql(
                "UPDATE travel_usage_cohorts SET started_at = statement_timestamp() - interval '8 days' WHERE travel_plan_id = :id",
            ).param("id", plan.id)
            .update()

        fun action(
            kind: TravelUsageActivity,
            session: UUID,
        ) = tx { service.recordActivity(owner, plan.id, session, kind) }

        fun actionTime(
            kind: TravelUsageActivity,
            interval: String,
        ) {
            jdbc
                .sql(
                    "UPDATE travel_usage_activity a SET occurred_at = c.started_at + CAST(:offset AS interval) FROM travel_usage_cohorts c WHERE a.cohort_id = c.id AND c.travel_plan_id = :id AND a.kind = :kind",
                ).param("offset", interval)
                .param("id", plan.id)
                .param("kind", kind.name)
                .update()
        }
        action(TravelUsageActivity.PLAN_DISPLAYED, laterSession)
        actionTime(TravelUsageActivity.PLAN_DISPLAYED, "25 hours")
        var current = report()
        assertEquals(value(before, "eligible_memberships") + 1, value(current, "eligible_memberships"))
        assertEquals(value(before, "displayed_again_memberships") + 1, value(current, "displayed_again_memberships"))
        assertEquals(value(before, "reused_memberships"), value(current, "reused_memberships"))
        action(TravelUsageActivity.DAY_SELECTED, initialSession)
        actionTime(TravelUsageActivity.DAY_SELECTED, "26 hours")
        assertEquals(value(before, "reused_memberships"), value(report(), "reused_memberships"))
        action(TravelUsageActivity.PIN_SELECTED, laterSession)
        for (interval in listOf("23 hours", "7 days 1 second")) {
            actionTime(TravelUsageActivity.PIN_SELECTED, interval)
            assertEquals(value(before, "reused_memberships"), value(report(), "reused_memberships"))
        }
        for (interval in listOf("24 hours", "7 days")) {
            actionTime(TravelUsageActivity.PIN_SELECTED, interval)
            current = report()
            assertEquals(value(before, "reused_memberships") + 1, value(current, "reused_memberships"))
            assertEquals(value(before, "reused_linked_users") + 1, value(current, "reused_linked_users"))
        }
        actionTime(TravelUsageActivity.PIN_SELECTED, "8 days")
        tx { service.execute(owner, plan.id, add("Later edit"), laterSession) }
        jdbc
            .sql(
                "UPDATE travel_usage_edits e SET occurred_at = c.started_at + interval '2 days' FROM travel_usage_cohorts c WHERE e.cohort_id = c.id AND c.travel_plan_id = :id",
            ).param("id", plan.id)
            .update()
        assertEquals(value(before, "reused_memberships") + 1, value(report(), "reused_memberships"))
    }

    @Test
    fun `구버전 생성의 세션 미상을 후속 행동으로 덮어쓰지 않는다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_preparation_reuse.sql"))

        fun report() =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .first { it["entry_source"] == "ALL" }
        // Ensure a total row exists even when this test runs before any other cohort.
        val owner = member()
        val before = report()
        val plan = tx { service.create(owner, creation(LocalDate.now().plusDays(20))) }
        jdbc
            .sql("UPDATE travel_usage_cohorts SET started_at = statement_timestamp() - interval '8 days' WHERE travel_plan_id = :id")
            .param("id", plan.id)
            .update()
        tx { service.recordActivity(owner, plan.id, UUID.randomUUID(), TravelUsageActivity.DAY_SELECTED) }
        jdbc
            .sql(
                "UPDATE travel_usage_activity a SET occurred_at = c.started_at + interval '2 days' FROM travel_usage_cohorts c WHERE a.cohort_id = c.id AND c.travel_plan_id = :id",
            ).param("id", plan.id)
            .update()
        assertNull(
            jdbc
                .sql("SELECT start_session_id FROM travel_usage_cohorts WHERE travel_plan_id = :id")
                .param("id", plan.id)
                .query()
                .singleRow()["start_session_id"],
        )
        val after = report()
        assertEquals(
            (before.getValue("eligible_memberships") as Number).toLong() + 1,
            (after.getValue("eligible_memberships") as Number).toLong(),
        )
        assertEquals(before["session_known_memberships"], after["session_known_memberships"])
        assertEquals(before["reused_memberships"], after["reused_memberships"])
    }

    @Test
    fun `다음 여행 탐색은 종료 여행에서만 기록하고 준비 기간 재사용에 넣지 않는다`() {
        val owner = member()
        val today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        val upcoming = tx { service.create(owner, creation(today.plusDays(20)), UUID.randomUUID()) }
        val session = UUID.randomUUID()
        val kind = TravelUsageActivity.NEXT_HOLIDAY_OPENED
        val error =
            assertThrows(ResponseStatusException::class.java) {
                tx { service.recordActivity(owner, upcoming.id, session, kind) }
            }
        assertEquals(HttpStatus.BAD_REQUEST, error.statusCode)
        val ended = tx { service.create(owner, creation(today.minusDays(10)), UUID.randomUUID()) }
        repeat(2) { tx { service.recordActivity(owner, ended.id, session, kind) } }
        val planActivity =
            jdbc
                .sql(
                    """
                    SELECT count(*) FROM travel_usage_activity a JOIN travel_usage_cohorts c ON c.id = a.cohort_id
                    WHERE c.travel_plan_id = :plan AND a.kind = :kind
                    """,
                ).param("plan", ended.id)
                .param("kind", kind.name)
                .query(Long::class.javaObjectType)
                .single()
        assertEquals(1L, planActivity)
        val sql = Files.readString(Path.of("../scripts/report_travel_preparation_reuse.sql"))
        fun reused(): Any =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .first { it["entry_source"] == "ALL" }
                .getValue("reused_memberships")
        val before = reused()
        jdbc
            .sql(
                """
                UPDATE travel_usage_cohorts c
                SET days_until_start = 8, started_at = a.occurred_at - interval '10 days'
                FROM travel_usage_activity a WHERE a.cohort_id = c.id AND c.travel_plan_id = :plan
                """,
            ).param("plan", ended.id)
            .update()
        jdbc
            .sql(
                """
                UPDATE travel_usage_activity a
                SET occurred_at = c.started_at + interval '2 days'
                FROM travel_usage_cohorts c WHERE a.cohort_id = c.id AND c.travel_plan_id = :plan
                """,
            ).param("plan", ended.id)
            .update()
        assertEquals(before, reused())
    }

    @Test
    fun `다음 여행 집계는 회원별 첫 종료와 90일 경계에서 관찰 결과를 나눈다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_next_trip_reuse.sql"))
        tx {
            jdbc.sql("SET LOCAL oguri.travel_observation_start = '2025-01-01T00:00:00Z'").update()
            jdbc.sql("SET LOCAL oguri.travel_observation_end = '2025-05-01T00:00:00Z'").update()
            val firstEnd = OffsetDateTime.parse("2025-01-15T00:00:00Z")
            val owner = member()
            val pendingOwner = member()
            val unlinkedOwner = member()
            val nonqualifyingOwner = member()
            val endedPlan = usageCohort(owner, firstEnd.minusDays(12), firstEnd)
            usageCohort(owner, firstEnd.minusDays(60), firstEnd.minusDays(40))
            usageCohort(owner, firstEnd.minusDays(10), firstEnd.plusDays(4))
            usageCohort(pendingOwner, firstEnd.plusDays(57), firstEnd.plusDays(70))
            usageCohort(unlinkedOwner, firstEnd.minusDays(12), firstEnd)
            usageCohort(nonqualifyingOwner, firstEnd.minusDays(2), firstEnd, leadDays = 0)
            jdbc.sql("DELETE FROM members WHERE id = :member").param("member", unlinkedOwner).update()

            val boundary = firstEnd.plusDays(90)
            usageCohort(owner, firstEnd, boundary, entry = TravelUsageEntry.CREATED, leadDays = 0)
            usageCohort(owner, boundary.plusSeconds(1), boundary.plusDays(14), entry = TravelUsageEntry.JOINED)
            jdbc
                .sql(
                    """
                    INSERT INTO travel_usage_activity (cohort_id, session_id, kind, occurred_at)
                    VALUES (:cohort, :session, 'NEXT_HOLIDAY_OPENED', :time)
                    """,
                ).param("cohort", endedPlan)
                .param("session", UUID.randomUUID())
                .param("time", boundary)
                .update()
            fun totals(): Map<String, Long> =
                jdbc
                    .sql(sql)
                    .query()
                    .singleRow()
                    .mapValues { (_, value) -> (value as Number).toLong() }
            assertEquals(
                mapOf(
                    "eligible_linked_users" to 1L,
                    "pending_linked_users" to 1L,
                    "explored_linked_users" to 1L,
                    "created_linked_users" to 0L,
                    "joined_linked_users" to 0L,
                    "started_next_trip_linked_users" to 0L,
                    "unlinked_ended_memberships" to 1L,
                ),
                totals(),
            )
            usageCohort(owner, boundary, boundary.plusDays(20), entry = TravelUsageEntry.CREATED)
            usageCohort(owner, boundary, boundary.plusDays(20), entry = TravelUsageEntry.JOINED)
            val after = totals()
            assertEquals(1L, after["created_linked_users"])
            assertEquals(1L, after["joined_linked_users"])
            assertEquals(1L, after["started_next_trip_linked_users"])
        }
    }

    @Test
    fun `변경에서 날짜 열기와 명시적 알림 해제를 사용자 여행별로 한 번 센다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_usage.sql"))
        fun totals(): Map<String, Long> =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .firstOrNull { it["entry_source"] == "DIRECT" }
                .orEmpty()
                .filterValues { it is Number }
                .mapValues { (_, value) -> (value as Number).toLong() }
        val before = totals()
        val alarm = reminderFixture()
        val (planId, owner) = alarm.trip
        repeat(2) {
            tx { service.recordActivity(owner, planId, UUID.randomUUID(), TravelUsageActivity.CHANGE_DAY_OPENED) }
        }
        tx { reminderService.cancel(owner, planId, UUID.randomUUID()) }
        assertEquals(before["reminder_opt_out_memberships"] ?: 0L, totals().getValue("reminder_opt_out_memberships"))
        repeat(2) { tx { reminderService.cancel(owner, planId, alarm.row.id) } }
        val after = totals()
        for (column in listOf("changes_viewed_memberships", "changed_day_opened_memberships", "reminder_opt_out_memberships")) {
            assertEquals((before[column] ?: 0L) + 1, after[column])
        }
    }

    @Test
    fun `추천 진입은 세션별로 중복을 막고 확인된 여행 생성은 별도로 센다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_recommendation_entries.sql"))
        fun totals(
            source: TravelRecommendationSource,
            signedIn: Boolean,
        ): Map<String, Long> =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .firstOrNull { it["source"] == source.name && it["signed_in"] == signedIn }
                .orEmpty()
                .filterValues { it is Number }
                .mapValues { (_, value) -> (value as Number).toLong() }
        val source = TravelRecommendationSource.PERIOD
        val beforeGuest = totals(source, false)
        val beforeMember = totals(source, true)
        val usage = TravelUsageRepository(jdbc)
        val session = UUID.randomUUID()
        repeat(2) {
            usage.recordRecommendationEntry(session, source, TravelRecommendationEntryKind.EXPOSED, false)
            usage.recordRecommendationEntry(session, source, TravelRecommendationEntryKind.CLICKED, false)
            usage.recordRecommendationEntry(session, source, TravelRecommendationEntryKind.LOGIN_RESUMED, true)
        }
        val upcoming = LocalDate.now().plusDays(20)
        val owner = member()
        val request = creation(upcoming).copy(source = TravelPlanSourceRequest(null, upcoming, upcoming.plusDays(2)))
        repeat(2) { tx { service.create(owner, request, session) } }
        val guest = totals(source, false)
        val member = totals(source, true)
        assertEquals((beforeGuest["exposed_sessions"] ?: 0L) + 1, guest["exposed_sessions"])
        assertEquals((beforeGuest["clicked_sessions"] ?: 0L) + 1, guest["clicked_sessions"])
        assertEquals(0L, guest["created_trips"])
        assertEquals((beforeMember["resumed_after_login_sessions"] ?: 0L) + 1, member["resumed_after_login_sessions"])
        assertEquals((beforeMember["created_trips"] ?: 0L) + 1, member["created_trips"])
    }

    @Test
    fun `방문 저장 실패와 충돌 복구는 확정 편집과 별도로 집계한다`() {
        val sql = Files.readString(Path.of("../scripts/report_travel_usage.sql"))
        fun totals(): Map<String, Long> =
            jdbc
                .sql(sql)
                .query()
                .listOfRows()
                .firstOrNull { it["entry_source"] == "DIRECT" }
                .orEmpty()
                .filterValues { it is Number }
                .mapValues { (_, value) -> (value as Number).toLong() }
        val trip = fixture()
        val before = totals()
        for (kind in listOf(
            TravelUsageActivity.VISIT_SAVE_FAILED,
            TravelUsageActivity.VISIT_SAVE_CONFLICT,
            TravelUsageActivity.VISIT_CONFLICT_RECOVERED,
        )) {
            tx { service.recordActivity(trip.owner, trip.id, UUID.randomUUID(), kind) }
        }
        val after = totals()
        for (column in listOf("visit_save_failed_memberships", "visit_save_conflict_memberships", "visit_conflict_recovered_memberships")) {
            assertEquals((before[column] ?: 0L) + 1, after[column])
        }
        assertEquals(before["activated_plans"], after["activated_plans"])
        assertEquals(0L, usageEditCount(trip.id))
    }

    private fun usageCohort(
        memberId: String,
        startedAt: OffsetDateTime,
        endedAt: OffsetDateTime,
        entry: TravelUsageEntry = TravelUsageEntry.CREATED,
        leadDays: Int = 8,
    ): UUID {
        val id = UUID.randomUUID()
        jdbc
            .sql(
                """
                INSERT INTO travel_usage_cohorts (id, plan_key, member_id, entry_kind, entry_source,
                    started_at, days_until_start, initial_end_at)
                VALUES (:id, :key, :member, :entry, 'DIRECT', :started, :lead, :ended)
                """,
            ).param("id", id)
            .param("key", UUID.randomUUID())
            .param("member", memberId)
            .param("entry", entry.name)
            .param("started", startedAt)
            .param("lead", leadDays)
            .param("ended", endedAt)
            .update()
        return id
    }

    private fun reminderFixture(forOwner: Boolean = true): ReminderFixture {
        val trip = fixture(startDate = LocalDate.now().plusDays(10))
        val member = if (forOwner) trip.owner else trip.other
        val device = UUID.randomUUID()
        val secret = "a".repeat(64)
        val token = "test-push-token-" + UUID.randomUUID()
        val id = UUID.randomUUID()
        tx {
            reminderService.registerDevice(member, device, RegisterTravelReminderDevice(secret, token))
            reminders.insert(id, trip.id, member, device, OffsetDateTime.now().minusSeconds(30), "UTC")
        }
        return ReminderFixture(trip, requireNotNull(reminders.find(id)), secret, token)
    }

    private fun fixture(
        visits: Int = 0,
        startDate: LocalDate = LocalDate.of(2027, 4, 1),
    ): Fixture {
        val owner = member()
        val other = member()
        val id = tx { service.create(owner, creation(startDate)).id }
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

    private fun creation(startDate: LocalDate = LocalDate.of(2027, 4, 1)): CreateTravelPlanRequest =
        CreateTravelPlanRequest(
            UUID.randomUUID(),
            "Seoul",
            "Asia/Seoul",
            "Trip",
            startDate,
            startDate.plusDays(2),
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
        require(table in setOf("travel_plans", "travel_plan_changes", "travel_plan_member_commands", "travel_usage_cohorts"))
        val column = if (table == "travel_plans") "id" else "travel_plan_id"
        return jdbc
            .sql("SELECT count(*) FROM $table WHERE $column = :id")
            .param("id", plan)
            .query(Long::class.javaObjectType)
            .single()
    }

    private fun <T> tx(action: () -> T): T =
        requireNotNull(
            transaction.execute {
                jdbc.sql("SET LOCAL statement_timeout = '8s'").update()
                TransactionValue(action())
            },
        ).value

    private data class TransactionValue<T>(
        val value: T,
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

    private data class ReminderFixture(
        val trip: Fixture,
        val row: TravelReminderRow,
        val secret: String,
        val token: String,
    )

    private data class Fixture(
        val id: UUID,
        val owner: String,
        val other: String,
    )
}
