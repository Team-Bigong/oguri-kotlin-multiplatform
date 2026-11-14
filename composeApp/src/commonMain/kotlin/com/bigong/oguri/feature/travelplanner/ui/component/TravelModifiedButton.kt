package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.feature.travelplanner.ui.model.TravelModifiedUnit
import com.bigong.oguri.feature.travelplanner.ui.model.travelModifiedAge
import kotlin.time.Clock
import oguri.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelModifiedButton(
    plan: TravelPlan,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
    TravelTextButton(enabled = enabled, onClick = onClick, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.textTertiary)
            Icon(painterResource(Res.drawable.ic_right_arrow), null, Modifier.size(14.dp), tint = OguriTheme.colors.textTertiary)
        }
    }
}
