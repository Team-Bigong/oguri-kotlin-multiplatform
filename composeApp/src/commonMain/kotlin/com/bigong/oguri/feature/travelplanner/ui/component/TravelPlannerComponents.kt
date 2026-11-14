package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral10
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.CenteredLoadingIndicator
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanFailure
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.common_network_error_message
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_back
import oguri.composeapp.generated.resources.travel_conflict
import oguri.composeapp.generated.resources.travel_invalid_request
import oguri.composeapp.generated.resources.travel_invite_expired
import oguri.composeapp.generated.resources.travel_invite_revoked
import oguri.composeapp.generated.resources.travel_invite_trip_deleted
import oguri.composeapp.generated.resources.travel_rate_limit
import oguri.composeapp.generated.resources.travel_sign_in_required
import oguri.composeapp.generated.resources.travel_unavailable
import oguri.composeapp.generated.resources.travel_undo_unavailable
import oguri.composeapp.generated.resources.travel_visit_limit
import oguri.composeapp.generated.resources.travel_visit_removed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelPlannerPage(
    title: String,
    onBack: (() -> Unit)?,
    isSaving: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlatformBackHandler(enabled = isSaving) {}
    PlatformBackGestureContainer(enabled = !isSaving && onBack != null, onBack = { onBack?.invoke() }) {
        Box(modifier.fillMaxSize().background(Neutral5)) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Neutral0).heightIn(min = 48.dp).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        TravelIconButton(onClick = onBack, enabled = !isSaving) {
                            Icon(painterResource(Res.drawable.btn_back), stringResource(Res.string.travel_back), tint = Neutral100)
                        }
                    }
                    Text(
                        title,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    actions()
                }
                HorizontalDivider(color = Neutral20)
                content()
            }
            if (isSaving) CenteredLoadingIndicator()
        }
    }
}

@Composable
internal fun TravelPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    TravelButton(onClick, modifier, enabled, if (enabled) Mint70 else Neutral10, content = content)
}

@Composable
internal fun TravelTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    TravelButton(onClick, modifier, enabled, Color.Transparent, content = content)
}

@Composable
internal fun TravelOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    TravelButton(onClick, modifier, enabled, Neutral0, outlined = true, content = content)
}

@Composable
internal fun TravelIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.size(48.dp).semantics { role = Role.Button }.noRippleClickable(
            onClick = onClick,
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
        ),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
private fun TravelButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    backgroundColor: Color,
    outlined: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier.heightIn(min = 48.dp).background(backgroundColor, shape)
            .then(if (outlined) Modifier.border(1.dp, Neutral20, shape) else Modifier)
            .semantics { role = Role.Button }
            .noRippleClickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
            ).padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides if (enabled) Neutral100 else Neutral50) {
            ProvideTextStyle(OguriTheme.typography.labelLarge, content)
        }
    }
}

@Composable
internal fun TravelFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Neutral90,
    unfocusedTextColor = Neutral90,
    focusedBorderColor = Neutral90,
    unfocusedBorderColor = Neutral20,
    focusedLabelColor = Neutral90,
    unfocusedLabelColor = Neutral50,
    cursorColor = Neutral90,
)

@Composable
internal fun TravelLoading(modifier: Modifier = Modifier) {
    SkeletonBox(modifier.height(20.dp))
}

@Composable
internal fun TravelLoadFailure(
    failure: TravelPlanFailure,
    onRetry: () -> Unit,
) {
    if (failure == TravelPlanFailure.CONNECTION) {
        NetworkErrorRetryContent(onRetryClick = onRetry)
    } else {
        TravelInlineFailure(failure)
    }
}

@Composable
internal fun TravelInlineFailure(
    failure: TravelPlanFailure,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = stringResource(Res.string.common_retry_button),
    message: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text =
                message ?: stringResource(
                    when (failure) {
                        TravelPlanFailure.SIGN_IN_REQUIRED -> Res.string.travel_sign_in_required
                        TravelPlanFailure.UNAVAILABLE -> Res.string.travel_unavailable
                        TravelPlanFailure.CONFLICT -> Res.string.travel_conflict
                        TravelPlanFailure.VISIT_REMOVED -> Res.string.travel_visit_removed
                        TravelPlanFailure.UNDO_UNAVAILABLE -> Res.string.travel_undo_unavailable
                        TravelPlanFailure.VISIT_LIMIT -> Res.string.travel_visit_limit
                        TravelPlanFailure.RATE_LIMIT -> Res.string.travel_rate_limit
                        TravelPlanFailure.INVALID -> Res.string.travel_invalid_request
                        TravelPlanFailure.INVITE_EXPIRED -> Res.string.travel_invite_expired
                        TravelPlanFailure.INVITE_REVOKED -> Res.string.travel_invite_revoked
                        TravelPlanFailure.INVITE_TRIP_DELETED -> Res.string.travel_invite_trip_deleted
                        TravelPlanFailure.CONNECTION -> Res.string.common_network_error_message
                    },
                ),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (onRetry != null) TravelTextButton(onClick = onRetry) { Text(retryLabel, color = OguriTheme.colors.textPrimary) }
    }
}
