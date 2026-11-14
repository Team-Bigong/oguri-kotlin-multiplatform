package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.feature.travelplanner.ui.model.TravelModifiedUnit
import com.bigong.oguri.feature.travelplanner.ui.model.travelModifiedAge
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun TravelModifiedButton(
    plan: TravelPlan,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val modified = plan.lastModifiedAt ?: return
    val age = remember(plan.summary.id, modified) { travelModifiedAge(modified, Clock.System.now()) }
    val label =
        when (age.unit) {
            TravelModifiedUnit.JUST_NOW -> stringResource(Res.string.travel_modified_just_now)
            TravelModifiedUnit.MINUTES -> stringResource(Res.string.travel_modified_minutes, age.count)
            TravelModifiedUnit.HOURS -> stringResource(Res.string.travel_modified_hours, age.count)
            TravelModifiedUnit.DAYS -> stringResource(Res.string.travel_modified_days, age.count)
        }
    TravelTextButton(enabled = enabled, onClick = onClick) { Text(label, style = MaterialTheme.typography.bodySmall) }
}
