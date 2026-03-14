package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme

@Composable
fun HomeGreetingSection(
    nameText: String,
    questionText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = nameText,
            style = OguriTheme.typography.bodyMedium,
            color = Neutral100,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = questionText,
            style = OguriTheme.typography.sectionTitle,
            color = Neutral100,
        )
    }
}
