package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.domain.model.Place
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.mypage_empty_saved_place
import oguri.composeapp.generated.resources.mypage_section_saved_place
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageSavedPlaceSection(
    savedPlaces: List<Place>,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        GuideHeader(
            iconResource = Res.drawable.ic_plane,
            titleText = stringResource(Res.string.mypage_section_saved_place),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (savedPlaces.isEmpty()) {
            Text(
                text = stringResource(Res.string.mypage_empty_saved_place),
                style = OguriTheme.typography.bodyMedium,
                color = Neutral40,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            return@Column
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items = savedPlaces, key = { place: Place -> place.id }) { place: Place ->
                MyPageSavedPlaceCard(
                    place = place,
                    onDeleteClick = onDeleteClick,
                )
            }
        }
    }
}
