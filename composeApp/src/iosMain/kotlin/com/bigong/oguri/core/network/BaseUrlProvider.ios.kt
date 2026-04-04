package com.bigong.oguri.core.network

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
internal actual fun isDebugPlatformBuild(): Boolean = Platform.isDebugBinary
