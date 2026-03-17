package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun CalendarLeaveDaysBottomSheetPreview() {
    OguriTheme {
        CalendarLeaveDaysBottomSheet(
            leaveDays = 3,
            onLeaveDaysChanged = {},
        )
    }
}
