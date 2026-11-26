package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_day_off_done
import oguri.composeapp.generated.resources.onboarding_day_off_error_max_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_error_positive
import oguri.composeapp.generated.resources.onboarding_day_off_error_preferred_exceeds_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_field_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_title_almost_subtitle
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome_highlight
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome_subtitle
import oguri.composeapp.generated.resources.onboarding_day_off_unit
import org.jetbrains.compose.resources.stringResource
import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError as LeaveDaysValidationError

private const val REMAINING_DAY_OFF_HINT: Int = 15
private const val ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS = 240

@Composable
fun OnboardingLeaveDaysContent(
    remainingDayOffInput: String,
    preferredDayOffInput: String,
    remainingDayOffError: LeaveDaysValidationError?,
    preferredDayOffError: LeaveDaysValidationError?,
    onRemainingDayOffChange: (String) -> Unit,
    onPreferredDayOffChange: (String) -> Unit,
    shouldShowPreferredDayOffInput: Boolean,
    shouldShowCompleteButton: Boolean,
    isCompleteButtonEnabled: Boolean,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val unitText = stringResource(Res.string.onboarding_day_off_unit)
    val canComplete = shouldShowCompleteButton && isCompleteButtonEnabled

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(Res.string.onboarding_day_off_title_welcome),
            style = OguriTheme.typography.cardTitle,
            color = Neutral90,
        )
        OnboardingHighlightedTitle(
            fullText = stringResource(Res.string.onboarding_day_off_title_welcome_subtitle),
            highlightedText = stringResource(Res.string.onboarding_day_off_title_welcome_highlight),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingDayOffInputField(
            labelText = stringResource(Res.string.onboarding_day_off_field_remaining),
            showLabel = false,
            value = remainingDayOffInput,
            placeholderText = REMAINING_DAY_OFF_HINT.toString(),
            unitText = unitText,
            warningText = remainingDayOffError.toWarningText(),
            onValueChange = onRemainingDayOffChange,
        )

        AnimatedVisibility(
            visible = shouldShowPreferredDayOffInput,
            enter =
                expandVertically(
                    animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
            exit = fadeOut(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OnboardingDayOffInputField(
                    labelText = stringResource(Res.string.onboarding_day_off_title_almost_subtitle),
                    value = preferredDayOffInput,
                    unitText = unitText,
                    warningText = preferredDayOffError.toWarningText(),
                    onValueChange = onPreferredDayOffChange,
                )
            }
        }

        AnimatedVisibility(
            visible = shouldShowCompleteButton,
            enter = fadeIn(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
            exit = fadeOut(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
        ) {
            Column {
                Spacer(modifier = Modifier.height(26.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color =
                                    if (canComplete) {
                                        Mint70
                                    } else {
                                        Neutral30
                                    },
                                shape = RoundedCornerShape(8.dp),
                            ).noRippleClickable(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    onCompleteClick()
                                },
                                enabled = canComplete,
                            ).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_day_off_done),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral0,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaveDaysValidationError?.toWarningText(): String? =
    when (this) {
        LeaveDaysValidationError.DAY_OFF_MUST_BE_POSITIVE -> stringResource(Res.string.onboarding_day_off_error_positive)
        LeaveDaysValidationError.REMAINING_DAY_OFF_EXCEEDS_MAX -> stringResource(Res.string.onboarding_day_off_error_max_remaining)
        LeaveDaysValidationError.PREFERRED_DAY_OFF_EXCEEDS_REMAINING ->
            stringResource(
                Res.string.onboarding_day_off_error_preferred_exceeds_remaining,
            )
        null -> null
    }
