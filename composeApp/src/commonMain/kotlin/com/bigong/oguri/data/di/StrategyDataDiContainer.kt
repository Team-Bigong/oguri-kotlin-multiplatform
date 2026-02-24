package com.bigong.oguri.data.di

import com.bigong.oguri.data.remote.KtorStrategyRemoteDataSource
import com.bigong.oguri.data.remote.StrategyRemoteDataSource
import com.bigong.oguri.data.repository.AnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.DefaultAnnualLeaveStrategyRepository
import com.bigong.oguri.data.repository.InMemoryUserMvpStateRepository
import com.bigong.oguri.data.repository.UserMvpStateRepository
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

@BindingContainer
object StrategyDataDiContainer {
    @Provides
    fun provideStrategyRemoteDataSource(
        httpClient: HttpClient,
    ): StrategyRemoteDataSource {
        return KtorStrategyRemoteDataSource(httpClient = httpClient)
    }

    @Provides
    fun provideAnnualLeaveStrategyRepository(
        strategyRemoteDataSource: StrategyRemoteDataSource,
    ): AnnualLeaveStrategyRepository {
        return DefaultAnnualLeaveStrategyRepository(strategyRemoteDataSource = strategyRemoteDataSource)
    }

    @Provides
    fun provideUserMvpStateRepository(): UserMvpStateRepository {
        return InMemoryUserMvpStateRepository
    }
}
