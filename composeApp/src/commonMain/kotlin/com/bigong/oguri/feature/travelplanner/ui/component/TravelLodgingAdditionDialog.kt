package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.runtime.Composable
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.ConfirmAlertDialog
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_travel_pin
import oguri.composeapp.generated.resources.travel_lodging_add_other_days
import oguri.composeapp.generated.resources.travel_lodging_add_this_day_only
import oguri.composeapp.generated.resources.travel_lodging_extra_days_message
import oguri.composeapp.generated.resources.travel_lodging_extra_days_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelLodgingAdditionDialog(
    onDismiss: () -> Unit,
    onAddOnlyThisDay: () -> Unit,
    onAddOtherDays: () -> Unit,
) {
    ConfirmAlertDialog(
        iconResource = Res.drawable.ic_travel_pin,
        titleText = stringResource(Res.string.travel_lodging_extra_days_title),
        messageText = stringResource(Res.string.travel_lodging_extra_days_message),
        confirmButtonText = stringResource(Res.string.travel_lodging_add_other_days),
        cancelButtonText = stringResource(Res.string.travel_lodging_add_this_day_only),
        onDismissRequest = onDismiss,
        onConfirmClick = onAddOtherDays,
        onCancelClick = onAddOnlyThisDay,
        iconTint = Mint70,
        messageColor = OguriTheme.colors.textSecondary,
    )
}
