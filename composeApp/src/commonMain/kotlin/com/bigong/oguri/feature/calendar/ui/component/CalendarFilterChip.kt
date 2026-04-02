package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_pen
import org.jetbrains.compose.resources.painterResource

@Composable
fun CalendarFilterChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(color = Mint5, shape = RoundedCornerShape(100.dp))
                .border(width = 1.dp, color = Mint70, shape = RoundedCornerShape(100.dp))
                .noRippleClickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = OguriTheme.typography.labelMedium,
            color = Mint70,
        )
        Image(
            painter = painterResource(Res.drawable.ic_pen),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Mint70),
            modifier = Modifier.size(12.dp),
        )
    }
}
