package com.bigong.oguri.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import com.bigong.oguri.core.navigation.WebDocumentType
import com.bigong.oguri.domain.usecase.ValidateOnboardingLeaveDaysUseCase
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingSideEffect
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Inject
class OnboardingViewModel(
    private val validateOnboardingLeaveDaysUseCase: ValidateOnboardingLeaveDaysUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<OnboardingSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

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
            currentUiState.copy(
                remainingDayOffInput = sanitizedInput,
                remainingDayOffError = null,
            )
        }
    }

    fun updatePreferredDayOffInput(inputText: String) {
        val sanitizedInput = sanitizeDayOffInput(inputText)
        _uiState.update { currentUiState ->
            currentUiState.copy(
                preferredDayOffInput = sanitizedInput,
                preferredDayOffError = null,
            )
        }
    }

    fun commitRemainingDayOffInput() {
        val currentUiState = uiState.value
        val remainingDayOff = currentUiState.remainingDayOffInput.toIntOrNull()
        val preferredDayOff = currentUiState.preferredDayOffInput.toIntOrNull()
        val validation =
            validateOnboardingLeaveDaysUseCase(
                remainingDayOff = remainingDayOff,
                preferredDayOff = preferredDayOff,
            )

        _uiState.update {
            it.copy(
                isRemainingDayOffConfirmed =
                    it.isRemainingDayOffConfirmed ||
                        (remainingDayOff != null && validation.remainingDayOffError == null),
                remainingDayOffError = if (currentUiState.remainingDayOffInput.isBlank()) null else validation.remainingDayOffError,
                preferredDayOffError =
                    if (currentUiState.preferredDayOffInput.isBlank()) {
                        null
                    } else {
                        validation.preferredDayOffError
                    },
                isPreferredDayOffConfirmed =
                    if (currentUiState.preferredDayOffInput.isBlank()) {
                        it.isPreferredDayOffConfirmed
                    } else {
                        it.isPreferredDayOffConfirmed && validation.preferredDayOffError == null
                    },
            )
        }
    }

    fun commitPreferredDayOffInput() {
        val currentUiState = uiState.value
        val remainingDayOff = currentUiState.remainingDayOffInput.toIntOrNull()
        val preferredDayOff = currentUiState.preferredDayOffInput.toIntOrNull()
        val validation =
            validateOnboardingLeaveDaysUseCase(
                remainingDayOff = remainingDayOff,
                preferredDayOff = preferredDayOff,
            )

        _uiState.update {
            it.copy(
                isPreferredDayOffConfirmed =
                    it.isPreferredDayOffConfirmed ||
                        (preferredDayOff != null && validation.preferredDayOffError == null),
                preferredDayOffError = if (currentUiState.preferredDayOffInput.isBlank()) null else validation.preferredDayOffError,
                remainingDayOffError = if (currentUiState.remainingDayOffInput.isBlank()) null else validation.remainingDayOffError,
            )
        }
    }

    fun completeOnboarding() {
        val remainingDayOff = uiState.value.remainingDayOffInput.toIntOrNull()
        val preferredDayOff = uiState.value.preferredDayOffInput.toIntOrNull()
        val validation =
            validateOnboardingLeaveDaysUseCase(
                remainingDayOff = remainingDayOff,
                preferredDayOff = preferredDayOff,
            )
        val canComplete = validation.isValid && remainingDayOff != null && preferredDayOff != null
        if (!canComplete) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    remainingDayOffError = validation.remainingDayOffError,
                    preferredDayOffError = validation.preferredDayOffError,
                )
            }
            return
        }
        _sideEffect.tryEmit(OnboardingSideEffect.NavigateToHome)
    }

    private fun sanitizeDayOffInput(inputText: String): String = inputText.filter { character -> character.isDigit() }.take(2)
}
