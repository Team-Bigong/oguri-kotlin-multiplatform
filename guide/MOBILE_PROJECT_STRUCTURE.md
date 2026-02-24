# MOBILE_PROJECT_STRUCTURE

## 목표
- **Kotlin Multiplatform(KMP)** 기반으로 Android / iOS를 **하나의 Shared 코드베이스**로 최대한 구현한다.
- 아키텍처는 **Google 권장 의존성 방향**을 따른다: **data <- domain <- ui**
    - `data`는 `domain`/`ui`를 import 하지 않는다.
    - `domain`은 `ui`를 import 하지 않는다.
    - `ui`만 `domain`을 import 한다.
- 플랫폼 종속 구현은 다음 우선순위를 따른다.
    1) Kotlin으로 구현 가능: **추상화(인터페이스) + 의존성 주입**으로 shared에서 사용
    2) 순수 네이티브가 필요: **최상단(앱 레이어)으로 콜백 함수를 끌어올리고**, 각 OS 앱에서 네이티브로 구현해 주입

---

## 최상위 레포 구조

oguri/
├─ build-logic/                       # Gradle convention plugins (version, lint, compose, kmp 설정)
├─ gradle/                            # wrapper, versions catalog 등
├─ config/                            # 환경별 설정(dev, prod), 키, 템플릿 (민감정보는 제외)
├─ shared/                            # KMP Shared Root (멀티모듈)
│  ├─ core/
│  │  ├─ core-common/                 # 공통 유틸, Result/Error, Coroutine/Flow 규약, 시간/로케일 추상화
│  │  ├─ core-platform/               # expect/actual: device, appInfo, secureStorage 등
│  │  ├─ core-network/                # Ktor client, serialization, interceptor, retry, logging
│  │  ├─ core-database/               # SQLDelight/Room 대체(선택), local storage 공통화
│  │  ├─ core-designsystem/           # Compose Multiplatform 디자인 토큰(색/타이포/컴포넌트)
│  │  ├─ core-analytics/              # 이벤트 모델 + 트래커 인터페이스(플랫폼 구현 주입)
│  │  └─ core-testing/                # test fixtures, fake, test utilities
│  ├─ domain/
│  │  ├─ domain-model/                # 순수 데이터 모델, 값 객체, 정책 모델 (Kotlin only)
│  │  ├─ domain-usecase/              # UseCase(또는 Interactor) 집합
│  │  └─ domain-repository/           # Repository interface (port)
│  ├─ data/
│  │  ├─ data-remote/                 # remote datasource (API DTO, mapping)
│  │  ├─ data-local/                  # local datasource (db, preferences)
│  │  ├─ data-repository/             # domain-repository 구현체
│  │  └─ data-mapper/                 # DTO <-> Domain 변환 (단방향 규칙)
│  ├─ feature/
│  │  ├─ feature-onboarding/
│  │  │  ├─ feature-onboarding-domain/ # feature별 도메인(선택) - 규모 커지면 분리
│  │  │  ├─ feature-onboarding-data/   # feature별 data(선택)
│  │  │  └─ feature-onboarding-ui/     # Compose 화면 + ViewModel(공통)
│  │  ├─ feature-home/
│  │  ├─ feature-strategy/
│  │  ├─ feature-calendar/
│  │  └─ feature-mypage/
│  ├─ app/
│  │  ├─ app-ui/                      # 공통 네비게이션, 루트 화면, 전역 상태(세션 등)
│  │  └─ app-di/                      # Koin/Hilt 대체: shared DI 모듈 선언
│  └─ shared-resources/               # string, image 등 멀티플랫폼 리소스 (moko-resources 또는 compose-resources)
├─ androidApp/                        # Android application (Activity, Manifest, AdMob, Billing, Firebase 등)
├─ iosApp/                            # iOS application (SwiftUI/UIViewController, StoreKit, AdMob, Firebase 등)
├─ tools/                             # 스크립트(릴리즈, 스냅샷, 코드젠 등)
└─ docs/                              # 문서(아키텍처, 릴리즈 프로세스, API 명세)

> **설명**
- `shared/` 내부를 **멀티모듈**로 쪼개서 기능 단위로 확장 가능하게 한다.
- feature가 작으면 `feature-xxx-ui`만 두고, 커지면 `feature-xxx-domain/data`까지 분리한다.
- Android/iOS 앱은 “최상단(앱 레이어)”에서만 플랫폼 SDK를 직접 다룬다.

---

## 모듈 의존성 규칙

### 레이어 규칙 (Google 권장)
- `ui` → `domain` → `data` 방향으로만 의존성이 흐른다.
- 실 구현은 DI로 연결한다.

예시:
- `feature-home-ui` imports:
    - `domain-usecase`, `domain-model`, `core-common`, `core-designsystem`
- `data-repository` imports:
    - `domain-repository`, `domain-model`, `core-network`, `core-database`
- `domain-usecase` imports:
    - `domain-model`, `domain-repository`, `core-common`
- `domain-model` imports:
    - 아무 레이어도 import 하지 않음 (Kotlin/stdlib 정도만)

---

## Feature 기반 패키지 구조 (공통 UI 기준)

각 `feature-xxx-ui` 모듈 내부 패키지 예시:

feature-home-ui/
└─ src/commonMain/kotlin/com/oguri/feature/home/
├─ ui/
│  ├─ HomeRoute.kt                 # Screen entry, state collection
│  ├─ HomeScreen.kt                # Pure UI
│  ├─ HomeViewModel.kt             # ViewModel (ui 루트)
│  ├─ model/
│  │  └─ HomeUiState.kt            # immutable state
│  ├─ component/
│  │  └─ HomeComponents.kt         # 화면 내부 컴포넌트
│  └─ HomeStrings.kt               # 화면 전용 문자열 키(리소스 연결, 선택)
└─ navigation/
└─ HomeNavigation.kt            # route, deep link(선택)

> MVP shared 규칙: `presentation` 패키지를 따로 두지 않고, `ui` 내부(`ui`, `ui/model`, `ui/component`)로 정리한다.
> ViewModel은 `commonMain`에 두고, 플랫폼별 UI 호스트(Android Activity / iOS SwiftUI)가 이를 사용한다.

---

## 플랫폼 종속 코드 배치 원칙

### 1) Kotlin으로 구현 가능하면: 추상화 + DI
- `shared/core/core-platform`에 인터페이스(또는 expect/actual)로 추상화한다.
- `shared/app/app-di`에서 기본 바인딩 제공.
- 필요 시 Android/iOS 앱에서 platform-specific 바인딩을 override한다.

예:
- `DeviceInfoProvider`
- `OpenExternalLinkHandler`
- `TimeProvider`
- `NotificationPermissionController`(가능 범위 내)

### 2) 순수 네이티브가 필요하면: 최상단 콜백으로 끌어올리기
- Shared에서는 “요청 이벤트”만 발행하고,
- 최상단(각 OS 앱)이 콜백을 구현해 주입한다.

예:
- 결제(StoreKit / Google Play Billing)
- 광고(AdMob iOS/Android SDK)
- 시스템 설정 화면 이동(플랫폼별 인텐트/URL scheme)
- 권한 UI 트리거(iOS 권한 프롬프트 등)

구조 예시:

shared/
└─ core/core-common/
└─ PlatformCallbackRegistry.kt     # shared에서 호출할 콜백 타입 정의

androidApp/
└─ PlatformCallbacksAndroid.kt        # 실제 SDK 호출 구현 + DI로 주입

iosApp/
└─ PlatformCallbacksIos.swift         # Swift 구현 + shared에 주입(bridge)

---

## DI(의존성 주입) 구성 위치

- `shared/app/app-di`:
    - shared에서 제공 가능한 바인딩(UseCase, Repository, HttpClient, Database 등)
- `androidApp` / `iosApp`:
    - 광고/결제/권한 등 네이티브 구현 바인딩 등록
    - 환경별 설정(baseUrl, debugFlag 등) 주입

---

## 리소스(문자열/이미지) 관리
- 출력 문자열은 하드코딩하지 않고 `shared/shared-resources`로 통일한다.
- 화면별로 필요한 키는 `HomeStrings.kt` 같은 파일에서 “접근 함수/키”만 정리해 UI 가독성을 유지한다.

---

## 빌드 및 환경 분리
- `config/`에 환경별 설정 템플릿을 두고,
- 민감정보(API key)는 CI secret 또는 로컬 ignored 파일로 관리한다.
- Android: buildTypes/flavors
- iOS: xcconfig 또는 build configuration
- Shared: `expect val isDebugBuild: Boolean` 같은 형태로 필요한 최소한만 전달

---

## 추천 최소 모듈 셋 (1.0.0 기준)
처음부터 과분리하지 않고, 아래로 시작해서 필요 시 feature를 분리한다.

- `core-common`, `core-platform`, `core-network`, `core-designsystem`
- `domain-model`, `domain-repository`, `domain-usecase`
- `data-remote`, `data-local`, `data-repository`
- `app-ui`, `app-di`
- `feature-home-ui`, `feature-strategy-ui`, `feature-calendar-ui`, `feature-mypage-ui`, `feature-onboarding-ui`
- `androidApp`, `iosApp`
