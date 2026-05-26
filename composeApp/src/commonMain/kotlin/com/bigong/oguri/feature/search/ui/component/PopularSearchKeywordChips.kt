package com.bigong.oguri.feature.search.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PopularSearchKeywordChips(
    popularSearchKeywords: List<String>,
    onPopularSearchKeywordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        popularSearchKeywords.forEach { keyword ->
            Text(
                text = keyword,
                style = OguriTheme.typography.bodyMedium,
                color = Neutral70,
                modifier =
                    Modifier
                        .border(width = 1.dp, color = Neutral50, shape = searchKeywordChipShape)
                        .noRippleClickable(onClick = { onPopularSearchKeywordClick(keyword) })
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
