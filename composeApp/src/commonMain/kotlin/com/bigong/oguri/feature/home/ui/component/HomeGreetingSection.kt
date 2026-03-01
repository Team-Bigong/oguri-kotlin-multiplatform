package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme

@Composable
fun HomeGreetingSection(
    nameText: String,
    questionText: String,
) {
    Column {
        Text(
            text = nameText,
            style = OguriTheme.typography.cardSubtitle,
            color = Color(0xFF5D6670),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = questionText,
            style = OguriTheme.typography.sectionTitle,
            color = Color(0xFF14181F),
        )
    }
}
