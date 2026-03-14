package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun CalendarMonthSelectorPreview() {
    OguriTheme {
        CalendarMonthSelector(
            selectedYear = 2026,
            selectedMonth = 3,
            onYearMonthSelected = { _, _ -> },
        )
    }
}
