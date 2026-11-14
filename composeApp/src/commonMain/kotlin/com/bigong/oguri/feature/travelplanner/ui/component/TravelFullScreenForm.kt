package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelFullScreenForm(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable () -> Unit = {},
    shape: Shape = RoundedCornerShape(0.dp),
    containerColor: Color = OguriTheme.colors.backgroundSecondary,
) {
    Dialog(onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        TravelPlannerPage(title = "", onBack = null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TravelIconButton(onClick = onDismissRequest) { Icon(painterResource(Res.drawable.ic_travel_back), stringResource(Res.string.travel_back)) }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { ProvideTextStyle(OguriTheme.typography.cardTitle, title) }
                Spacer(Modifier.size(48.dp))
            }
            Box(Modifier.weight(1f).fillMaxWidth().padding(20.dp)) { ProvideTextStyle(OguriTheme.typography.bodyMedium, text) }
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { dismissButton(); confirmButton() }
        }
    }
}
