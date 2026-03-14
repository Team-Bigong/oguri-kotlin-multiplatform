package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun AnchoredPopupMenu(
    expanded: Boolean,
    anchorHeightPx: Int,
    onDismissRequest: () -> Unit,
    alignment: Alignment,
    verticalOffset: Dp = 4.dp,
    shape: Shape,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) {
        return
    }
    val density = LocalDensity.current
    val verticalOffsetPx = with(density) { verticalOffset.roundToPx() }

    Popup(
        alignment = alignment,
        offset = IntOffset(x = 0, y = anchorHeightPx + verticalOffsetPx),
        onDismissRequest = onDismissRequest,
        properties =
            PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Surface(
            shape = shape,
            color = containerColor,
        ) {
            Column(content = content)
        }
    }
}
