package com.bigong.oguri.feature.onboarding.ui

import com.bigong.oguri.domain.model.AutoLoginState
import com.bigong.oguri.domain.repository.AuthRepository
import com.bigong.oguri.domain.usecase.CompleteOnboardingUseCase
import com.bigong.oguri.domain.usecase.ValidateOnboardingLeaveDaysUseCase
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingSideEffect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun completingWithInputKeepsTheSavedResultForALateObserver() =
        runTest {
            val repository = RecordingAuthRepository()
            val viewModel = createViewModel(repository)
            viewModel.agreeAllAndMoveToNext()
            assertEquals("", viewModel.uiState.value.remainingDayOffInput)
            assertEquals("", viewModel.uiState.value.preferredDayOffInput)
            assertFalse(viewModel.uiState.value.shouldShowPreferredDayOffInput)

            viewModel.updateRemainingDayOffInput("12")
            assertTrue(viewModel.uiState.value.shouldShowPreferredDayOffInput)
            assertFalse(viewModel.uiState.value.hasValidDayOffInputs)
            viewModel.completeOnboarding()
            runCurrent()
            assertTrue(repository.submissions.isEmpty())

            viewModel.updatePreferredDayOffInput("3")
            assertTrue(viewModel.uiState.value.hasValidDayOffInputs)

            viewModel.completeOnboarding()
            runCurrent()

            assertEquals(listOf(SubmittedDayOff(remainingDayOff = 12, preferredDayOff = 3)), repository.submissions)
            val state = viewModel.uiState.first()
            assertTrue(state.isCompleted)
            assertFalse(state.isSubmitting)
        }

    @Test
    fun editingDayOffInputsUpdatesCompletionWithoutLeavingTheField() =
        runTest {
            val repository = RecordingAuthRepository()
            val viewModel = createViewModel(repository)
            viewModel.agreeAllAndMoveToNext()

            viewModel.updateRemainingDayOffInput("41")
            assertFalse(viewModel.uiState.value.shouldShowPreferredDayOffInput)
            viewModel.updateRemainingDayOffInput("1")
            assertTrue(viewModel.uiState.value.shouldShowPreferredDayOffInput)
            viewModel.updatePreferredDayOffInput("2")
            assertFalse(viewModel.uiState.value.hasValidDayOffInputs)
            viewModel.completeOnboarding()
            runCurrent()
            assertTrue(repository.submissions.isEmpty())

            viewModel.updatePreferredDayOffInput("1")
            assertTrue(viewModel.uiState.value.hasValidDayOffInputs)
            viewModel.updatePreferredDayOffInput("")
            assertFalse(viewModel.uiState.value.hasValidDayOffInputs)

            viewModel.updateRemainingDayOffInput("15")
            viewModel.updatePreferredDayOffInput("2")
            assertTrue(viewModel.uiState.value.hasValidDayOffInputs)
            viewModel.updateRemainingDayOffInput("0")
            assertFalse(viewModel.uiState.value.hasValidDayOffInputs)
        }

    @Test
    fun skippingRequiresTermsAndSubmitsDefaultsOnlyOnce() =
        runTest {
            val save = CompletableDeferred<Unit>()
            val repository = RecordingAuthRepository { save.await() }
            val viewModel = createViewModel(repository)

            viewModel.skipOnboardingLeaveDays()
            runCurrent()
            assertTrue(repository.submissions.isEmpty())

            viewModel.agreeAllAndMoveToNext()
            viewModel.skipOnboardingLeaveDays()
            runCurrent()
            assertTrue(viewModel.uiState.value.isSubmitting)
            assertFalse(viewModel.uiState.value.isCompleted)

            viewModel.skipOnboardingLeaveDays()
            save.complete(Unit)
            runCurrent()
            viewModel.skipOnboardingLeaveDays()
            runCurrent()

            assertEquals(listOf(SubmittedDayOff(remainingDayOff = 17, preferredDayOff = 2)), repository.submissions)
            assertTrue(viewModel.uiState.value.isCompleted)
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun failedSkipKeepsInputAndCanBeRetried() =
        runTest {
            val repository = RecordingAuthRepository { error("Save failed") }
            val viewModel = createViewModel(repository)
            viewModel.agreeAllAndMoveToNext()
            viewModel.updateRemainingDayOffInput("11")
            viewModel.updatePreferredDayOffInput("4")
            val editingState = viewModel.uiState.value
            val failure = async { viewModel.sideEffect.first() }
            runCurrent()

            viewModel.skipOnboardingLeaveDays()
            runCurrent()

            assertEquals(OnboardingSideEffect.OnboardingFailed, failure.await())
            assertEquals(editingState, viewModel.uiState.value)

            repository.save = {}
            viewModel.skipOnboardingLeaveDays()
            runCurrent()

            assertEquals(List(2) { SubmittedDayOff(remainingDayOff = 17, preferredDayOff = 2) }, repository.submissions)
            assertTrue(viewModel.uiState.value.isCompleted)
        }

    @Test
    fun cancelledSkipEmitsNeitherCompletionNorFailure() =
        runTest {
            val repository = RecordingAuthRepository { throw CancellationException() }
            val viewModel = createViewModel(repository)
            val effects = mutableListOf<OnboardingSideEffect>()
            backgroundScope.launch { viewModel.sideEffect.toList(effects) }
            runCurrent()

            viewModel.agreeAllAndMoveToNext()
            viewModel.skipOnboardingLeaveDays()
            runCurrent()

            assertFalse(viewModel.uiState.value.isCompleted)
            assertFalse(viewModel.uiState.value.isSubmitting)
            assertTrue(effects.isEmpty())
        }

    private fun createViewModel(repository: AuthRepository): OnboardingViewModel =
        OnboardingViewModel(
            validateOnboardingLeaveDaysUseCase = ValidateOnboardingLeaveDaysUseCase(),
            completeOnboardingUseCase = CompleteOnboardingUseCase(repository),
        )

    private data class SubmittedDayOff(
        val remainingDayOff: Int,
        val preferredDayOff: Int,
    )

    private class RecordingAuthRepository(
        var save: suspend () -> Unit = {},
    ) : AuthRepository {
        val submissions: MutableList<SubmittedDayOff> = mutableListOf()

        override suspend fun completeOnboarding(
            preferredDayOff: Int,
            remainingDayOff: Int,
        ) {
            submissions += SubmittedDayOff(remainingDayOff = remainingDayOff, preferredDayOff = preferredDayOff)
            save()
        }

        override suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): Boolean = error("Unexpected login")

        override suspend fun loginWithGoogleIdentityToken(identityToken: String): Boolean = error("Unexpected login")

        override suspend fun loginWithAppleIdentityToken(identityToken: String): Boolean = error("Unexpected login")

        override suspend fun getAutoLoginState(): AutoLoginState = error("Unexpected auto login")

        override fun clearTokens() = error("Unexpected token removal")
    }
}
