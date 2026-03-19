package com.bigong.oguri.feature.login.ui.model

sealed interface LoginSideEffect {
    data object LoginFailed : LoginSideEffect
}
