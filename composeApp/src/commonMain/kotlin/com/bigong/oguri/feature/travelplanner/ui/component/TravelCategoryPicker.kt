package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint40
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
        TravelVisitCategory.FLIGHT -> Res.string.travel_category_flight
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(Res.string.travel_category_optional),
            style = OguriTheme.typography.bodySmall,
            color = OguriTheme.colors.textTertiary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TravelVisitCategory.entries.forEach { option ->
                TravelChoiceChip(
                    label = stringResource(option.label()),
                    selected = category == option,
                    enabled = enabled,
                    onClick = { onSelect(option.takeUnless { it == category }) },
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
) {
    val colors = OguriTheme.colors
    Box(
        modifier.heightIn(min = 44.dp)
            .selectable(selected, interactionSource = null, indication = null, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 6.dp)
            .background(if (selected) Mint40 else colors.surfaceCard, CircleShape)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.borderSubtle, CircleShape))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = if (selected) OguriTheme.typography.labelLarge else OguriTheme.typography.bodyMedium,
            color = when {
                !enabled -> colors.textSecondary
                !selected -> colors.textTertiary
                else -> colors.textPrimary
            },
        )
    }
}
