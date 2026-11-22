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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigong.oguri.core.designsystem.LightOguriColorPalette
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.CenteredLoadingIndicator
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.TravelPlanFailure
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_network_error_message
import oguri.composeapp.generated.resources.travel_retry
import oguri.composeapp.generated.resources.ic_travel_back
import oguri.composeapp.generated.resources.travel_back
import oguri.composeapp.generated.resources.travel_conflict_title
import oguri.composeapp.generated.resources.travel_conflict_message
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

// The filled mint accent uses the same dark foreground in both palettes.
internal val travelAccentForeground: Color = LightOguriColorPalette.textPrimary

internal val TravelSheetScrim: Color = Color.Black.copy(alpha = 0.45f)

@Composable
internal fun TravelPlannerPage(
    title: String,
    onBack: (() -> Unit)?,
    isSaving: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    showTopDivider: Boolean = true,
    modifier: Modifier = Modifier,
    loadingTitle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlatformBackHandler(enabled = isSaving) {}
    PlatformBackGestureContainer(enabled = !isSaving && onBack != null, onBack = { onBack?.invoke() }) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = OguriTheme.colors.backgroundSecondary,
            contentColor = OguriTheme.colors.textPrimary,
        ) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                if (onBack == null && title.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TravelPageTitle(title, Modifier.weight(1f).padding(horizontal = 8.dp))
                        actions()
                    }
                } else if (onBack != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (loadingTitle) {
                            SkeletonBox(Modifier.size(width = 96.dp, height = 18.dp), RoundedCornerShape(4.dp))
                        } else {
                            TravelPageTitle(title, Modifier.fillMaxWidth().padding(horizontal = 72.dp), TextAlign.Center)
                        }
                        TravelIconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.CenterStart),
                            enabled = !isSaving,
                        ) {
                            Icon(painterResource(Res.drawable.ic_travel_back), stringResource(Res.string.travel_back), tint = OguriTheme.colors.textPrimary)
                        }
                        Row(Modifier.align(Alignment.CenterEnd), content = actions)
                    }
                }
                if (showTopDivider && (onBack != null || title.isNotEmpty())) HorizontalDivider(color = OguriTheme.colors.borderSubtle)
                content()
            }
            if (isSaving) CenteredLoadingIndicator()
        }
    }
}

@Composable
private fun TravelPageTitle(title: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        title,
        modifier = modifier,
        textAlign = textAlign,
        style = travelTextStyle(18, 21, FontWeight.SemiBold),
        color = OguriTheme.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun TravelPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = OguriTheme.typography.cardTitle,
    content: @Composable () -> Unit,
) {
    TravelButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = if (!enabled) OguriTheme.colors.backgroundTertiary else OguriTheme.colors.mint,
        contentColor = travelAccentForeground,
        minHeight = 45.dp,
        textStyle = textStyle,
        content = content,
    )
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
    TravelButton(
        onClick, modifier, enabled, OguriTheme.colors.mintContainer, borderColor = OguriTheme.colors.mint,
        textStyle = travelTextStyle(18, 22, FontWeight.SemiBold),
        contentColor = OguriTheme.colors.textPrimary,
        minHeight = 45.dp,
        content = content,
    )
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
    borderColor: Color? = null,
    textStyle: TextStyle = OguriTheme.typography.labelLarge,
    contentColor: Color = OguriTheme.colors.textPrimary,
    minHeight: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier.heightIn(min = minHeight).background(backgroundColor, shape)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .semantics { role = Role.Button }
            .noRippleClickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
            ).padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides if (enabled) contentColor else OguriTheme.colors.textTertiary) {
            ProvideTextStyle(textStyle, content)
        }
    }
}

@Composable
internal fun TravelFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedPlaceholderColor = OguriTheme.colors.textTertiary,
    unfocusedPlaceholderColor = OguriTheme.colors.textTertiary,
    focusedTextColor = OguriTheme.colors.textPrimary,
    unfocusedTextColor = OguriTheme.colors.textPrimary,
    focusedBorderColor = OguriTheme.colors.textPrimary,
    unfocusedBorderColor = OguriTheme.colors.borderSubtle,
    focusedLabelColor = OguriTheme.colors.textPrimary,
    unfocusedLabelColor = OguriTheme.colors.textTertiary,
    cursorColor = OguriTheme.colors.textPrimary,
    focusedContainerColor = OguriTheme.colors.surfaceCard,
    unfocusedContainerColor = OguriTheme.colors.surfaceCard,
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
    retryLabel: String = stringResource(Res.string.travel_retry),
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    val isConflict = failure == TravelPlanFailure.CONFLICT && message == null
    TravelIssueCard(
        title = message ?: stringResource(
            when (failure) {
                TravelPlanFailure.SIGN_IN_REQUIRED -> Res.string.travel_sign_in_required
                TravelPlanFailure.UNAVAILABLE -> Res.string.travel_unavailable
                TravelPlanFailure.CONFLICT -> Res.string.travel_conflict_title
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
        description = if (isConflict) stringResource(Res.string.travel_conflict_message) else null,
        onRetry = onRetry,
        retryLabel = retryLabel,
        modifier = modifier,
    )
}

@Composable
internal fun TravelIssueCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = stringResource(Res.string.travel_retry),
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val colors = OguriTheme.colors
    Column(
        modifier = modifier.fillMaxWidth()
            .background(OguriTheme.colors.orangeContainer, shape)
            .border(1.dp, OguriTheme.colors.borderSubtle, shape)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (onRetry == null) 14.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = OguriTheme.typography.cardSubtitle, color = colors.textPrimary)
            if (description != null) {
                Text(description, style = OguriTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (onRetry != null) {
            androidx.compose.foundation.layout.FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                secondaryAction?.invoke()
                TravelTextButton(onClick = onRetry) { Text(retryLabel, color = colors.textPrimary) }
            }
        }
    }
}

@Composable
internal fun TravelBottomAction(
    enabled: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        if (showDivider) HorizontalDivider(color = OguriTheme.colors.borderSubtle)
        TravelPrimaryButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = bottomPadding),
            textStyle = travelTextStyle(18, 22, FontWeight.SemiBold),
            content = content,
        )
    }
}

@Composable
internal fun travelTextStyle(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal): TextStyle =
    with(OguriTheme.typography) {
        when {
            size >= 28 -> heroTitle
            size >= 22 -> sectionTitle
            size >= 18 -> cardTitle
            size >= 15 && weight >= FontWeight.Medium -> cardSubtitle
            size == 15 -> bodyLarge.copy(fontSize = cardSubtitle.fontSize)
            size >= 15 -> bodyLarge
            size == 14 && weight >= FontWeight.SemiBold -> labelLarge
            size == 14 -> bodyMedium
            size == 13 -> bodySmall
            size == 12 && weight >= FontWeight.Medium -> labelMedium
            size == 12 -> caption
            else -> labelSmall
        }
    }
