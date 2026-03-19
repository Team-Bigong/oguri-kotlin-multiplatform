package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun MyPageSelectedPeriodCardPreview() {
    OguriTheme {
        MyPageSelectedPeriodCard(
            period =
                MyPageSelectedPeriod(
                    id = 1L,
                    startDate = LocalDate.parse("2026-02-28"),
                    endDate = LocalDate.parse("2026-03-04"),
                    totalTripCount = 3,
                    dayOffCount = 2,
                ),
            dDayText = "D-12",
            dDayColor = Mint70,
            dateRangeText = "2월 28일 - 3월 4일",
            onDeleteClick = {},
            onClick = {},
        )
    }
}
