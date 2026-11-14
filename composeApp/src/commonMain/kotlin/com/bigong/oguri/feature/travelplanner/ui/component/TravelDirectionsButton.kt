package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import oguri.composeapp.generated.resources.travel_directions
import oguri.composeapp.generated.resources.travel_directions_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelDirectionsButton(
    placeId: String?,
    destination: String?,
    enabled: Boolean = true,
) {
    val target = destination?.takeIf { it.isNotBlank() }
    if (target == null && placeId == null) return
    val uriHandler = LocalUriHandler.current
    var failed by remember(placeId, target) { mutableStateOf(false) }
    TextButton(enabled = enabled, onClick = {
        val url =
            URLBuilder()
                .apply {
                    takeFrom("https://www.google.com/maps/dir/")
                    parameters.append("api", "1")
                    parameters.append("destination", target ?: "place_id:$placeId")
                    if (placeId != null) parameters.append("destination_place_id", placeId)
                }.buildString()
        failed = runCatching { uriHandler.openUri(url) }.isFailure
    }) { Text(stringResource(Res.string.travel_directions)) }
    if (failed) {
        Text(
            stringResource(Res.string.travel_directions_unavailable),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}
