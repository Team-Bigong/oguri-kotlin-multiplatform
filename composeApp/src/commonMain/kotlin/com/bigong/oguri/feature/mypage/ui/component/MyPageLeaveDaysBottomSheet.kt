package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.dismissKeyboardOnOutsideTouch
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError
import com.bigong.oguri.domain.usecase.ValidateOnboardingLeaveDaysUseCase
import com.bigong.oguri.feature.onboarding.ui.component.OnboardingDayOffInputField
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_exit
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_done
import oguri.composeapp.generated.resources.mypage_leave_days_field_preferred
import oguri.composeapp.generated.resources.mypage_leave_days_field_remaining
import oguri.composeapp.generated.resources.mypage_leave_days_sheet_title
import oguri.composeapp.generated.resources.mypage_leave_days_unit
import oguri.composeapp.generated.resources.onboarding_day_off_error_max_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_error_positive
import oguri.composeapp.generated.resources.onboarding_day_off_error_preferred_exceeds_remaining
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageLeaveDaysBottomSheet(
    currentRemainingLeaveDays: Int,
    currentPreferredLeaveDays: Int,
    onDismissRequest: () -> Unit,
    onSubmit: (Int, Int) -> Unit,
) {
    val validateLeaveDaysUseCase = remember { ValidateOnboardingLeaveDaysUseCase() }
    var remainingLeaveDaysInput by rememberSaveable(currentRemainingLeaveDays) { mutableStateOf(currentRemainingLeaveDays.toString()) }
    var preferredLeaveDaysInput by rememberSaveable(currentPreferredLeaveDays) { mutableStateOf(currentPreferredLeaveDays.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(currentRemainingLeaveDays, currentPreferredLeaveDays) {
        remainingLeaveDaysInput = currentRemainingLeaveDays.toString()
        preferredLeaveDaysInput = currentPreferredLeaveDays.toString()
    }
    val remainingLeaveDaysValue = remainingLeaveDaysInput.toIntOrNull()
    val preferredLeaveDaysValue = preferredLeaveDaysInput.toIntOrNull()
    val validation =
        validateLeaveDaysUseCase(
            remainingDayOff = remainingLeaveDaysValue,
            preferredDayOff = preferredLeaveDaysValue,
        )
    val remainingLeaveDaysError: OnboardingLeaveDaysValidationError? =
        if (remainingLeaveDaysInput.isBlank()) {
            null
        } else {
            validation.remainingDayOffError
        }
    val preferredLeaveDaysError: OnboardingLeaveDaysValidationError? =
        if (preferredLeaveDaysInput.isBlank()) {
            null
        } else {
            validation.preferredDayOffError
        }
    val isSubmitEnabled =
        remainingLeaveDaysValue != null &&
            preferredLeaveDaysValue != null &&
            remainingLeaveDaysError == null &&
            preferredLeaveDaysError == null

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        dragHandle = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier =
                        Modifier
                            .size(width = 68.dp, height = 4.dp)
                            .background(color = Neutral50, shape = RoundedCornerShape(size = 100.dp)),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        containerColor = Neutral0,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .dismissKeyboardOnOutsideTouch()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.mypage_leave_days_sheet_title),
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral90,
                )
                Image(
                    painter = painterResource(resource = Res.drawable.btn_exit),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).noRippleClickable(onClick = onDismissRequest),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OnboardingDayOffInputField(
                labelText = stringResource(Res.string.mypage_leave_days_field_remaining),
                value = remainingLeaveDaysInput,
                onValueChange = { inputText ->
                    remainingLeaveDaysInput = inputText.filter { character -> character.isDigit() }.take(2)
                },
                unitText = stringResource(Res.string.mypage_leave_days_unit),
                warningText = remainingLeaveDaysError.toWarningText(),
                onInputCommitted = {},
            )

            Spacer(modifier = Modifier.height(18.dp))

            OnboardingDayOffInputField(
                labelText = stringResource(Res.string.mypage_leave_days_field_preferred),
                value = preferredLeaveDaysInput,
                onValueChange = { inputText ->
                    preferredLeaveDaysInput = inputText.filter { character -> character.isDigit() }.take(2)
                },
                unitText = stringResource(Res.string.mypage_leave_days_unit),
                warningText = preferredLeaveDaysError.toWarningText(),
                onInputCommitted = {},
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSubmitEnabled) Mint70 else Neutral30,
                            shape = RoundedCornerShape(size = 8.dp),
                        ).noRippleClickable(
                            onClick = {
                                val remainingLeaveDays = remainingLeaveDaysInput.toIntOrNull()
                                val preferredLeaveDays = preferredLeaveDaysInput.toIntOrNull()
                                if (
                                    isSubmitEnabled &&
                                    remainingLeaveDays != null &&
                                    preferredLeaveDays != null
                                ) {
                                    onSubmit(remainingLeaveDays, preferredLeaveDays)
                                }
                            },
                            enabled = isSubmitEnabled,
                        ).padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.calendar_leave_days_sheet_done),
                    style = OguriTheme.typography.cardTitle,
                    color = Neutral0,
                )
            }
        }
    }
}

@Composable
private fun OnboardingLeaveDaysValidationError?.toWarningText(): String? =
    when (this) {
        OnboardingLeaveDaysValidationError.DAY_OFF_MUST_BE_POSITIVE -> {
            stringResource(Res.string.onboarding_day_off_error_positive)
        }

        OnboardingLeaveDaysValidationError.REMAINING_DAY_OFF_EXCEEDS_MAX -> {
            stringResource(Res.string.onboarding_day_off_error_max_remaining)
        }

        OnboardingLeaveDaysValidationError.PREFERRED_DAY_OFF_EXCEEDS_REMAINING -> {
            stringResource(Res.string.onboarding_day_off_error_preferred_exceeds_remaining)
        }

        null -> {
            null
        }
    }
