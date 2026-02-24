package com.bigong.oguri.core.di

import com.bigong.oguri.core.network.providePlatformHttpClientEngineFactory
import com.bigong.oguri.data.di.StrategyDataDiContainer
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.UserMvpStateRepository
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@DependencyGraph(bindingContainers = [StrategyDataDiContainer::class])
interface AppGraph {
    val annualLeaveStrategyRepository: AnnualLeaveStrategyRepository
    val userMvpStateRepository: UserMvpStateRepository

    @Provides
    fun provideAppConfiguration(): AppConfiguration {
        return AppConfiguration(
            baseUrl = "https://api.oguri.example",
        )
    }

    @Provides
    fun provideHttpClient(
        appConfiguration: AppConfiguration,
    ): HttpClient {
        return HttpClient(engineFactory = providePlatformHttpClientEngineFactory()) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = appConfiguration.baseUrl.removePrefix("https://")
                }
            }
        }
    }
}

data class AppConfiguration(
    val baseUrl: String,
)
