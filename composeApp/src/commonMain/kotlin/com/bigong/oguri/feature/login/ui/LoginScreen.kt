package com.bigong.oguri.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.LoginProviderType
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderHeader
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingLarge
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall
import com.bigong.oguri.getPlatform
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.login_apple_start
import oguri.composeapp.generated.resources.login_guest_browse
import oguri.composeapp.generated.resources.login_guest_note
import oguri.composeapp.generated.resources.login_kakao_start
import oguri.composeapp.generated.resources.login_subtitle
import oguri.composeapp.generated.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginRoute(
    onLoginClick: (LoginProviderType) -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    LoginScreen(
        onLoginClick = onLoginClick,
        onGuestBrowseClick = onGuestBrowseClick,
    )
}

@Composable
fun LoginScreen(
    onLoginClick: (LoginProviderType) -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    val isIosPlatform: Boolean = getPlatform().name.contains(other = "iOS", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingLarge),
    ) {
        PlaceholderHeader(
            screenTitleText = stringResource(Res.string.login_title),
            screenSubtitleText = stringResource(Res.string.login_subtitle),
        )

        PlaceholderSectionCard {
            PlaceholderActionButton(
                labelText = stringResource(Res.string.login_kakao_start),
                onClick = { onLoginClick(LoginProviderType.KAKAO) },
            )
            if (isIosPlatform) {
                Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
                PlaceholderActionButton(
                    labelText = stringResource(Res.string.login_apple_start),
                    onClick = { onLoginClick(LoginProviderType.APPLE) },
                    emphasized = false,
                )
            }
            Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
            PlaceholderActionButton(
                labelText = stringResource(Res.string.login_guest_browse),
                onClick = onGuestBrowseClick,
                emphasized = false,
            )
            Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
            androidx.compose.material3.Text(
                text = stringResource(Res.string.login_guest_note),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.caption,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
