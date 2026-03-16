package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_alert
import oguri.composeapp.generated.resources.ic_info
import oguri.composeapp.generated.resources.ic_success
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class OguriSnackBarType {
    SUCCESS,
    ALERT,
    INFO,
}

class OguriSnackBarVisuals(
    override val message: String,
    val type: OguriSnackBarType,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

suspend fun SnackbarHostState.showOguriSnackbar(
    message: String,
    type: OguriSnackBarType = OguriSnackBarType.INFO,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult =
    showSnackbar(
        visuals =
            OguriSnackBarVisuals(
                message = message,
                type = type,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration,
            ),
    )

@Composable
fun OguriSnackBarHost(
    hostState: SnackbarHostState,
    hasBottomNavigation: Boolean,
    modifier: Modifier = Modifier,
) {
    val bottomOffset =
        if (hasBottomNavigation) {
            12.dp
        } else {
            16.dp
        }
    SnackbarHost(
        hostState = hostState,
        modifier =
            modifier
                .let { snackBarModifier ->
                    if (hasBottomNavigation) {
                        snackBarModifier
                    } else {
                        snackBarModifier.navigationBarsPadding()
                    }
                }
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomOffset),
        snackbar = { snackbarData ->
            val visuals = snackbarData.visuals as? OguriSnackBarVisuals
            val type = visuals?.type ?: OguriSnackBarType.INFO
            OguriSnackBar(
                message = snackbarData.visuals.message,
                type = type,
            )
        },
    )
}

@Composable
fun OguriSnackBar(
    message: String,
    type: OguriSnackBarType,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = Neutral50.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                ).padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Image(
            painter = painterResource(resource = type.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = message,
            style = OguriTheme.typography.bodyLarge,
            color = Neutral0,
        )
    }
}

private fun OguriSnackBarType.iconResource(): DrawableResource =
    when (this) {
        OguriSnackBarType.SUCCESS -> Res.drawable.ic_success
        OguriSnackBarType.ALERT -> Res.drawable.ic_alert
        OguriSnackBarType.INFO -> Res.drawable.ic_info
    }
