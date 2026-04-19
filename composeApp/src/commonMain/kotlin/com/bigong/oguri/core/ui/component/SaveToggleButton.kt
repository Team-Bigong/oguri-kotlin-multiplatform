package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.util.extension.HapticType
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.perform
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_save_checked
import oguri.composeapp.generated.resources.ic_save_unchecked
import org.jetbrains.compose.resources.painterResource

@Composable
fun SaveToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(32.dp)
                .noRippleClickable(
                    onClick = {
                        HapticType.Selection.perform()
                        onCheckedChange(!checked)
                    },
                ).padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (checked) Res.drawable.ic_save_checked else Res.drawable.ic_save_unchecked),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Mint70),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
