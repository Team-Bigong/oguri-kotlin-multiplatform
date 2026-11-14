package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelVisitCategory
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun TravelVisitCategory.label(): StringResource =
    when (this) {
        TravelVisitCategory.RESTAURANT -> Res.string.travel_category_restaurant
        TravelVisitCategory.CAFE -> Res.string.travel_category_cafe
        TravelVisitCategory.SIGHTSEEING -> Res.string.travel_category_sightseeing
        TravelVisitCategory.LODGING -> Res.string.travel_category_lodging
        TravelVisitCategory.FLIGHT -> Res.string.travel_category_transport
        TravelVisitCategory.TRANSPORT -> Res.string.travel_category_transport
        TravelVisitCategory.SHOPPING -> Res.string.travel_category_shopping
        TravelVisitCategory.OTHER -> Res.string.travel_category_other
    }

@Composable
internal fun TravelCategoryPicker(
    category: TravelVisitCategory?,
    enabled: Boolean,
    onSelect: (TravelVisitCategory?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(Res.string.travel_category_optional),
            style = travelTextStyle(12, 14, FontWeight.Medium),
            color = OguriTheme.colors.textTertiary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TravelVisitCategory.entries.filter { it != TravelVisitCategory.FLIGHT }.forEach { option ->
                TravelChoiceChip(
                    label = stringResource(option.label()),
                    selected = category == option,
                    enabled = enabled,
                    onClick = { onSelect(option.takeUnless { it == category }) },
                    categoryChip = true,
                )
            }
        }
    }
}

@Composable
internal fun TravelChoiceChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryChip: Boolean = false,
    segmented: Boolean = false,
) {
    val colors = OguriTheme.colors
    val textSize = if (categoryChip) { if (selected) 14 else 13 } else 12
    val lineHeight = if (categoryChip) 16 else 14
    val chipHeight = if (categoryChip) 32.dp else if (segmented) 28.dp else 30.dp
    val touchPadding = if (categoryChip) 4.dp else if (segmented) 0.dp else 7.dp
    Box(
        modifier.selectable(selected, interactionSource = null, indication = null, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = touchPadding)
            .heightIn(min = chipHeight)
            .widthIn(min = if (categoryChip) 52.dp else Dp.Unspecified)
            .background(if (selected) Mint40 else if (segmented) Color.Transparent else colors.surfaceCard, CircleShape)
            .then(if (selected || segmented) Modifier else Modifier.border(1.dp, colors.borderSubtle, CircleShape))
            .padding(horizontal = 13.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = travelTextStyle(textSize, lineHeight, if (categoryChip && selected) FontWeight.SemiBold else if (categoryChip) FontWeight.Normal else FontWeight.Medium),
            color = when {
                !enabled -> colors.textSecondary
                !selected -> colors.textTertiary
                categoryChip || segmented -> Neutral100
                else -> colors.textPrimary
            },
        )
    }
}
