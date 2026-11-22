package com.bigong.oguri.feature.travelplanner.ui.component

import com.bigong.oguri.core.designsystem.OguriTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_directions_compact
import oguri.composeapp.generated.resources.travel_directions
import oguri.composeapp.generated.resources.travel_directions_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelDirectionsButton(
    placeId: String?,
    destination: String?,
    enabled: Boolean = true,
    compact: Boolean = false,
    outlined: Boolean = false,
    modifier: Modifier = Modifier,
    onOpened: () -> Unit,
    buttonContainer: @Composable (@Composable () -> Unit) -> Unit = { it() },
) {
    val target = destination?.takeIf { it.isNotBlank() }
    if (target == null && placeId == null) return
    val uriHandler = LocalUriHandler.current
    var failed by remember(placeId, target) { mutableStateOf(false) }
    val openDirections = {
        val url =
            URLBuilder()
                .apply {
                    takeFrom("https://www.google.com/maps/dir/")
                    parameters.append("api", "1")
                    parameters.append("destination", target ?: "place_id:$placeId")
                    if (placeId != null) parameters.append("destination_place_id", placeId)
                }.buildString()
        failed = runCatching { uriHandler.openUri(url) }.isFailure
        if (!failed) onOpened()
    }
    val content: @Composable () -> Unit = { Text(stringResource(if (compact) Res.string.travel_directions_compact else Res.string.travel_directions), style = if (outlined) OguriTheme.typography.cardTitle else if (compact) OguriTheme.typography.labelLarge else OguriTheme.typography.labelMedium) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttonContainer {
            if (outlined) {
                TravelOutlineButton(onClick = openDirections, enabled = enabled, modifier = modifier, content = content)
            } else {
                TravelTextButton(onClick = openDirections, enabled = enabled, modifier = modifier, content = content)
            }
        }
        if (failed) {
            Text(
                stringResource(Res.string.travel_directions_unavailable),
                style = OguriTheme.typography.caption,
                color = OguriTheme.colors.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}
