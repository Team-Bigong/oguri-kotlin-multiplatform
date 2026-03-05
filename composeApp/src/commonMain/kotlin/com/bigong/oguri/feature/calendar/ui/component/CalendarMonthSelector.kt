package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_expand_menu
import oguri.composeapp.generated.resources.calendar_month_year
import oguri.composeapp.generated.resources.calendar_month_year_selector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarMonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onYearMonthSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDropdownVisible by remember { mutableStateOf(false) }
    val yearMonthOptions = remember(selectedYear) { yearMonthOptions(baseYear = selectedYear) }

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
        ) {
            yearMonthOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.calendar_month_year, option.year, option.month),
                            style = OguriTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onYearMonthSelected(option.year, option.month)
                        isDropdownVisible = false
                    },
                )
            }
        }
    }
}

private data class YearMonthOption(
    val year: Int,
    val month: Int,
)

private fun yearMonthOptions(baseYear: Int): List<YearMonthOption> {
    val startYear = baseYear - 1
    val endYear = baseYear + 1

    return buildList {
        for (year in startYear..endYear) {
            for (month in 1..12) {
                add(YearMonthOption(year = year, month = month))
            }
        }
    }
}
