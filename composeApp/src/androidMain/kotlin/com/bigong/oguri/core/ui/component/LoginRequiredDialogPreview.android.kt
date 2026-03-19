package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LoginRequiredDialogPreview() {
    OguriTheme {
        LoginRequiredDialog(
            onDismissRequest = {},
            onLoginClick = {},
        )
    }
}
