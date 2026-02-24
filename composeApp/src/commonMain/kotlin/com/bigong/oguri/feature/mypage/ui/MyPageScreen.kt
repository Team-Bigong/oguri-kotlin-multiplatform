package com.bigong.oguri.feature.mypage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.mypage.ui.component.AccountSection
import com.bigong.oguri.feature.mypage.ui.component.ProSection
import com.bigong.oguri.feature.mypage.ui.component.StrategySettingsSection
import com.bigong.oguri.feature.mypage.ui.component.SupportInfoSection
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.mypage_logout
import oguri.composeapp.generated.resources.mypage_title
import oguri.composeapp.generated.resources.mypage_user_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageScreen(
    uiState: MyPageUiState,
    onIncreaseAnnualLeave: () -> Unit,
    onDecreaseAnnualLeave: () -> Unit,
    onToggleWorkSchedule: () -> Unit,
    onTogglePro: () -> Unit,
    onSupportInquiryClick: () -> Unit,
    onDummyActionClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
) {
    val userState = uiState.userState

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
            AccountSection(
                userState = userState,
                onIncreaseAnnualLeave = onIncreaseAnnualLeave,
                onDecreaseAnnualLeave = onDecreaseAnnualLeave,
            )
        }
        item {
            StrategySettingsSection(
                userState = userState,
                onToggleWorkSchedule = onToggleWorkSchedule,
                onDummyActionClick = onDummyActionClick,
            )
        }
        item {
            ProSection(
                isProSubscribed = userState.isProSubscribed,
                onTogglePro = onTogglePro,
            )
        }
        item {
            SupportInfoSection(
                onSupportInquiryClick = onSupportInquiryClick,
                onDummyActionClick = onDummyActionClick,
            )
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
