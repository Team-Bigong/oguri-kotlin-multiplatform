package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderRowItem
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSelectableChip
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.mypage_faq
import oguri.composeapp.generated.resources.mypage_line_leave
import oguri.composeapp.generated.resources.mypage_line_work_pattern
import oguri.composeapp.generated.resources.mypage_login_info_apple
import oguri.composeapp.generated.resources.mypage_login_info_guest
import oguri.composeapp.generated.resources.mypage_login_info_kakao
import oguri.composeapp.generated.resources.mypage_login_info_none
import oguri.composeapp.generated.resources.mypage_login_info_title
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
import oguri.composeapp.generated.resources.onboarding_leave_days_value
import oguri.composeapp.generated.resources.onboarding_work_schedule_five
import oguri.composeapp.generated.resources.onboarding_work_schedule_six
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AccountSection(
    userState: UserState,
    onIncreaseAnnualLeave: () -> Unit,
    onDecreaseAnnualLeave: () -> Unit,
) {
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
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(labelText = "-1", onClick = onDecreaseAnnualLeave, emphasized = false)
            }
            Box(modifier = Modifier.weight(1f)) {
                PlaceholderActionButton(labelText = "+1", onClick = onIncreaseAnnualLeave, emphasized = false)
            }
        }
        PlaceholderRowItem(
            titleText = stringResource(Res.string.mypage_login_info_title),
            trailingText = loginProviderLabelText(userState.loginProviderType),
        )
    }
}

@Composable
internal fun StrategySettingsSection(
    userState: UserState,
    onToggleWorkSchedule: () -> Unit,
    onDummyActionClick: (String) -> Unit,
) {
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

@Composable
internal fun ProSection(
    isProSubscribed: Boolean,
    onTogglePro: () -> Unit,
) {
    PlaceholderSectionCard {
        PlaceholderSectionTitle(text = stringResource(Res.string.mypage_section_pro))
        PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_ad_remove))
        PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_pdf))
        PlaceholderRowItem(titleText = stringResource(Res.string.mypage_pro_analysis))
        PlaceholderActionButton(
            labelText = if (isProSubscribed) {
                stringResource(Res.string.mypage_pro_manage)
            } else {
                stringResource(Res.string.mypage_pro_start)
            },
            onClick = onTogglePro,
            emphasized = !isProSubscribed,
        )
    }
}

@Composable
internal fun SupportInfoSection(
    onSupportInquiryClick: () -> Unit,
    onDummyActionClick: (String) -> Unit,
) {
    val noticeText: String = stringResource(Res.string.mypage_notice)
    val faqText: String = stringResource(Res.string.mypage_faq)
    val suggestText: String = stringResource(Res.string.mypage_suggest)
    val termsText: String = stringResource(Res.string.mypage_terms)
    val privacyText: String = stringResource(Res.string.mypage_privacy)
    val openSourceText: String = stringResource(Res.string.mypage_open_source)

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

@Composable
internal fun loginProviderLabelText(loginProviderType: LoginProviderType): String {
    return when (loginProviderType) {
        LoginProviderType.NONE -> stringResource(Res.string.mypage_login_info_none)
        LoginProviderType.GUEST -> stringResource(Res.string.mypage_login_info_guest)
        LoginProviderType.KAKAO -> stringResource(Res.string.mypage_login_info_kakao)
        LoginProviderType.APPLE -> stringResource(Res.string.mypage_login_info_apple)
    }
}
