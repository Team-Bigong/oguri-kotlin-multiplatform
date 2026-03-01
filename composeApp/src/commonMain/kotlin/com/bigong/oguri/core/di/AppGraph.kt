package com.bigong.oguri.core.di

import com.bigong.oguri.data.remote.HomeRemoteDataSource
import com.bigong.oguri.data.remote.KtorHomeRemoteDataSource
import com.bigong.oguri.data.repository.DefaultHomeRepository
import com.bigong.oguri.domain.repository.HomeRepository
import com.bigong.oguri.feature.home.ui.HomeViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Provider
import io.ktor.client.HttpClient

@DependencyGraph
interface AppGraph {
    val homeViewModelProvider: Provider<HomeViewModel>

    @Provides
    fun provideHomeRemoteDataSource(implementation: KtorHomeRemoteDataSource): HomeRemoteDataSource = implementation

    @Provides
    fun provideHomeRepository(implementation: DefaultHomeRepository): HomeRepository = implementation

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
        ): AppGraph
    }
}
