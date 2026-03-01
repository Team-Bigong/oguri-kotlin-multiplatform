package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.previewPlace

@Preview(showBackground = true)
@Composable
private fun HomePlaceCardPreview() {
    OguriTheme {
        HomePlaceCard(
            place = previewPlace,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
