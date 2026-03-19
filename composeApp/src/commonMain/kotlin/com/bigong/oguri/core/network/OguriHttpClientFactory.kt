package com.bigong.oguri.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun provideOguriHttpClient(): HttpClient =
    HttpClient(engineFactory = providePlatformHttpClientEngineFactory()) {
        expectSuccess = true
        install(plugin = OguriRequestHeadersPlugin)
        install(plugin = ContentNegotiation) {
            json(
                json =
                    Json {
                        ignoreUnknownKeys = true
                    },
            )
        }
    }
