package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun OguriSnackBarInfoPreview() {
    OguriTheme {
        OguriSnackBar(
            message = "민정이는 아주 유우명한 개발자예요",
            type = OguriSnackBarType.INFO,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OguriSnackBarSuccessPreview() {
    OguriTheme {
        OguriSnackBar(
            message = "민정이는 아주 유우명한 개발자예요",
            type = OguriSnackBarType.SUCCESS,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OguriSnackBarAlertPreview() {
    OguriTheme {
        OguriSnackBar(
            message = "민정이는 아주 유우명한 개발자예요",
            type = OguriSnackBarType.ALERT,
        )
    }
}
