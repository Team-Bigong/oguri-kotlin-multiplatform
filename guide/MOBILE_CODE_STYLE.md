# MOBILE_CODE_STYLE

## 핵심 원칙
- 코드 가독성, 예측 가능성, 변경 용이성을 우선한다.
- UI 문자열은 리소스로 관리한다.
- 하드코딩 매직 넘버는 역할이 분명한 상수로 분리한다.
- 레이어 경계를 흐리는 편의성보다 오용하기 어려운 API를 우선한다.

## Kotlin 네이밍
- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- 불필요한 축약어를 사용하지 않는다. 예: `msg`, `cnt`, `btn` 지양
- `XxxList` 네이밍은 지양하고 의미 있는 복수형을 사용한다. 예: `rankLabels`, `hotelImageUrls`
- 단, UseCase 이름은 현재 관례에 맞춰 `List` 접미사를 허용한다. 예: `GetRecommendPeriodListUseCase`

## 타입 선언 규칙
- public API, 클래스 프로퍼티, 상수, 함수 반환 타입은 명시한다.
- 함수 내부 지역 변수는 가독성을 해치지 않는 범위에서 타입 추론을 사용한다.
- Compose callback 파라미터는 의미가 드러나도록 명명한다. 예: `onPlaceClick`, `onRetryClick`, `onLoginRequired`

## 문자열/상수
- 사용자 노출 문자열은 `composeResources` 문자열 리소스로 관리한다.
- 새 문자열은 `values`, `values-ko`, `values-ja`, `values-zh`에 모두 추가한다.
- 외부 문서 URL은 `composeResources/values/uris.xml`로 관리한다.
- 상수화 기준은 아래 중 하나라도 만족하는 경우다.
  1. 외부 계약값: API path, header/query key, storage key, scheme, endpoint
  2. 비즈니스 규칙값: 최소/최대 범위, timeout, interval, retry, threshold
  3. 파일/레이어를 넘어 재사용되는 값
- 단발성 UI 값은 인라인을 허용한다. 예: `8.dp`, `RoundedCornerShape(8.dp)`
- 복잡한 표현식이 3회 이상 재사용될 때만 파일 지역 `private val`을 허용한다.
- `const val`은 원시 타입/문자열 계약값에 사용하고, `Dp`, `Shape`, `Color`는 인라인 또는 지역 `val`을 사용한다.

## Compose UI 규칙
- `Route`와 `Screen`은 별도 파일로 분리한다.
- `Route`는 상태 연결, side effect 처리, 이벤트 위임을 담당한다.
- `Screen`은 순수 UI 렌더링과 상태 분기만 담당한다.
- `Route`에서 `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다.
- `ViewModel`은 `viewModelScope`를 사용하고 별도 scope를 만들지 않는다.
- `Screen` 파일이 커지면 카드/섹션/버튼을 `ui/component`로 분리한다.
- 공용 컴포넌트는 `core/ui/component`에 둔다.
- 각 `Screen`과 `ui/component` 컴포넌트는 가능한 한 프리뷰를 제공한다.
- 공통 소스셋 제한으로 `commonMain` 프리뷰가 어려우면 `androidMain` 프리뷰 파일로 대체한다.
- 플랫폼별 화면 구현이 필요하면 `commonMain`의 `expect`와 각 플랫폼 source set의 `actual`로 분리한다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.
- 혼합 스타일 텍스트는 `getStyledText`를 사용한다.
- 화면 전용 `SideEffect`는 `ui/model`에 분리한다.
- 외부 문서(약관/개인정보/건의하기)는 `WebDocument` route와 `PlatformWebView`로 처리한다.
- 삭제/로그아웃 확인 팝업은 공용 `ConfirmAlertDialog`를 재사용한다.
- 스낵바는 공용 `OguriSnackBar`를 사용하고, `NavDisplay`의 단일 `SnackbarHostState`와 `OguriSnackBarHost`로 관리한다.
- 스낵바 표시는 `showOguriSnackbar(...)` 확장 함수를 사용하며 타입(`SUCCESS`, `ALERT`, `INFO`)을 명시한다.
- 네트워크 에러 재시도 상태 UI는 공용 `NetworkErrorRetryContent`를 사용한다.
- 네트워크 초기 로딩은 화면 구조와 동일한 skeleton UI를 우선한다.
- 스켈레톤 애니메이션은 공용 `SkeletonBox` 기반 Shimmer로 구현하고, 화면별 skeleton 컴포넌트를 둔다.
- 로그인/제출/탈퇴처럼 짧은 blocking 상태는 `CenteredLoadingIndicator`를 사용할 수 있다.

## 상태/동시성
- UI 상태는 `MutableStateFlow` + immutable `UiState`로 관리한다.
- one-shot event는 `MutableSharedFlow` 기반 `SideEffect`로 분리한다.
- 사용자 요청에 따른 작업은 `viewModelScope`에서 관리한다. 화면이 활성화된 동안만 필요한 조회는 `repeatOnLifecycle`처럼 화면 생명주기에 묶고 취소를 전파한다.
- 저장 성공·편집 충돌·접근 종료처럼 화면을 다시 열어도 필요한 결과는 UI 상태로 유지한다. 저장 명령은 최신값으로 덮어쓰는 보기 상태와 분리한다.
- 중복 클릭 방지는 `isLoading`, `isSubmitting` 같은 상태로 먼저 막는다.
- Paging 화면은 `LazyPagingItems`의 loading/error 상태를 UI에 명시한다.
- token refresh처럼 공유 자원을 갱신하는 로직은 `Mutex` 등으로 중복 실행을 방지한다.

## 패키지 규칙
- `presentation` 패키지를 사용하지 않는다.
- 화면 코드는 `feature/{name}/ui` 하위에 둔다.
- `UiState`와 `SideEffect`는 `feature/{name}/ui/model`에 둔다.
- 화면 내부 컴포넌트는 `feature/{name}/ui/component`에 둔다.
- 여러 화면에서 재사용하는 컴포넌트는 `core/ui/component`에 둔다.
- `data` 레이어 네트워크 모델은 `model/request`, `model/response`로 분리한다.
- `core/deeplink`, `core/ad`, `core/analytics`, `core/notification`, `core/platform`에는 여러 화면에서 공통으로 쓰는 기능을 둔다.
- 광고 모델의 `url`은 목적지 링크로 사용하고, 광고 이미지 선택은 UI 레이어에서 `platform` 기준으로 처리한다.

## Ktlint
- `composeApp`와 `server`는 `ktlint` 플러그인을 명시적으로 적용한다.
- 실행 커맨드:
  - `./gradlew :composeApp:ktlintCheck`
  - `./gradlew :composeApp:ktlintFormat`
  - `./gradlew :server:ktlintCheck`
- 공통 설정은 루트 `build.gradle.kts`의 `subprojects { pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") { ... } }`에서 관리한다.
- 현재 ktlint 설정은 `ignoreFailures = true`이다. 경고를 무시해도 된다는 의미가 아니라, CI/빌드 차단 여부만 완화한 상태로 해석한다.
