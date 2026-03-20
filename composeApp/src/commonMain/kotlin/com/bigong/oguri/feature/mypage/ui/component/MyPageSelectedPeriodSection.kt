package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_d_day_after
import oguri.composeapp.generated.resources.calendar_d_day_before
import oguri.composeapp.generated.resources.calendar_month_day
import oguri.composeapp.generated.resources.ic_diary
import oguri.composeapp.generated.resources.mypage_empty_selected_period
import oguri.composeapp.generated.resources.mypage_section_selected_period
import oguri.composeapp.generated.resources.mypage_selected_period_date_range
import oguri.composeapp.generated.resources.mypage_selected_period_date_with_year
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun MyPageSelectedPeriodSection(
    selectedPeriods: List<MyPageSelectedPeriod>,
    onDeleteClick: (Long) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GuideHeader(
            iconResource = Res.drawable.ic_diary,
            titleText = stringResource(Res.string.mypage_section_selected_period),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (selectedPeriods.isEmpty()) {
            Text(
                text = stringResource(Res.string.mypage_empty_selected_period),
                style = OguriTheme.typography.bodyMedium,
                color = Neutral40,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            return@Column
        }

        val currentYear =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .year
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(
                items = selectedPeriods,
                key = { _, period -> period.id },
            ) { _, period ->
                val dDay = (period.startDate.toEpochDays() - today.toEpochDays()).toInt()
                val dDayText =
                    if (dDay >= 0) {
                        stringResource(Res.string.calendar_d_day_before, dDay)
                    } else {
                        stringResource(Res.string.calendar_d_day_after, -dDay)
                    }
                val dDayColor = if (dDay < 100) Mint70 else Orange50
                val dateRangeText =
                    stringResource(
                        Res.string.mypage_selected_period_date_range,
                        period.startDate.toMonthDayText(currentYear = currentYear),
                        period.endDate.toMonthDayText(currentYear = currentYear),
                    )

                MyPageSelectedPeriodCard(
                    period = period,
                    dDayText = dDayText,
                    dDayColor = dDayColor,
                    dateRangeText = dateRangeText,
                    onDeleteClick = onDeleteClick,
                    onClick = {
                        onPeriodClick(
                            period.startDate.toString(),
                            period.endDate.toString(),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun LocalDate.toMonthDayText(currentYear: Int): String =
    if (year == currentYear) {
        stringResource(
            Res.string.calendar_month_day,
            month.ordinal + 1,
            day,
        )
    } else {
        stringResource(
            Res.string.mypage_selected_period_date_with_year,
            year,
            month.ordinal + 1,
            day,
        )
    }
