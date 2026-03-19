package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.getStyledText

@Composable
fun OnboardingHighlightedTitle(
    fullText: String,
    highlightedText: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text =
            fullText.getStyledText(
                style = OguriTheme.typography.sectionTitle.copy(color = Mint70, fontWeight = FontWeight.Bold),
                highlightedText,
            ),
        style = OguriTheme.typography.cardTitle,
        color = Neutral90,
        modifier = modifier,
    )
}
