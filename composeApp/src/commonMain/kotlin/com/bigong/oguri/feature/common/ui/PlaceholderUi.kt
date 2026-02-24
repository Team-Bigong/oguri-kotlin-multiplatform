package com.bigong.oguri.feature.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val screenHorizontalPadding = 20.dp
private val sectionCornerRadius = 18.dp
private val actionButtonCornerRadius = 12.dp
private val verticalSpacingSmall = 8.dp
private val verticalSpacingLarge = 20.dp
private const val PlaceholderSectionAlpha: Float = 0.55f
private const val PlaceholderSurfaceAlpha: Float = 0.35f

val PlaceholderSpacingSmall = 8.dp
val PlaceholderSpacingMedium = 12.dp

@Composable
fun PlaceholderScreenFrame(
    screenTitleText: String,
    screenSubtitleText: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = screenHorizontalPadding, vertical = verticalSpacingLarge),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = screenTitleText,
            style = OguriTheme.typography.sectionTitle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(verticalSpacingSmall))
        Text(
            text = screenSubtitleText,
            style = OguriTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(verticalSpacingLarge))
        content()
    }
}

@Composable
fun PlaceholderInfoCard(
    lines: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PlaceholderSurfaceAlpha),
                shape = RoundedCornerShape(sectionCornerRadius),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingSmall),
    ) {
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
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PlaceholderSectionAlpha)
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
            .noRippleClickable(onClick = onClick)
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
