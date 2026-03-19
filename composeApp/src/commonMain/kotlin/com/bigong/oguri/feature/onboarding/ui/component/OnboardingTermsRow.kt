package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_right_arrow
import oguri.composeapp.generated.resources.ic_terms_checked
import oguri.composeapp.generated.resources.ic_terms_unchecked
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingTermsRow(
    text: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onToggle)
                .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter =
                    painterResource(
                        if (isChecked) {
                            Res.drawable.ic_terms_checked
                        } else {
                            Res.drawable.ic_terms_unchecked
                        },
                    ),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = text,
                style = OguriTheme.typography.cardSubtitle,
                color = Neutral90,
            )
        }

        Image(
            painter = painterResource(Res.drawable.ic_right_arrow),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Neutral90),
            modifier =
                Modifier
                    .size(22.dp)
                    .noRippleClickable(onClick = onOpenDetail),
        )
    }
}
