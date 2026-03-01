package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme

@Composable
fun HomeMoreRecommendationButton(
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFE8F0EF), shape = RoundedCornerShape(size = 12.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = OguriTheme.typography.cardTitle,
            color = Color(0xFF43B9A8),
        )
    }
}
