package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.OguriTheme
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
fun PlaceDetailTopBar(
    city: String,
    isSaved: Boolean,
    isCollapsed: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val tintColor = if (isCollapsed) Neutral100 else Neutral0

    if (!isCollapsed) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(color = Neutral100.copy(alpha = 0.3f), shape = CircleShape),
            ) {
                Image(
                    painter = painterResource(Res.drawable.btn_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(tintColor),
                    modifier = Modifier.noRippleClickable(onClick = onBackClick),
                )
            }
        }
    } else {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(Neutral0)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.btn_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(tintColor),
                modifier = Modifier.noRippleClickable(onClick = onBackClick),
            )
            Text(
                text = city,
                style = OguriTheme.typography.cardTitle,
                color = tintColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.btn_menu),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(tintColor),
                    modifier = Modifier.noRippleClickable(onClick = { isMenuExpanded = true }),
                )
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    containerColor = Neutral0,
                    tonalElevation = 0.dp,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = Neutral30),
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.place_detail_menu_share),
                                style = OguriTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            onShareClick()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    if (isSaved) {
                                        stringResource(Res.string.place_detail_menu_unsave)
                                    } else {
                                        stringResource(Res.string.place_detail_menu_save)
                                    },
                                style = OguriTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            onSaveToggleClick()
                        },
                    )
                }
            }
        }
    }
}
