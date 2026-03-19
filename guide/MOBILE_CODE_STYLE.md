# MOBILE_CODE_STYLE

## 핵심 원칙
- 코드 가독성, 예측 가능성, 변경 용이성을 우선한다.
- UI 문자열은 리소스로 관리한다.
- 하드코딩 매직 넘버는 상수화한다.

## Kotlin 네이밍
- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- 축약어 사용 금지 (`msg`, `cnt`, `btn` 지양)
- `XxxList` 네이밍 지양
: 예) `rankLabels`, `hotelImageUrls` 사용
- 단, UseCase 이름은 `List` 접미사 허용
: 예) `GetRecommendPeriodListUseCase`

## 타입 선언 규칙
- 프로퍼티/파라미터/반환 타입은 명시한다.
- 같은 함수 내부의 지역 변수는 타입 추론을 사용한다.

## 문자열/상수
- 사용자 노출 문자열은 `composeResources` 문자열 리소스로 관리한다.
- 상수화 기준:
: 아래 조건 중 하나라도 만족하면 상수화한다.
  1) 외부 계약값(API path, header/query key, storage key, scheme/endpoint)
  2) 비즈니스 규칙값(임계값, 최소/최대 범위, 지연 시간, 가중치)
  3) 파일/레이어를 넘어서 재사용되는 값
- 단발성 UI 값(예: 1~2회만 쓰는 `8.dp`, `RoundedCornerShape(8.dp)`)은 상수화하지 않고 인라인한다.
- 복잡한 표현식이 3회 이상 재사용될 때만 파일 지역 `private val`을 허용한다.
- `const val`은 원시 타입/문자열 계약값에만 사용하고, `Dp/Shape/Color`는 인라인 또는 지역 `val`을 사용한다.

## Compose UI 규칙
- `Route`와 `Screen`은 반드시 별도 파일로 분리한다.
- `Route`는 상태 연결/이벤트 위임 담당.
- `Screen`은 순수 UI 렌더링 담당.
- `Route`에서 `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다.
- `Screen` 파일은 200줄 이상이 되지 않도록 유지하고, 복잡한 UI는 `ui/component`로 분리한다.
- `Screen`은 레이아웃 조합과 상태 분기만 담당하고, 카드/섹션/버튼은 컴포넌트로 위임한다.
- 각 `Screen`과 각 `ui/component` 컴포넌트는 프리뷰를 제공한다.
- 공통 소스셋 제한으로 `commonMain` 프리뷰가 어려운 경우 `androidMain` 프리뷰 파일로 대체한다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.
- 혼합 스타일 텍스트는 `getStyledText`만 사용한다. (`getColoredText` 사용 금지)
- 화면 전용 `SideEffect`는 `ui/model`에 파일로 분리한다.
- 외부 문서(약관/개인정보/건의하기)는 `WebDocument` 라우트 + `PlatformWebView`로 처리한다.
- 삭제/로그아웃 확인 팝업은 공용 `ConfirmAlertDialog`를 재사용한다.
- 스낵바는 공용 `OguriSnackBar`를 사용하고, `NavDisplay`의 단일 `SnackbarHostState` + `OguriSnackBarHost`로 관리한다.
- 스낵바 표시는 `showOguriSnackbar(...)` 확장 함수를 사용하며, 타입(`SUCCESS`, `ALERT`, `INFO`)을 명시한다.
- 네트워크 에러 재시도 상태 UI는 공용 `NetworkErrorRetryContent`를 사용한다.
- 네트워크 로딩 상태는 `CircularProgressIndicator`를 사용하지 않고, 화면 구조와 동일한 스켈레톤 UI를 사용한다.
- 스켈레톤 애니메이션은 공용 `SkeletonBox` 기반 Shimmer로 구현하고, 화면별로 전용 스켈레톤 컴포넌트를 둔다.

## Ktlint
- `composeApp`와 `server`는 모두 `ktlint` 플러그인을 명시적으로 적용한다.
- 실행 커맨드:
: `./gradlew :composeApp:ktlintCheck`
: `./gradlew :composeApp:ktlintFormat`
: `./gradlew :server:ktlintCheck`
- 공통 설정은 루트 `build.gradle.kts`의 `subprojects { pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") { ... } }`에서 관리한다.

## 패키지 규칙
- `presentation` 패키지를 사용하지 않는다.
- 화면 코드는 `ui` 하위에 둔다.
- `UiState`는 `ui/model`에 둔다.
- 화면 내부 컴포넌트는 `ui/component`에 둔다.
- `data` 레이어 네트워크 모델은 `model/request`, `model/response`로 분리한다.
- 광고 모델의 `url`은 목적지 링크로 사용하고, 광고 이미지 선택은 UI 레이어에서 `platform` 기준으로 처리한다.
