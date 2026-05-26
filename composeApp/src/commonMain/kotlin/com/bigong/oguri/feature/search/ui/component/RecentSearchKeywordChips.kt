package com.bigong.oguri.feature.search.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_trashcan
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchKeywordChips(
    recentSearchKeywords: List<String>,
    onRecentSearchKeywordClick: (String) -> Unit,
    onRecentSearchDeleteClick: (String) -> Unit,
    deleteRecentSearchContentDescriptionText: String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        recentSearchKeywords.forEach { keyword ->
            Row(
                modifier =
                    Modifier
                        .border(width = 1.dp, color = Neutral50, shape = searchKeywordChipShape)
                        .noRippleClickable(onClick = { onRecentSearchKeywordClick(keyword) })
                        .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
            ) {
                Text(
                    text = keyword,
                    style = OguriTheme.typography.bodyMedium,
                    color = Neutral70,
                )
                Image(
                    painter = painterResource(resource = Res.drawable.ic_trashcan),
                    contentDescription = deleteRecentSearchContentDescriptionText,
                    colorFilter = ColorFilter.tint(color = Neutral50),
                    modifier =
                        Modifier
                            .size(size = 14.dp)
                            .noRippleClickable(onClick = { onRecentSearchDeleteClick(keyword) }),
                )
            }
        }
    }
}
