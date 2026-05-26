package com.bigong.oguri.feature.search.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import oguri.composeapp.generated.resources.btn_search
import org.jetbrains.compose.resources.painterResource

@Composable
fun SearchTopBar(
    searchHintText: String,
    backContentDescriptionText: String,
    searchContentDescriptionText: String,
    searchQueryText: String,
    searchTextFieldFocusRequester: FocusRequester,
    onBackClick: () -> Unit,
    onSearchQueryTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 12.dp),
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.btn_back),
            contentDescription = backContentDescriptionText,
            colorFilter = ColorFilter.tint(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 10.dp)
                    .noRippleClickable(onClick = onBackClick),
        )
        BasicTextField(
            value = searchQueryText,
            onValueChange = onSearchQueryTextChange,
            textStyle = OguriTheme.typography.bodyLarge.copy(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 36.dp, end = 52.dp)
                    .focusRequester(searchTextFieldFocusRequester),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (searchQueryText.isBlank()) {
                    Text(
                        text = searchHintText,
                        style = OguriTheme.typography.cardSubtitle,
                        color = Neutral40,
                    )
                }
                innerTextField()
            },
        )
        Image(
            painter = painterResource(resource = Res.drawable.btn_search),
            contentDescription = searchContentDescriptionText,
            colorFilter = ColorFilter.tint(color = Neutral100),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(size = 48.dp)
                    .noRippleClickable(onClick = onSearchClick)
                    .padding(all = 8.dp),
        )
    }
}
