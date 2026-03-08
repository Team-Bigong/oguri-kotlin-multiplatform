package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.util.HapticType
import com.bigong.oguri.core.util.extension.perform
import com.bigong.oguri.core.util.extension.noRippleClickable
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
                    .size(48.dp)
                    .noRippleClickable(
                        onClick = {
                            HapticType.Selection.perform()
                            onCheckedChange(!checked)
                        },
                    )
                    .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (checked) Res.drawable.ic_save_checked else Res.drawable.ic_save_unchecked),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
