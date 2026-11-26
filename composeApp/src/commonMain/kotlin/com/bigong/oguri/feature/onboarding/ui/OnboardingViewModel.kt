package com.bigong.oguri.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.core.navigation.WebDocumentType
import com.bigong.oguri.domain.usecase.CompleteOnboardingUseCase
import com.bigong.oguri.domain.usecase.ValidateOnboardingLeaveDaysUseCase
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingSideEffect
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class OnboardingViewModel(
    private val validateOnboardingLeaveDaysUseCase: ValidateOnboardingLeaveDaysUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<OnboardingSideEffect>(extraBufferCapacity = 1)
    val sideEffect: SharedFlow<OnboardingSideEffect> = _sideEffect.asSharedFlow()

    fun toggleServiceTermsChecked() {
        _uiState.update { currentUiState ->
            currentUiState.copy(isServiceTermsChecked = !currentUiState.isServiceTermsChecked)
        }
    }

    fun togglePrivacyPolicyChecked() {
        _uiState.update { currentUiState ->
            currentUiState.copy(isPrivacyPolicyChecked = !currentUiState.isPrivacyPolicyChecked)
        }
    }

    fun openTermsOfService() {
        _sideEffect.tryEmit(OnboardingSideEffect.OpenWebDocument(WebDocumentType.TERMS_OF_SERVICE))
    }

    fun openPrivacyPolicy() {
        _sideEffect.tryEmit(OnboardingSideEffect.OpenWebDocument(WebDocumentType.PRIVACY_POLICY))
    }

    fun agreeAllAndMoveToNext() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isServiceTermsChecked = true,
                isPrivacyPolicyChecked = true,
                step = OnboardingStep.LEAVE_DAYS,
            )
        }
    }

    fun onBackFromLeaveDays() {
        _uiState.update { currentUiState ->
            currentUiState.copy(step = OnboardingStep.TERMS)
        }
    }

    fun updateRemainingDayOffInput(inputText: String) {
        val sanitizedInput = sanitizeDayOffInput(inputText)
        _uiState.update { currentUiState ->
            currentUiState.copy(remainingDayOffInput = sanitizedInput).withValidatedDayOffInputs()
        }
    }

    fun updatePreferredDayOffInput(inputText: String) {
        val sanitizedInput = sanitizeDayOffInput(inputText)
        _uiState.update { currentUiState ->
            currentUiState.copy(preferredDayOffInput = sanitizedInput).withValidatedDayOffInputs()
        }
    }

    fun completeOnboarding() {
        val currentUiState = uiState.value
        if (!currentUiState.canStartSubmission || !currentUiState.hasValidDayOffInputs) return

        val remainingDayOff = currentUiState.remainingDayOffInput.toIntOrNull() ?: return
        val preferredDayOff = currentUiState.preferredDayOffInput.toIntOrNull() ?: return
        completeOnboardingWithDayOff(
            remainingDayOff = remainingDayOff,
            preferredDayOff = preferredDayOff,
        )
    }

    fun skipOnboardingLeaveDays() {
        completeOnboardingWithDayOff(
            remainingDayOff = ValidateOnboardingLeaveDaysUseCase.DEFAULT_REMAINING_DAY_OFF,
            preferredDayOff = ValidateOnboardingLeaveDaysUseCase.DEFAULT_PREFERRED_DAY_OFF,
        )
    }

    private fun completeOnboardingWithDayOff(
        remainingDayOff: Int,
        preferredDayOff: Int,
    ) {
        if (!startSubmitting()) {
            return
        }
        viewModelScope.launch {
            var isCompleted = false
            try {
                completeOnboardingUseCase(
                    preferredDayOff = preferredDayOff,
                    remainingDayOff = remainingDayOff,
                )
                ensureActive()
                isCompleted = true
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _sideEffect.tryEmit(OnboardingSideEffect.OnboardingFailed)
            } finally {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSubmitting = false, isCompleted = isCompleted)
                }
            }
        }
    }

    private fun OnboardingUiState.withValidatedDayOffInputs(): OnboardingUiState {
        val validation =
            validateOnboardingLeaveDaysUseCase(
                remainingDayOff = remainingDayOffInput.toIntOrNull(),
                preferredDayOff = preferredDayOffInput.toIntOrNull(),
            )
        return copy(
            remainingDayOffError = validation.remainingDayOffError,
            preferredDayOffError = validation.preferredDayOffError,
        )
    }

    private fun startSubmitting(): Boolean =
        _uiState
            .getAndUpdate { currentUiState ->
                if (currentUiState.canStartSubmission) currentUiState.copy(isSubmitting = true) else currentUiState
            }.canStartSubmission

    private fun sanitizeDayOffInput(inputText: String): String = inputText.filter { character -> character.isDigit() }.take(2)
}
