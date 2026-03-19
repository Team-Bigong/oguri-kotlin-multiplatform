package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError as LeaveDaysValidationError
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_day_off_done
import oguri.composeapp.generated.resources.onboarding_day_off_error_max_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_error_positive
import oguri.composeapp.generated.resources.onboarding_day_off_error_preferred_exceeds_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_field_preferred
import oguri.composeapp.generated.resources.onboarding_day_off_field_remaining
import oguri.composeapp.generated.resources.onboarding_day_off_title_almost
import oguri.composeapp.generated.resources.onboarding_day_off_title_almost_highlight
import oguri.composeapp.generated.resources.onboarding_day_off_title_almost_subtitle
import oguri.composeapp.generated.resources.onboarding_day_off_title_done
import oguri.composeapp.generated.resources.onboarding_day_off_title_done_highlight
import oguri.composeapp.generated.resources.onboarding_day_off_title_done_subtitle
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome_highlight
import oguri.composeapp.generated.resources.onboarding_day_off_title_welcome_subtitle
import oguri.composeapp.generated.resources.onboarding_day_off_unit
import org.jetbrains.compose.resources.stringResource

private const val ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS = 240

@Composable
fun OnboardingLeaveDaysContent(
    remainingDayOffInput: String,
    preferredDayOffInput: String,
    remainingDayOffError: LeaveDaysValidationError?,
    preferredDayOffError: LeaveDaysValidationError?,
    onRemainingDayOffChange: (String) -> Unit,
    onRemainingDayOffCommit: () -> Unit,
    onPreferredDayOffChange: (String) -> Unit,
    onPreferredDayOffCommit: () -> Unit,
    isRemainingDayOffConfirmed: Boolean,
    isPreferredDayOffConfirmed: Boolean,
    isSubmitting: Boolean,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remainingDayOffValue = remainingDayOffInput.toIntOrNull()
    val preferredDayOffValue = preferredDayOffInput.toIntOrNull()
    val shouldShowPreferredField = isRemainingDayOffConfirmed
    val isDoneStage = isRemainingDayOffConfirmed && isPreferredDayOffConfirmed
    val isCompleteButtonEnabled =
        isDoneStage &&
            !isSubmitting &&
            remainingDayOffValue != null &&
            preferredDayOffValue != null &&
            remainingDayOffError == null &&
            preferredDayOffError == null
    val unitText = stringResource(Res.string.onboarding_day_off_unit)

    Column(modifier = modifier) {
        if (!shouldShowPreferredField) {
            Text(
                text = stringResource(Res.string.onboarding_day_off_title_welcome),
                style = OguriTheme.typography.cardTitle,
                color = Neutral90,
            )
            OnboardingHighlightedTitle(
                fullText = stringResource(Res.string.onboarding_day_off_title_welcome_subtitle),
                highlightedText = stringResource(Res.string.onboarding_day_off_title_welcome_highlight),
            )
        } else if (!isDoneStage) {
            Text(
                text = stringResource(Res.string.onboarding_day_off_title_almost),
                style = OguriTheme.typography.cardTitle,
                color = Neutral90,
            )
            OnboardingHighlightedTitle(
                fullText = stringResource(Res.string.onboarding_day_off_title_almost_subtitle),
                highlightedText = stringResource(Res.string.onboarding_day_off_title_almost_highlight),
            )
        } else {
            Text(
                text = stringResource(Res.string.onboarding_day_off_title_done),
                style = OguriTheme.typography.cardTitle,
                color = Neutral90,
            )
            OnboardingHighlightedTitle(
                fullText = stringResource(Res.string.onboarding_day_off_title_done_subtitle),
                highlightedText = stringResource(Res.string.onboarding_day_off_title_done_highlight),
            )
        }

        Spacer(modifier = Modifier.height(if (isDoneStage) 28.dp else 24.dp))

        Column(
            modifier =
                Modifier.animateContentSize(
                    animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS),
                ),
        ) {
            AnimatedVisibility(
                visible = shouldShowPreferredField,
                enter =
                    expandVertically(
                        animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
                exit = fadeOut(animationSpec = tween(durationMillis = ONBOARDING_CONTENT_ANIMATION_DURATION_MILLIS)),
            ) {
                Column {
                    OnboardingDayOffInputField(
                        labelText =
                            if (isDoneStage) {
                                stringResource(Res.string.onboarding_day_off_field_preferred)
                            } else {
                                null
                            },
                        value = preferredDayOffInput,
                        unitText = unitText,
                        warningText = preferredDayOffError.toWarningText(),
                        onValueChange = onPreferredDayOffChange,
                        onInputCommitted = onPreferredDayOffCommit,
                    )
                    Spacer(modifier = Modifier.height(if (isDoneStage) 14.dp else 16.dp))
                }
            }

            OnboardingDayOffInputField(
                labelText =
                    if (shouldShowPreferredField) {
                        stringResource(Res.string.onboarding_day_off_field_remaining)
                    } else {
                        null
                    },
                value = remainingDayOffInput,
                unitText = unitText,
                warningText =
                    if (shouldShowPreferredField && !isDoneStage) {
                        null
                    } else {
                        remainingDayOffError.toWarningText()
                    },
                onValueChange = onRemainingDayOffChange,
                onInputCommitted = onRemainingDayOffCommit,
            )

            AnimatedVisibility(
                visible = isDoneStage,
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
                                        if (isCompleteButtonEnabled) {
                                            Mint70
                                        } else {
                                            Neutral30
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .noRippleClickable(
                                    onClick = onCompleteClick,
                                    enabled = isCompleteButtonEnabled,
                                )
                                .padding(vertical = 14.dp),
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
}

@Composable
private fun LeaveDaysValidationError?.toWarningText(): String? =
    when (this) {
        LeaveDaysValidationError.DAY_OFF_MUST_BE_POSITIVE -> stringResource(Res.string.onboarding_day_off_error_positive)
        LeaveDaysValidationError.REMAINING_DAY_OFF_EXCEEDS_MAX -> stringResource(Res.string.onboarding_day_off_error_max_remaining)
        LeaveDaysValidationError.PREFERRED_DAY_OFF_EXCEEDS_REMAINING -> stringResource(Res.string.onboarding_day_off_error_preferred_exceeds_remaining)
        null -> null
    }
