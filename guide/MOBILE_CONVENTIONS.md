# MOBILE_CONVENTIONS

## 목표
- KMP 기반 iOS/Android 동시 개발에서 “매번 결정하지 않아도 되는 규칙”을 고정한다.
- MVP(오구리 1.0.0) 운영에 필요한 **광고/구독/문의/정책/로그/환경**을 표준화한다.
- 플랫폼 종속 기능은 “공통 우선 → 추상화+DI → 네이티브 콜백” 우선순위를 지킨다.

---

## 1) 네비게이션 규약

### 1.1 바텀 네비게이션 (고정 3탭)
- 탭: `Home`, `Calendar`, `My`
- 탭 간 이동은 “상태 유지”를 기본으로 한다.
- 상세 화면은 탭 라우트 밖에서 `push`로 이동한다.
- 현재 구현 기준(shared `composeApp`):
    - `MainNavHost`에서 전체 그래프를 관리
    - `NavDisplay`에서 테마 + Scaffold + 바텀 네비 + 상단 스낵바 호스트를 관리
    - 라우트 모델은 `core/navigation/RouteModels.kt`의 `@Serializable data object` / `@Serializable data class`로 정의
    - `MainNavigator`(`core/navigation`)가 `NavHostController` 래핑 및 공통 이동 API를 제공
    - 각 feature의 route 등록/화면 진입 wiring은 `feature/*/navigation/*NavGraph.kt`에서 담당
    - Navigation Compose 타입 세이프 라우팅 사용: `composable<Route>()`, `navigate(Route())`, `toRoute<Route>()`

권장 라우트 ID (축약 금지):
- `home`
- `strategy_detail`
- `strategy_calendar`
- `my`

### 1.2 딥링크/유니버설링크(1.0.0 최소)
- 1.0.0에서는 “읽기 전용 딥링크”만 허용한다.
    - 예: 특정 전략 상세로 진입
- 딥링크 파싱은 shared에서 수행하고, 실제 라우팅은 app 레벨에서 실행한다(콜백).

---

## 1.5) MVP 초기 구현 규칙 (임시)
- 1.0.0 초기 구현 단계에서는 `domain` 레이어를 생략하고, `ui`가 `data`를 직접 사용한다.
- 단, 패키지 구조는 `data/*`, `core.di/*`, `feature/*`를 유지해서 이후 `domain` 도입 시 이동 비용을 줄인다.
- 네트워크는 `Ktor HttpClient`로 실제 구성하되, 서버 미구현 동안에는 data layer에서 더미 응답을 반환한다.
- DI는 `Metro`를 사용하며, 루트 그래프는 `core/di`에 둔다. (`data/di`, 추후 `feature/*/di` 확장 예정)
- 사용자 노출 문자열은 `const val`로 두지 않고 `composeResources` 문자열 리소스로 관리한다. (기본/ko 분리)

---

## 2) UI 레이어 규약 (Route / Screen / Component)

### 2.1 Route / Screen 분리
- `Route`: 상태 수집, 이벤트 처리, DI 접근, navigation callback 호출
- `Screen`: 순수 UI. 외부 의존성 접근 금지.
- `ViewModel`: `ui` 패키지 루트에 별도 파일로 분리한다. (`HomeViewModel.kt`)
- `UiState`: `ui/model` 패키지에 별도 파일로 둔다. (`HomeUiState.kt`)
- 화면 내부 서브 컴포넌트는 `ui/component` 패키지로 분리한다. (`HomeComponents.kt`)
- `presentation` 패키지는 사용하지 않는다. (MVP shared 기준)

표준 시그니처:
- `Screen(uiState: UiState, onAction: (UiAction) -> Unit)`

### 2.2 One-shot 이벤트 처리
- `UiEvent`는 `SharedFlow`로 발행한다.
- `UiEvent`에서 가능한 행동:
    - `NavigateTo(route)`
    - `OpenExternalLink(url)`
    - `OpenSupportEmail(subject, body)`
    - `ShowSnackbar(message)`
    - `RequestNativeAction(action)` (네이티브 필요 시)

---

## 3) 광고(배너) 규약 — 1.0.0 고정 정책

### 3.1 광고 노출 화면
- 노출: `Home`, `Calendar`, `StrategyDetail`
- 미노출: `Splash`, `Login`, `Onboarding`, `My`, `Support`(문의/약관)

### 3.2 위치 및 레이아웃
- **하단 고정 배너 1개**만 허용한다.
- 콘텐츠와 광고 영역은 `Divider`(또는 명확한 separation)로 분리한다.
- 광고 높이는 화면의 `10%~12%` 이내를 권장한다.

### 3.3 스크롤 정책
- 광고는 **스크롤 중간 삽입 금지**
- 리스트/카드 사이에 끼워 넣지 않는다.
- 상세 화면도 “하단 고정”만 허용한다.

### 3.4 Pro 사용자 정책
- Pro 사용자는 배너를 제거한다.
- UI에는 “광고 제거됨” 같은 노이즈 표시는 하지 않는다(깔끔함 유지).

---

## 4) Pro(구독) 규약

### 4.1 1.0.0 Pro 혜택(고정)
- 배너 광고 제거
- 연간 전략 PDF 내보내기
- 고급 효율 분석(추가 지표/정렬)

### 4.2 권한 체크 표준
- shared에서 `SubscriptionStatusProvider`(인터페이스)로 상태만 조회한다.
- 실제 결제/복원/구독 관리 UI는 네이티브 필요 → **최상단 콜백으로 끌어올리기**.

표준 상태:
- `NotSubscribed`
- `Subscribed`
- `Unknown` (초기 로딩/네트워크 불가)

### 4.3 기능 플래그(Feature Flag) 규칙
- Pro 기능은 반드시 `subscriptionStatus`로 gate한다.
- UI는 “막기”보다 “유도”를 기본으로 한다.
    - 예: PDF 버튼 클릭 → `UiEvent.NavigateToProPaywall`

---

## 5) 문의(건의) 규약 — 이메일 연결

### 5.1 UX 흐름(표준)
- 마이페이지 → 건의하기 → 문의 유형 선택 → 메일 앱 실행

문의 유형(고정 3종):
- `FeatureSuggestion`
- `BugReport`
- `OtherInquiry`

### 5.2 메일 템플릿 표준
- 수신자: `support@<your-domain>` (확정되면 상수로 고정)
- 제목:
    - `[오구리] 기능 제안`
    - `[오구리] 오류 신고`
    - `[오구리] 기타 문의`
- 본문 자동 포함 항목:
    - 문의 유형
    - 앱 버전
    - OS 버전
    - 기기 모델 (가능한 범위)
    - (선택) 타임존/로케일

### 5.3 구현 규칙
- shared는 “메일 열기”를 직접 하지 않는다.
- shared는 `UiEvent.OpenSupportEmail(subject, body)`만 발행한다.
- Android/iOS 앱이 최상단에서 네이티브로 처리한다.
- MVP 와이어프레임 임시 구현 상태:
    - 문의 유형 선택 후 제목/본문 자동 생성 프리뷰까지 shared에서 구현
    - 실제 메일 앱 실행은 더미 스낵바로 대체 (서버/브리지 구현 전)

---

## 6) 정책/법적 고지 규약

### 6.1 마이페이지 필수 항목
- 이용약관
- 개인정보 처리방침
- 오픈소스 라이선스
- 공지사항
- FAQ

### 6.2 표시 방식
- 1.0.0에서는 **외부 링크 또는 WebView** 중 하나로 통일한다.
- 링크 주소는 `config/`에서 환경별로 주입 가능하게 한다.

---

## 7) 로깅/분석(Analytics) 규약

### 7.1 이벤트 네이밍
- snake_case 사용
- prefix로 feature 구분
    - 예: `home_viewed`
    - 예: `strategy_detail_viewed`
    - 예: `pro_paywall_opened`
    - 예: `support_email_opened`

### 7.2 파라미터 규칙
- 파라미터 키도 snake_case
- PII(개인식별정보) 금지
- 필수 파라미터 최소:
    - `screen_name`
    - `app_version`
    - `platform` (`android`/`ios`)

### 7.3 구현 원칙
- shared에는 `AnalyticsTracker` 인터페이스만 둔다.
- Android/iOS가 Firebase 등 실제 SDK 구현체를 제공한다(DI).

---

## 8) 환경 분리(Dev/Prod) 규약

### 8.1 환경 값 주입
- `baseUrl`, `isDebugBuild`, `policyUrl`, `termsUrl`, `privacyUrl` 등은
    - 앱 레벨(Android/iOS)에서 주입
    - shared는 `AppConfiguration` 데이터로만 받는다.

### 8.2 민감정보
- API Key/광고 Unit ID/결제 Product ID는:
    - Git에 커밋 금지
    - CI secret 또는 로컬 ignored 파일
    - 스토어용과 개발용 분리

### 8.3 MVP 더미 데이터 규칙 (서버 미구현)
- `Ktor HttpClient`는 실제로 구성한다.
- 서버 API 미구현 동안 `data/remote`에서 네트워크 지연만 시뮬레이션하고 더미 응답을 반환한다.
- 홈/전략 상세/전략 캘린더는 동일한 더미 전략 데이터 계열을 사용해 화면 간 일관성을 유지한다.

---

## 9) 네이티브 브리지 규약 (Kotlin 가능 vs 순수 네이티브)

### 9.1 Kotlin으로 구현 가능하면
- shared에 인터페이스 정의
- 각 플랫폼 구현체 제공
- shared DI에 바인딩

예:
- `DeviceInfoProvider`
- `TimeProvider`
- `ExternalLinkOpener`

### 9.2 순수 네이티브가 필요하면
- shared에서 콜백/이벤트 타입만 정의
- 앱 레벨에서 네이티브 구현 후 주입

예:
- `openSupportEmail`
- `startPurchase`
- `showBannerAd`

표준 원칙:
- “기능 구현은 플랫폼”
- “호출 트리거/정책/상태 모델링은 shared”

---

## 10) 버전/릴리즈 규약(간단)

### 10.1 버전 표기
- 앱 버전: `major.minor.patch` (예: 1.0.0)
- shared 모듈 버전과 앱 버전은 동일하게 유지(초기엔 단순함 우선)

### 10.2 릴리즈 체크리스트(1.0.0)
- 광고 정책 준수(하단 배너 only)
- Pro 결제 동작(구매/복원/상태 확인)
- 약관/개인정보 링크 동작
- 문의 이메일 템플릿 동작
- 크래시 로깅/기본 분석 이벤트 수집 확인
