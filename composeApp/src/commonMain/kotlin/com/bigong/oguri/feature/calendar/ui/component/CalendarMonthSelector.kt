package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_expand_menu
import oguri.composeapp.generated.resources.calendar_month_year
import oguri.composeapp.generated.resources.calendar_month_year_selector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

private val MONTH_MENU_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun CalendarMonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onYearMonthSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDropdownVisible by remember { mutableStateOf(false) }
    val yearMonthOptions = remember { yearMonthOptions() }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = { isDropdownVisible = true })
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.calendar_month_year, selectedYear, selectedMonth),
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
        )
        Image(
            painter = painterResource(resource = Res.drawable.btn_expand_menu),
            contentDescription = stringResource(Res.string.calendar_month_year_selector),
            modifier = Modifier.padding(start = 8.dp),
        )

        DropdownMenu(
            expanded = isDropdownVisible,
            onDismissRequest = { isDropdownVisible = false },
            shape = MONTH_MENU_SHAPE,
            containerColor = Neutral0,
            modifier = Modifier.background(color = Neutral0, shape = MONTH_MENU_SHAPE),
        ) {
            Box(
                modifier =
                    Modifier
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Column {
                    yearMonthOptions.forEachIndexed { index: Int, option: YearMonthOption ->
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable(
                                        onClick = {
                                            onYearMonthSelected(option.year, option.month)
                                            isDropdownVisible = false
                                        },
                                    ).padding(horizontal = 16.dp, vertical = 12.dp),
                            text = stringResource(Res.string.calendar_month_year, option.year, option.month),
                            style = OguriTheme.typography.bodyMedium,
                            color = Neutral100,
                        )
                        if (index < yearMonthOptions.lastIndex) {
                            HorizontalDivider(color = Neutral20)
                        }
                    }
                }
            }
        }
    }
}

private data class YearMonthOption(
    val year: Int,
    val month: Int,
)

private fun yearMonthOptions(): List<YearMonthOption> {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    val startDate = today.minus(value = today.day - 1, unit = DateTimeUnit.DAY)
    val endDate = startDate.plus(value = 2, unit = DateTimeUnit.YEAR)
    val options = mutableListOf<YearMonthOption>()
    var cursorDate = startDate

    while (cursorDate <= endDate) {
        options += YearMonthOption(year = cursorDate.year, month = cursorDate.month.ordinal + 1)
        cursorDate = cursorDate.plus(value = 1, unit = DateTimeUnit.MONTH)
    }

    return options
}
