# MOBILE_PROJECT_STRUCTURE

## 현재 기준
- 확인 기준: `settings.gradle.kts`, `composeApp/build.gradle.kts`, `shared/build.gradle.kts`, `composeApp/src/**`
- 프로젝트 모듈은 `composeApp`, `shared`, `server`로 구성한다.
- 모바일 앱의 주 소스 루트는 `composeApp/src/commonMain/kotlin/com/bigong/oguri`이다.
- Android 전용 구현과 프리뷰는 `composeApp/src/androidMain/kotlin/com/bigong/oguri`에 둔다.
- `composeApp`은 Android, iOS(`iosArm64`, `iosSimulatorArm64`)를 대상으로 하는 Kotlin Multiplatform + Compose Multiplatform 앱이다.

## 모바일 디렉터리 구조

```text
composeApp/src/commonMain/kotlin/com/bigong/oguri
├─ app
│  └─ OguriApp.kt
├─ core
│  ├─ ad
│  ├─ analytics
│  ├─ deeplink
│  ├─ designsystem
│  ├─ di
│  ├─ navigation
│  ├─ network
│  ├─ platform
│  ├─ ui
│  │  └─ component
│  └─ util
│     └─ extension
├─ data
│  ├─ local
│  ├─ remote
│  │  └─ model
│  │     ├─ request
│  │     └─ response
│  └─ repository
├─ domain
│  ├─ model
│  ├─ repository
│  └─ usecase
└─ feature
   ├─ splash
   │  └─ ui
   ├─ login
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ onboarding
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ home
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ search
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ calendar
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ perioddetail
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ placedetail
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ photodetail
   │  └─ ui
   │     └─ component
   ├─ mypage
   │  └─ ui
   │     ├─ component
   │     └─ model
   └─ webdocument
      └─ ui
```

## Source Set 기준
- `commonMain`: 공통 UI, navigation, domain, data 추상화/공통 구현, Ktor remote data source, Metro graph를 둔다.
- `androidMain`: Android SDK 의존 구현을 둔다. 예: Kakao/Google 로그인, DataStore/EncryptedSharedPreferences 기반 local data source, AdMob, Amplitude, WebView, preview.
- `iosMain`: iOS `ComposeUIViewController`, Darwin Ktor engine, NSUserDefaults local data source, Apple/Kakao login, WebKit WebView, AdMob/Amplitude/share notification bridge 등 iOS actual 구현을 둔다.
- `commonTest`: 공통 테스트를 둔다.

## 레이어 규칙
- `domain`은 어떤 앱 레이어도 import하지 않는다.
- `data`는 `domain` 추상화를 구현하고, `ui` 또는 `feature`를 import하지 않는다.
- `feature/*/ui`는 `domain`과 `core`에 의존할 수 있지만 `data`를 직접 import하지 않는다.
- `app`은 앱 진입점 역할만 담당하고, 화면 조합은 `core.navigation`에 위임한다.
- `core`는 디자인 시스템, navigation, network, platform bridge, 공용 UI, analytics, deeplink, ad처럼 여러 feature가 공유하는 기반 기능을 둔다.

## UI 파일 구성
- `Route`와 `Screen`은 분리한다.
- `Route`는 ViewModel 연결, lifecycle-aware state 수집, side effect 처리, navigation callback 위임을 담당한다.
- `Screen`은 순수 UI 렌더링, 상태 분기, 레이아웃 조합을 담당한다.
- `ViewModel`은 각 feature의 `ui` 패키지에 둔다.
- `UiState`와 `SideEffect`는 `ui/model`에 둔다.
- 반복 가능한 UI는 `ui/component` 또는 `core/ui/component`로 분리한다.
- `commonMain`에서 preview가 어려운 컴포넌트는 `androidMain`에 `*Preview.android.kt`를 둔다.
- `LoginScreen`처럼 플랫폼 UI 차이가 큰 화면은 `commonMain`에 `expect`, 각 플랫폼 source set에 `actual`을 둘 수 있다.

## 주요 공용 영역
- `core/navigation`: 타입 세이프 `RouteModel`, `MainNavigator`, `MainNavHost`, `NavDisplay`, bottom navigation을 관리한다.
- `core/deeplink`: `oguri://open/...` 및 KakaoLink deeplink를 route로 파싱하고, `DeepLinkStore`로 진입 URL을 전달한다.
- `core/network`: generated config 기반 `BASE_URL`, Ktor client, 전역 header plugin, token store를 관리한다.
- `core/di`: Metro `AppGraph`에서 remote data source, repository, ViewModel provider, deep link store를 제공한다.
- `core/ad`: AdMob app open/banner bridge를 관리한다.
- `core/analytics`: Amplitude event와 Android 초기화를 관리한다.
- `core/platform`: 로그인, 공유, WebView, haptic, back gesture, theme mode, floating bottom navigation 등 플랫폼 bridge를 둔다.

## 리소스
- 공통 문자열/이미지/폰트는 `composeApp/src/commonMain/composeResources`를 사용한다.
- 현재 문자열 리소스는 기본값(`values`, 영어), `values-ko`, `values-ja`, `values-zh`를 유지한다.
- Android framework 리소스는 `composeApp/src/androidMain/res`에 둔다.
- 외부 문서 URI는 `composeResources/values/uris.xml` 키 기반으로 관리한다.
- 사용자 노출 문자열은 Kotlin 상수로 두지 않고 string resource로 분리한다.

## 빌드 설정 메모
- `composeApp/build.gradle.kts`의 `GenerateNetworkConfigTask`가 `DebugNetworkConfig.kt`를 생성한다.
- `local.properties`에서 `debug.base.url`, `release.base.url`, `kakao.key`, `google.web.client.id`, `amplitude.api.key`를 읽는다.
- Android `versionCode`, `versionName`은 루트 `version.properties`의 `CURRENT_PROJECT_VERSION`, `MARKETING_VERSION`을 사용한다.
- `shared` 모듈은 현재 공통 scaffold(`Constants`, `Greeting`, `Platform`) 수준이며, 모바일 domain/data 규칙을 우회하는 용도로 사용하지 않는다.
