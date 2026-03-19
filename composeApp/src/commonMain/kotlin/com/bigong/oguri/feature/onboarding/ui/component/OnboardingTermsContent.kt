package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.onboarding_terms_all_agree
import oguri.composeapp.generated.resources.onboarding_terms_privacy_policy_required
import oguri.composeapp.generated.resources.onboarding_terms_service_required
import oguri.composeapp.generated.resources.onboarding_terms_title
import oguri.composeapp.generated.resources.onboarding_terms_title_highlight
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingTermsContent(
    isServiceTermsChecked: Boolean,
    isPrivacyPolicyChecked: Boolean,
    onServiceTermsToggle: () -> Unit,
    onPrivacyPolicyToggle: () -> Unit,
    onServiceTermsOpen: () -> Unit,
    onPrivacyPolicyOpen: () -> Unit,
    onAgreeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OnboardingHighlightedTitle(
            fullText = stringResource(Res.string.onboarding_terms_title),
            highlightedText = stringResource(Res.string.onboarding_terms_title_highlight),
        )
        Spacer(modifier = Modifier.height(28.dp))
        OnboardingTermsRow(
            text = stringResource(Res.string.onboarding_terms_service_required),
            isChecked = isServiceTermsChecked,
            onToggle = onServiceTermsToggle,
            onOpenDetail = onServiceTermsOpen,
        )
        OnboardingTermsRow(
            text = stringResource(Res.string.onboarding_terms_privacy_policy_required),
            isChecked = isPrivacyPolicyChecked,
            onToggle = onPrivacyPolicyToggle,
            onOpenDetail = onPrivacyPolicyOpen,
        )

        Spacer(modifier = Modifier.height(28.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = Mint70, shape = RoundedCornerShape(8.dp))
                    .noRippleClickable(onClick = onAgreeAllClick)
                    .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.onboarding_terms_all_agree),
                style = OguriTheme.typography.cardTitle,
                color = Neutral0,
            )
        }
    }
}
