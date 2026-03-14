package com.bigong.oguri.core.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun providePlatformHttpClientEngineFactory(): HttpClientEngineFactory<*>
