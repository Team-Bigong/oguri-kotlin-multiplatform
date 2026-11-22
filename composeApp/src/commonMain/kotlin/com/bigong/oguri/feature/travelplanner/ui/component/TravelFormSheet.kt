package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.ConfirmAlertDialog
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelFormSheet(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit = {},
    confirmButton: @Composable RowScope.() -> Unit = {},
    dismissButton: @Composable RowScope.() -> Unit = {},
    shape: Shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    containerColor: Color = OguriTheme.colors.backgroundSecondary,
) {
    ModalBottomSheet(scrimColor = TravelSheetScrim, onDismissRequest = onDismissRequest, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), shape = shape, containerColor = containerColor, contentColor = Neutral100, dragHandle = { TravelSheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProvideTextStyle(OguriTheme.typography.cardTitle, title)
            ProvideTextStyle(OguriTheme.typography.bodyMedium, text)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { dismissButton(); confirmButton() }
        }
    }
}

@Composable
internal fun TravelDiscardConfirmation(onDiscard: () -> Unit, onKeep: () -> Unit) {
    ConfirmAlertDialog(
        iconResource = Res.drawable.ic_alert,
        titleText = stringResource(Res.string.travel_discard_title),
        messageText = stringResource(Res.string.travel_discard_message),
        confirmButtonText = stringResource(Res.string.travel_keep_writing),
        cancelButtonText = stringResource(Res.string.travel_search_close),
        onDismissRequest = onKeep, onConfirmClick = onKeep, onCancelClick = onDiscard,
        confirmButtonFirst = true,
    )
}

@Composable
internal fun TravelSheetHandle() {
    Box(Modifier.padding(top = 8.dp, bottom = 8.dp).size(width = 36.dp, height = 4.dp)
        .background(OguriTheme.colors.borderStrong, CircleShape))
}
