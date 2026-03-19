package com.bigong.oguri.feature.perioddetail.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.AnchoredPopupMenu
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.btn_menu
import oguri.composeapp.generated.resources.place_detail_menu_save
import oguri.composeapp.generated.resources.place_detail_menu_share
import oguri.composeapp.generated.resources.place_detail_menu_unsave
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodDetailTopBar(
    titleText: String,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var menuAnchorHeightPx by remember { mutableIntStateOf(0) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Neutral0),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.btn_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral100),
                modifier = Modifier.noRippleClickable(onClick = onBackClick),
            )
            Text(
                text = titleText,
                style = OguriTheme.typography.cardTitle,
                color = Neutral100,
            )
            androidx.compose.foundation.layout.Box(
                modifier =
                    Modifier
                        .padding(end = 2.dp)
                        .noRippleClickable(onClick = { isMenuExpanded = true })
                        .onGloballyPositioned { coordinates ->
                            menuAnchorHeightPx = coordinates.size.height
                        },
            ) {
                Image(
                    painter = painterResource(Res.drawable.btn_menu),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Neutral100),
                )
                AnchoredPopupMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    anchorHeightPx = menuAnchorHeightPx,
                    alignment = Alignment.TopEnd,
                    shape = RoundedCornerShape(8.dp),
                    containerColor = Neutral0,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.place_detail_menu_share),
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral100,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable(onClick = {
                                        isMenuExpanded = false
                                        onShareClick()
                                    })
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        HorizontalDivider(color = Neutral20)
                        Text(
                            text =
                                if (isSaved) {
                                    stringResource(Res.string.place_detail_menu_unsave)
                                } else {
                                    stringResource(Res.string.place_detail_menu_save)
                                },
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral100,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable(onClick = {
                                        isMenuExpanded = false
                                        onSaveToggleClick()
                                    })
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Neutral20)
    }
}
