package com.bigong.oguri.feature.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

private val sectionCornerRadius = 18.dp
private val actionButtonCornerRadius = 12.dp
private const val PlaceholderSectionAlpha: Float = 0.55f
private const val PlaceholderSurfaceAlpha: Float = 0.35f

val PlaceholderSpacingSmall = 8.dp
val PlaceholderSpacingMedium = 12.dp
val PlaceholderSpacingLarge = 20.dp

@Composable
fun PlaceholderHeader(
    screenTitleText: String,
    screenSubtitleText: String,
) {
    Text(
        text = screenTitleText,
        style = OguriTheme.typography.sectionTitle,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
    Text(
        text = screenSubtitleText,
        style = OguriTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun PlaceholderSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PlaceholderSurfaceAlpha),
                shape = RoundedCornerShape(sectionCornerRadius),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        content = content,
    )
}

@Composable
fun PlaceholderSectionTitle(
    text: String,
) {
    Text(
        text = text,
        style = OguriTheme.typography.cardTitle,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun PlaceholderInfoCard(
    lines: List<String>,
) {
    PlaceholderSectionCard {
        lines.forEach { lineText: String ->
            Text(
                text = lineText,
                style = OguriTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun PlaceholderActionButton(
    labelText: String,
    onClick: () -> Unit,
    emphasized: Boolean = true,
    enabled: Boolean = true,
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) PlaceholderSectionAlpha else 0.25f)
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(actionButtonCornerRadius),
            )
            .noRippleClickable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = labelText,
            style = OguriTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PlaceholderSelectableChip(
    labelText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = labelText,
            style = OguriTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PlaceholderStepper(
    valueText: String,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PlaceholderActionButton(labelText = "-", onClick = onMinusClick, emphasized = false)
        }
        Box(
            modifier = Modifier
                .weight(2f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = valueText,
                style = OguriTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            PlaceholderActionButton(labelText = "+", onClick = onPlusClick, emphasized = false)
        }
    }
}

@Composable
fun PlaceholderRowItem(
    titleText: String,
    subtitleText: String? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) {
        Modifier.noRippleClickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleText,
                style = OguriTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitleText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    style = OguriTheme.typography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = OguriTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun PlaceholderBannerAd(
    labelText: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = labelText,
                style = OguriTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
