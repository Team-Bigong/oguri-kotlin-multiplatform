package com.bigong.oguri.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.feature.calendar.ui.component.CalendarLeaveDaysEditor
import com.bigong.oguri.feature.calendar.ui.component.CalendarMonthGrid
import com.bigong.oguri.feature.calendar.ui.component.CalendarMonthSelector
import com.bigong.oguri.feature.calendar.ui.component.CalendarRecommendationSection
import com.bigong.oguri.feature.calendar.ui.component.CalendarSkeletonContent
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_header_subtitle
import oguri.composeapp.generated.resources.calendar_header_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun CalendarScreen(
    calendarUiState: CalendarUiState,
    onLeaveDaysChanged: (Int) -> Unit,
    onYearMonthSelected: (Int, Int) -> Unit,
    onDateClick: (kotlinx.datetime.LocalDate) -> Unit,
    onPeriodClick: (CalendarPeriod) -> Unit,
    onRetryClick: () -> Unit,
) {
    if (calendarUiState.isLoading) {
        CalendarSkeletonContent()
        return
    }

    if (calendarUiState.isError || calendarUiState.calendarRecommendation == null) {
        NetworkErrorRetryContent(onRetryClick = onRetryClick)
        return
    }

    val recommendation = calendarUiState.calendarRecommendation
    val selectedPeriod =
        recommendation.periods.firstOrNull { period ->
            period.id == calendarUiState.selectedPeriodId
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.calendar_header_title),
            style = OguriTheme.typography.sectionTitle,
            color = Neutral90,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.calendar_header_subtitle),
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral50,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        CalendarLeaveDaysEditor(
            leaveDays = calendarUiState.leaveDays,
            onLeaveDaysChanged = onLeaveDaysChanged,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Neutral20)

        CalendarMonthSelector(
            selectedYear = calendarUiState.selectedYear,
            selectedMonth = calendarUiState.selectedMonth,
            onYearMonthSelected = onYearMonthSelected,
        )

        CalendarMonthGrid(
            year = recommendation.year,
            month = recommendation.month,
            holidays = recommendation.holidays,
            periods = recommendation.periods,
            selectedPeriod = selectedPeriod,
            selectedDate = calendarUiState.selectedDate,
            todayDate =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
            onDateClick = onDateClick,
        )

        Spacer(modifier = Modifier.height(4.dp))

        CalendarRecommendationSection(
            periods = recommendation.periods,
            selectedPeriodId = calendarUiState.selectedPeriodId,
            onPeriodClick = onPeriodClick,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
