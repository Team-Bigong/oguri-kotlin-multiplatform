package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.period_detail_dummy_back
import oguri.composeapp.generated.resources.period_detail_dummy_description
import oguri.composeapp.generated.resources.period_detail_dummy_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodDetailScreen(
    periodId: Long,
    onBackClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.period_detail_dummy_back),
            style = OguriTheme.typography.cardSubtitle,
            color = Mint70,
            modifier = Modifier.noRippleClickable(onClick = onBackClick),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(Res.string.period_detail_dummy_title),
            style = OguriTheme.typography.sectionTitle,
            color = Neutral100,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(Res.string.period_detail_dummy_description, periodId),
            style = OguriTheme.typography.bodyLarge,
            color = Neutral50,
            textAlign = TextAlign.Center,
        )
    }
}
