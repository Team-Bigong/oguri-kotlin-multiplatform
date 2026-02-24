package com.bigong.oguri.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun providePlatformHttpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
