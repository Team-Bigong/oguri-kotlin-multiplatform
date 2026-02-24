package com.bigong.oguri.feature.strategy.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.data.model.StrategyDetailData
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderSectionCard
import com.bigong.oguri.feature.common.ui.PlaceholderSectionTitle
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.strategy_detail_button_calendar
import oguri.composeapp.generated.resources.strategy_detail_button_mini_calendar
import oguri.composeapp.generated.resources.strategy_detail_button_pdf_pro
import oguri.composeapp.generated.resources.strategy_detail_section_destinations
import oguri.composeapp.generated.resources.strategy_detail_section_leave_usage
import oguri.composeapp.generated.resources.strategy_detail_section_total_secured
import oguri.composeapp.generated.resources.strategy_detail_total_days_badge
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StrategyDetailContentSections(
    strategyDetailData: StrategyDetailData,
    onCalendarClick: () -> Unit,
    onSavePdfProClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium)) {
        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = strategyDetailData.titleText)
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_leave_usage))
            strategyDetailData.annualLeaveUsageDateTexts.forEach { lineText: String ->
                androidx.compose.material3.Text(
                    text = lineText,
                    style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyMedium,
                )
            }
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_total_secured))
            androidx.compose.material3.Text(
                text = stringResource(Res.string.strategy_detail_total_days_badge, strategyDetailData.totalVacationDaysSecured),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.heroTitle,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PlaceholderSpacingMedium)) {
                Box(modifier = Modifier.weight(1f)) {
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.strategy_detail_button_mini_calendar),
                        onClick = onCalendarClick,
                        emphasized = false,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PlaceholderActionButton(
                        labelText = stringResource(Res.string.strategy_detail_button_calendar),
                        onClick = onCalendarClick,
                    )
                }
            }
        }
        PlaceholderSectionCard {
            PlaceholderSectionTitle(text = stringResource(Res.string.strategy_detail_section_destinations))
            androidx.compose.material3.Text(
                text = strategyDetailData.recommendedDestinations.joinToString(" · "),
                style = com.bigong.oguri.core.designsystem.OguriTheme.typography.bodyLarge,
            )
        }
        PlaceholderActionButton(
            labelText = stringResource(Res.string.strategy_detail_button_pdf_pro),
            onClick = onSavePdfProClick,
            emphasized = false,
        )
    }
}
