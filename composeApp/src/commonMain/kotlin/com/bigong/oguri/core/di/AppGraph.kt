package com.bigong.oguri.core.di

import com.bigong.oguri.core.deeplink.DeepLinkStore
import com.bigong.oguri.core.deeplink.provideDeepLinkStore
import com.bigong.oguri.data.remote.AuthRemoteDataSource
import com.bigong.oguri.data.remote.CalendarRemoteDataSource
import com.bigong.oguri.data.remote.HomeRemoteDataSource
import com.bigong.oguri.data.remote.KtorAuthRemoteDataSource
import com.bigong.oguri.data.remote.KtorCalendarRemoteDataSource
import com.bigong.oguri.data.remote.KtorHomeRemoteDataSource
import com.bigong.oguri.data.remote.KtorMyPageRemoteDataSource
import com.bigong.oguri.data.remote.KtorPlaceDetailRemoteDataSource
import com.bigong.oguri.data.remote.MyPageRemoteDataSource
import com.bigong.oguri.data.remote.PlaceDetailRemoteDataSource
import com.bigong.oguri.data.repository.DefaultAuthRepository
import com.bigong.oguri.data.repository.DefaultCalendarRepository
import com.bigong.oguri.data.repository.DefaultHomeRepository
import com.bigong.oguri.data.repository.DefaultMyPageRepository
import com.bigong.oguri.data.repository.DefaultPlaceDetailRepository
import com.bigong.oguri.domain.repository.AuthRepository
import com.bigong.oguri.domain.repository.CalendarRepository
import com.bigong.oguri.domain.repository.HomeRepository
import com.bigong.oguri.domain.repository.MyPageRepository
import com.bigong.oguri.domain.repository.PlaceDetailRepository
import com.bigong.oguri.feature.calendar.ui.CalendarViewModel
import com.bigong.oguri.feature.home.ui.HomeViewModel
import com.bigong.oguri.feature.login.ui.LoginViewModel
import com.bigong.oguri.feature.mypage.ui.MyPageViewModel
import com.bigong.oguri.feature.onboarding.ui.OnboardingViewModel
import com.bigong.oguri.feature.perioddetail.ui.PeriodDetailViewModel
import com.bigong.oguri.feature.placedetail.ui.PlaceDetailViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

@DependencyGraph
interface AppGraph {
    val deepLinkStore: DeepLinkStore
    val calendarViewModelProvider: Provider<CalendarViewModel>
    val homeViewModelProvider: Provider<HomeViewModel>
    val loginViewModelProvider: Provider<LoginViewModel>
    val myPageViewModelProvider: Provider<MyPageViewModel>
    val onboardingViewModelProvider: Provider<OnboardingViewModel>
    val periodDetailViewModelProvider: Provider<PeriodDetailViewModel>
    val placeDetailViewModelProvider: Provider<PlaceDetailViewModel>

    @Provides
    fun provideAuthRemoteDataSource(implementation: KtorAuthRemoteDataSource): AuthRemoteDataSource = implementation

    @Provides
    fun provideCalendarRemoteDataSource(implementation: KtorCalendarRemoteDataSource): CalendarRemoteDataSource = implementation

    @Provides
    fun provideHomeRemoteDataSource(implementation: KtorHomeRemoteDataSource): HomeRemoteDataSource = implementation

    @Provides
    fun provideMyPageRemoteDataSource(implementation: KtorMyPageRemoteDataSource): MyPageRemoteDataSource = implementation

    @Provides
    fun providePlaceDetailRemoteDataSource(implementation: KtorPlaceDetailRemoteDataSource): PlaceDetailRemoteDataSource = implementation

    @Provides
    fun provideAuthRepository(implementation: DefaultAuthRepository): AuthRepository = implementation

    @Provides
    fun provideCalendarRepository(implementation: DefaultCalendarRepository): CalendarRepository = implementation

    @Provides
    fun provideHomeRepository(implementation: DefaultHomeRepository): HomeRepository = implementation

    @Provides
    fun provideMyPageRepository(implementation: DefaultMyPageRepository): MyPageRepository = implementation

    @Provides
    fun providePlaceDetailRepository(implementation: DefaultPlaceDetailRepository): PlaceDetailRepository = implementation

    @Provides
    fun provideDeepLinkStoreInstance(): DeepLinkStore = provideDeepLinkStore()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
        ): AppGraph
    }
}
