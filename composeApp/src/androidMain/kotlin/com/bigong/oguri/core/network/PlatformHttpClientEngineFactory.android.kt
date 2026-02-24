package com.bigong.oguri.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun providePlatformHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
