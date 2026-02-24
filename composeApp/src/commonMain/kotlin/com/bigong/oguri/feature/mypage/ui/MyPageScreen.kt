package com.bigong.oguri.feature.mypage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderRowItem
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import kotlinx.coroutines.flow.StateFlow
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.mypage_faq
import oguri.composeapp.generated.resources.mypage_line_leave
import oguri.composeapp.generated.resources.mypage_line_work_pattern
import oguri.composeapp.generated.resources.mypage_login_info_apple
import oguri.composeapp.generated.resources.mypage_login_info_guest
import oguri.composeapp.generated.resources.mypage_login_info_kakao
import oguri.composeapp.generated.resources.mypage_login_info_none
import oguri.composeapp.generated.resources.mypage_login_info_title
import oguri.composeapp.generated.resources.mypage_logout
import oguri.composeapp.generated.resources.mypage_notice
import oguri.composeapp.generated.resources.mypage_open_source
import oguri.composeapp.generated.resources.mypage_privacy
import oguri.composeapp.generated.resources.mypage_pro_ad_remove
import oguri.composeapp.generated.resources.mypage_pro_analysis
import oguri.composeapp.generated.resources.mypage_pro_manage
import oguri.composeapp.generated.resources.mypage_pro_pdf
import oguri.composeapp.generated.resources.mypage_pro_start
import oguri.composeapp.generated.resources.mypage_saved_strategy_count
import oguri.composeapp.generated.resources.mypage_saved_strategy_title
import oguri.composeapp.generated.resources.mypage_section_account
import oguri.composeapp.generated.resources.mypage_section_pro
import oguri.composeapp.generated.resources.mypage_section_strategy_settings
import oguri.composeapp.generated.resources.mypage_section_support_info
import oguri.composeapp.generated.resources.mypage_suggest
import oguri.composeapp.generated.resources.mypage_terms
import oguri.composeapp.generated.resources.mypage_title
import oguri.composeapp.generated.resources.mypage_user_title
import oguri.composeapp.generated.resources.onboarding_leave_days_value
import oguri.composeapp.generated.resources.onboarding_work_schedule_five
import oguri.composeapp.generated.resources.onboarding_work_schedule_six
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageRoute(
    userStateRepository: UserStateRepository,
    onSupportInquiryClick: () -> Unit,
    onShowDummySnackbar: (String) -> Unit,
    onShowProToggleSnackbar: (Boolean) -> Unit,
    onShowLogoutSnackbar: () -> Unit,
) {
    val userStateFlow: StateFlow<UserState> = userStateRepository.userStateFlow
    val userState: UserState by userStateFlow.collectAsState()

    MyPageScreen(
        userState = userState,
        onIncreaseAnnualLeave = {
            userStateRepository.updateRemainingAnnualLeaveDays(userState.remainingAnnualLeaveDays + 1)
        },
        onDecreaseAnnualLeave = {
            userStateRepository.updateRemainingAnnualLeaveDays(userState.remainingAnnualLeaveDays - 1)
        },
        onToggleWorkSchedule = {
            val nextWorkScheduleType = if (userState.workScheduleType == WorkScheduleType.FIVE_DAYS) {
                WorkScheduleType.SIX_DAYS
            } else {
                WorkScheduleType.FIVE_DAYS
            }
            userStateRepository.updateWorkScheduleType(nextWorkScheduleType)
        },
        onTogglePro = {
            userStateRepository.toggleProSubscription()
            onShowProToggleSnackbar(!userState.isProSubscribed)
        },
        onSupportInquiryClick = onSupportInquiryClick,
        onDummyActionClick = onShowDummySnackbar,
        onLogoutClick = {
            userStateRepository.logout()
            onShowLogoutSnackbar()
        },
    )
}

@Composable
fun MyPageScreen(
    userState: UserState,
    onIncreaseAnnualLeave: () -> Unit,
    onDecreaseAnnualLeave: () -> Unit,
    onToggleWorkSchedule: () -> Unit,
    onTogglePro: () -> Unit,
    onSupportInquiryClick: () -> Unit,
    onDummyActionClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
) {
    val noticeText: String = stringResource(Res.string.mypage_notice)
    val faqText: String = stringResource(Res.string.mypage_faq)
    val suggestText: String = stringResource(Res.string.mypage_suggest)
    val termsText: String = stringResource(Res.string.mypage_terms)
    val privacyText: String = stringResource(Res.string.mypage_privacy)
    val openSourceText: String = stringResource(Res.string.mypage_open_source)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        item {
            PlaceholderHeader(
                screenTitleText = stringResource(Res.string.mypage_user_title),
                screenSubtitleText = stringResource(Res.string.mypage_title),
            )
        }
        item {
            PlaceholderSectionCard {
                PlaceholderSectionTitle(text = stringResource(Res.string.mypage_section_account))
                PlaceholderRowItem(
                    titleText = stringResource(Res.string.mypage_line_leave),
                    trailingText = stringResource(Res.string.onboarding_leave_days_value, userState.remainingAnnualLeaveDays),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
                ) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        PlaceholderActionButton(labelText = "-1", onClick = onDecreaseAnnualLeave, emphasized = false)
                    }
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        PlaceholderActionButton(labelText = "+1", onClick = onIncreaseAnnualLeave, emphasized = false)
                    }
                }
                PlaceholderRowItem(
                    titleText = stringResource(Res.string.mypage_login_info_title),
                    trailingText = loginProviderLabelText(userState.loginProviderType),
                )
            }
        }
        item {
            PlaceholderSectionCard {
                PlaceholderSectionTitle(text = stringResource(Res.string.mypage_section_strategy_settings))
                PlaceholderRowItem(
                    titleText = stringResource(Res.string.mypage_saved_strategy_title),
                    trailingText = stringResource(Res.string.mypage_saved_strategy_count, userState.savedStrategyIdentifierList.size),
                )
                userState.savedStrategyIdentifierList.take(3).forEach { strategyIdentifier: String ->
                    PlaceholderRowItem(titleText = strategyIdentifier, onClick = { onDummyActionClick(strategyIdentifier) })
                }
                PlaceholderRowItem(titleText = stringResource(Res.string.mypage_line_work_pattern))
                Row(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall)) {
                    PlaceholderSelectableChip(
                        labelText = stringResource(Res.string.onboarding_work_schedule_five),
                        isSelected = userState.workScheduleType == WorkScheduleType.FIVE_DAYS,
                        onClick = onToggleWorkSchedule,
                        modifier = Modifier.weight(1f),
                    )
                    PlaceholderSelectableChip(
                        labelText = stringResource(Res.string.onboarding_work_schedule_six),
                        isSelected = userState.workScheduleType == WorkScheduleType.SIX_DAYS,
                        onClick = onToggleWorkSchedule,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            PlaceholderSectionCard {
                PlaceholderSectionTitle(text = stringResource(Res.string.mypage_section_pro))
                PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_ad_remove))
                PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_pdf))
                PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_analysis))
                PlaceholderActionButton(
                    labelText = if (userState.isProSubscribed) {
                        stringResource(Res.string.mypage_pro_manage)
                    } else {
                        stringResource(Res.string.mypage_pro_start)
                    },
                    onClick = onTogglePro,
                    emphasized = !userState.isProSubscribed,
                )
            }
        }
        item {
            PlaceholderSectionCard {
                PlaceholderSectionTitle(text = stringResource(Res.string.mypage_section_support_info))
                PlaceholderRowItem(titleText = noticeText, onClick = { onDummyActionClick(noticeText) })
                PlaceholderRowItem(titleText = faqText, onClick = { onDummyActionClick(faqText) })
                PlaceholderRowItem(titleText = suggestText, onClick = onSupportInquiryClick)
                PlaceholderRowItem(titleText = termsText, onClick = { onDummyActionClick(termsText) })
                PlaceholderRowItem(titleText = privacyText, onClick = { onDummyActionClick(privacyText) })
                PlaceholderRowItem(titleText = openSourceText, onClick = { onDummyActionClick(openSourceText) })
            }
        }
        item {
            PlaceholderActionButton(
                labelText = stringResource(Res.string.mypage_logout),
                onClick = onLogoutClick,
                emphasized = false,
            )
        }
    }
}

@Composable
private fun loginProviderLabelText(loginProviderType: LoginProviderType): String {
    return when (loginProviderType) {
        LoginProviderType.NONE -> stringResource(Res.string.mypage_login_info_none)
        LoginProviderType.GUEST -> stringResource(Res.string.mypage_login_info_guest)
        LoginProviderType.KAKAO -> stringResource(Res.string.mypage_login_info_kakao)
        LoginProviderType.APPLE -> stringResource(Res.string.mypage_login_info_apple)
    }
}
