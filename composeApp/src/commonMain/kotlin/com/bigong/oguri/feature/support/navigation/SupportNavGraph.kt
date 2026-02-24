package com.bigong.oguri.feature.support.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bigong.oguri.core.navigation.RouteModel
import com.bigong.oguri.data.model.SupportEmailDraft
import com.bigong.oguri.feature.support.ui.SupportInquiryTypeRoute
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.support_email_opened_dummy
import org.jetbrains.compose.resources.stringResource

object SupportNavGraph {
    fun register(
        navGraphBuilder: NavGraphBuilder,
        snackbarHostState: SnackbarHostState,
    ) {
        navGraphBuilder.composable<RouteModel.SupportInquiryType> {
            SupportNavEntry(snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
private fun SupportNavEntry(
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val emailOpenedDummyMessage: String = stringResource(Res.string.support_email_opened_dummy)

    SupportInquiryTypeRoute(
        onOpenSupportEmail = { _: SupportEmailDraft ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = emailOpenedDummyMessage)
            }
        },
    )
}
