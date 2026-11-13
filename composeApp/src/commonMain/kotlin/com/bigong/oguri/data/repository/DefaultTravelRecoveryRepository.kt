package com.bigong.oguri.data.repository

import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.data.local.travelRecoveryStorage
import com.bigong.oguri.domain.model.TravelDraft
import com.bigong.oguri.domain.model.TravelPendingSave
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelRecovery
import com.bigong.oguri.domain.repository.TravelRecoveryRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Inject
class DefaultTravelRecoveryRepository : TravelRecoveryRepository {
    private val sessionVersion: Long = AuthTokenStore.currentSession().version

    override suspend fun load(planId: String?): TravelRecovery =
        access {
            val entry = it[planId ?: CREATION] ?: return@access TravelRecovery()
            TravelRecovery(entry.plan?.value, entry.edit?.draft, entry.edit?.pending)
        } ?: TravelRecovery()

    override suspend fun rememberPlan(plan: TravelPlan) {
        mutate(plan.summary.id) { entry ->
            if ((entry.plan?.value?.revision ?: -1) > plan.revision) entry else entry.copy(plan = StoredPlan(now(), plan))
        }
    }

    override suspend fun saveDraft(
        planId: String,
        draft: TravelDraft?,
    ) {
        mutate(planId) { entry ->
            if (entry.edit?.pending != null) entry else entry.copy(edit = draft?.let { StoredEdit(now(), it, null) })
        }
    }

    override suspend fun saveRequest(
        planId: String?,
        request: TravelPendingSave,
        draft: TravelDraft?,
    ) {
        val saved = mutate(planId) { it.copy(edit = StoredEdit(now(), draft, request)) }
        if (!saved) throw TravelPlanException(TravelPlanFailure.SIGN_IN_REQUIRED)
    }

    override suspend fun finishRequest(
        planId: String?,
        clearDraft: Boolean,
    ) {
        mutate(planId) { entry ->
            val draft = entry.edit?.draft
            entry.copy(edit = if (clearDraft || draft == null) null else StoredEdit(now(), draft, null))
        }
    }

    override suspend fun forget(planId: String) {
        access { it.remove(planId) }
    }

    private suspend fun mutate(
        planId: String?,
        transform: (RecoveryEntry) -> RecoveryEntry,
    ): Boolean =
        access {
            val key = planId ?: CREATION
            it[key] = transform(it[key] ?: RecoveryEntry())
            true
        } ?: false

    private suspend fun <Result> access(block: (MutableMap<String, RecoveryEntry>) -> Result): Result? =
        withContext(Dispatchers.Default) {
            travelRecoveryStorage().access vault@{ raw, write ->
                val session = AuthTokenStore.currentSession()
                val account = session.accountKey
                if (session.version != sessionVersion || session.tokens == null || account == null) return@vault null
                val stored = raw?.let { runCatching { Json.decodeFromString<RecoveryVault>(it) }.getOrNull() }
                val now = now()
                val entries =
                    stored
                        ?.takeIf { it.account == account }
                        ?.entries
                        .orEmpty()
                        .mapValues { (_, entry) ->
                            RecoveryEntry(
                                plan = entry.plan?.takeIf { now - it.savedAt in 0..PLAN_TTL },
                                edit = entry.edit?.takeIf { now - it.savedAt in 0..EDIT_TTL },
                            )
                        }.filterValues { it.plan != null || it.edit != null }
                        .toMutableMap()
                val result = block(entries)
                val updated = entries.filterValues { it.plan != null || it.edit != null }
                if (updated.isEmpty()) {
                    if (raw != null) write(null)
                } else if (stored?.entries != updated || stored.account != account) {
                    write(Json.encodeToString(RecoveryVault(account, updated)))
                }
                result
            }
        }

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        const val CREATION: String = "creation"
        val PLAN_TTL: Long = 15.minutes.inWholeMilliseconds
        val EDIT_TTL: Long = 24.hours.inWholeMilliseconds
    }
}

@Serializable
private data class RecoveryVault(
    val account: String,
    val entries: Map<String, RecoveryEntry>,
)

@Serializable
private data class RecoveryEntry(
    val plan: StoredPlan? = null,
    val edit: StoredEdit? = null,
)

@Serializable
private data class StoredPlan(
    val savedAt: Long,
    val value: TravelPlan,
)

@Serializable
private data class StoredEdit(
    val savedAt: Long,
    val draft: TravelDraft?,
    val pending: TravelPendingSave?,
)
