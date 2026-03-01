package com.bigong.oguri.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_preparing_message
import oguri.composeapp.generated.resources.bottom_navigation_calendar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        androidx.compose.material3.Text(
            text = stringResource(Res.string.bottom_navigation_calendar),
            style = OguriTheme.typography.sectionTitle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        androidx.compose.material3.Text(
            text = stringResource(Res.string.calendar_preparing_message),
            style = OguriTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
