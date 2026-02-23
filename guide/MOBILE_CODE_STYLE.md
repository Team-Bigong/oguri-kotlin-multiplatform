# MOBILE_CODE_STYLE

## 목표
- Android/iOS 모두에서 **동일한 공통 로직(shared)** 을 최대한 사용한다.
- UI/Domain/Data 계층을 명확히 분리하고, **테스트 가능한 설계**를 기본값으로 한다.
- Kotlin 코드 품질은 “읽기 쉬움, 예측 가능함, 변경 용이성”을 최우선으로 한다.

---

## 1) Kotlin 기본 스타일

### 1.1 파일 / 클래스 / 함수 네이밍
- **축약어 사용 지양**: 의미가 명확한 전체 단어를 사용한다.
    - ✅ `HolidayStrategyCalculator`
    - ❌ `HolStratCalc`
- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE` + `const val`

### 1.2 타입 명시 규칙 (프로젝트 기본)
- 가능한 한 **타입을 명시**한다. (특히 public API, 프로퍼티, 복잡한 제네릭, Flow/State)
    - ✅ `val remainingAnnualLeaveDays: Int = 15`
    - ✅ `val uiStateFlow: StateFlow<HomeUiState> = ...`
- 단, 지역 스코프에서 명백한 경우는 생략 가능
    - ✅ `val strategyId = StrategyId(value = "2026-10-best")`

### 1.3 매직 넘버 금지
- 하드코딩 숫자는 `const val` 또는 `object`/`enum`/`sealed`로 이름 부여
    - ✅ `const val BANNER_AD_MAX_HEIGHT_RATIO: Float = 0.12f`

### 1.4 문자열 하드코딩 금지
- UI 문자열은 **shared-resources**(예: compose-resources/moko-resources)로 관리한다.
- “키 접근”은 화면별 `*Strings.kt`에 모아서 UI 가독성을 유지한다.

---

## 2) 아키텍처 규약 (UI / Domain / Data)

### 2.1 의존성 방향
- Google 권장: **data <- domain <- ui**
    - `data`는 `domain`/`ui`를 import 하지 않는다.
    - `domain`은 `ui`를 import 하지 않는다.
    - `ui`는 `domain`만 import 한다.

### 2.2 Domain 규칙
- Domain은 **프레임워크 독립**(Kotlin 표준 라이브러리 수준)
- Domain에는:
    - `UseCase`(Interactor)
    - `Repository interface`
    - 순수 모델(Value Object 포함)
    - 정책/검증 로직
- Domain은:
    - UI 상태/컴포넌트/플랫폼 API를 알지 못한다.

### 2.3 Data 규칙
- Data는:
    - Remote/Local 데이터 소스 + Mapping
    - Domain Repository 구현체 제공
- Data에서 발생한 예외는 **도메인 오류 모델**로 변환해 boundary를 넘긴다.

---

## 3) UI 상태 모델링 (권장: MVI-lite)

### 3.1 3요소 표준
각 feature UI는 아래 3요소를 기본으로 가진다.

- `UiState` : 화면에 필요한 모든 상태(불변)
- `UiAction` : 사용자 의도(클릭/입력/리프레시)
- `UiEvent` : one-shot 이벤트(네비게이션, 토스트, 외부 링크 등)

예시 네이밍:
- `HomeUiState`
- `HomeUiAction`
- `HomeUiEvent`

### 3.2 상태는 단방향
- UI는 `StateFlow<UiState>`를 관찰한다.
- UI는 액션만 ViewModel에 전달한다.
- ViewModel은 상태를 갱신하고 이벤트를 발행한다.

### 3.3 one-shot 이벤트 채널
- `MutableSharedFlow<UiEvent>` 사용
- `replay = 0` 기본
- 네비게이션/외부앱 열기/권한 요청은 이벤트로 처리한다.

---

## 4) 코루틴 / Flow 규약

### 4.1 Dispatcher 추상화
- shared에서 Dispatcher를 직접 하드코딩하지 않는다.
- `CoroutineDispatcherProvider` 인터페이스를 두고 DI로 주입한다.

권장 구성:
- `main`
- `io`
- `default`

### 4.2 Flow 반환 원칙
- Repository는 가능한 한 **Flow 기반**으로 제공
    - 상태성 데이터: `Flow<T>` 또는 `StateFlow<T>`
    - 단발 네트워크: `suspend` + `Result`(아래 참고)

### 4.3 취소/에러 처리
- ViewModel은 `SupervisorJob` 기반 스코프 사용을 권장
- 취소는 에러로 취급하지 않는다(예: `CancellationException`은 전파)

---

## 5) Result / Error 모델 규약

### 5.1 Result 타입 표준화
- shared `core-common`에 아래를 둔다.
    - `sealed class AppResult<out T>`
    - `sealed class AppError`

권장 형태:
- `AppResult.Success(data)`
- `AppResult.Failure(error)`

### 5.2 Error 분류(예시)
- `NetworkError` (timeout, offline, badResponse)
- `ServerError` (http code + server message)
- `ValidationError` (입력 검증 실패)
- `UnknownError` (예상 못한 예외)

### 5.3 로깅
- Domain에는 로깅을 두지 않는다(필요하면 Port로 추상화).
- Data 또는 App 레벨에서 `Logger` 인터페이스로 주입받아 사용한다.

---

## 6) DI 규약

### 6.1 shared DI의 역할
- shared는 “조립 가능한 기본 바인딩”을 제공한다.
- 플랫폼별 구현(광고/결제/권한 등)은 각 OS 앱에서 바인딩해 주입한다.

### 6.2 생성 규칙
- ViewModel/UseCase/Repository는 생성자 주입 우선
- 서비스 로케이터/전역 접근은 금지(불가피하면 app 레벨로 제한)

---

## 7) 플랫폼 종속 구현 가이드

### 7.1 Kotlin으로 구현 가능: 추상화 + DI
- shared에 `interface`를 두고, Android/iOS가 각각 구현체를 제공한다.

예시(개념):
- `ExternalLinkOpener`
- `AppSettingsOpener`
- `DeviceInfoProvider`
- `AnalyticsTracker`

### 7.2 순수 네이티브 필요: 최상단 콜백으로 끌어올리기
- shared에서는 “요청 이벤트”만 발생시키고,
- **각 OS 앱이 콜백 구현**을 제공해 동작시킨다.

적용 대상(대표):
- 광고(AdMob)
- 결제(StoreKit / Google Play Billing)
- iOS 특정 권한 프롬프트 트리거
- OS별 Deep Link/Universal Link 처리

권장 형태:
- `PlatformCallbacks` (단일 레지스트리)
    - `openSupportEmail(subject: String, body: String)`
    - `startPurchase(productId: String)`
    - `showBannerAd()` 등 (단, 광고 렌더링은 가능한 “호스트 UI”에서)

---

## 8) Compose Multiplatform UI 스타일

### 8.1 구성 원칙
- `Route`와 `Screen` 분리
    - `Route`: 상태 수집/이벤트 처리/DI 접근
    - `Screen`: 순수 UI(파라미터만 받고 렌더)

### 8.2 파라미터
- `Screen`은 가능한 한 아래만 받는다.
    - `uiState: UiState`
    - `onAction: (UiAction) -> Unit`
- 외부 의존(Repository 등)을 Screen에 직접 주입하지 않는다.

### 8.3 상태 안정성
- `UiState`는 `data class`로 불변 유지
- 빈번히 변하는 리스트는 “키 안정성”을 고려한다(예: `strategyId`)

---

## 9) 테스트 규약(간단)
- Domain UseCase는 unit test 1순위
- Repository는 fake datasource로 unit test 가능하게 설계
- UI는 최소한의 state reducer/mapper를 테스트(가능 범위)

---

## 10) 예시: 파일/패키지 규칙(요약)
- `presentation/` : ViewModel, UiState, UiAction, UiEvent
- `ui/` : Route, Screen, Components
- `navigation/` : route 정의(선택)
- `data/` : datasource, mapper, repositoryImpl
- `domain/` : model, repository interface, usecase
