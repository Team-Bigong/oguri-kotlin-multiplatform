# MOBILE_CONVENTIONS

## 아키텍처
- 의존성 방향은 `data -> domain <- feature/ui`를 지킨다.
- `feature/ui`는 `domain`과 `core`를 참조할 수 있지만 `data`를 직접 참조하지 않는다.
- `data`는 `domain`의 repository abstraction을 구현하고, `ui`/`feature`를 import하지 않는다.
- `domain`은 어떤 앱 레이어도 import하지 않는다.
- `core`는 여러 feature가 공유하는 navigation, design system, platform bridge, network, ad, analytics, deeplink, 공용 UI만 둔다.
- `app`은 `OguriApp` 진입점만 유지하고 실제 화면 조합은 `core.navigation.NavDisplay`로 위임한다.

## 데이터/네트워크
- 네트워크는 Ktor client 기반으로 구현한다.
- `BASE_URL`은 `debug.base.url`/`release.base.url`로 생성된 config에서 가져온다.
- 요청/응답 모델은 `data/remote/model/request`, `data/remote/model/response`에 둔다.
- UI 모델이 아닌 도메인 모델은 `domain/model`에 둔다.
- repository 구현체는 remote/local DTO를 domain model로 변환해서 반환한다.
- 앱은 인증, 홈, 캘린더, 여행지, 회원, 검색 API와 여행 API(`/api/v1/trips`, `/api/v1/trip-invites`, `/api/v1/travel-places`)를 사용한다.
- 인증 토큰은 `AuthTokenStore`에서 access/refresh token 쌍으로 관리하고 플랫폼별 local data source에 영속화한다.
- 전역 header 정책은 `core.network.OguriRequestHeadersPlugin`에서 관리한다.
- `/api/v1/auth/**` 요청에는 `Authorization`과 `X-USER-ID`를 제거한다.
- 토큰이 없으면 일반 API 요청에 `X-USER-ID: GUEST`를 주입한다.
- 토큰이 있으면 `Authorization: Bearer {accessToken}`을 주입하고 `X-USER-ID`는 제거한다.
- 일반 경로의 `AuthRequestExecutor.execute`는 `401` 또는 `403`에서 토큰을 갱신하고 같은 세션에서 요청을 한 번 재시도한다. 여행의 회원 전용 요청은 `executeForSession`에서 시작·갱신·응답 시 계정 세션을 확인하며, `401`에만 갱신을 시도한다.
- 재발급 동시성은 `AuthTokenStore.refreshTokens`의 `Mutex`로 보호한다.
- refresh token 인증 실패 시 저장된 토큰을 삭제한다.

## 로그인
- 카카오 Native App Key는 `local.properties`의 `kakao.key`를 사용한다.
- Google Web Client ID는 `local.properties`의 `google.web.client.id`를 사용한다.
- Android 카카오 로그인은 Kakao SDK의 `loginWithKakaoTalk`을 우선 사용하고, 실패 시 `loginWithKakaoAccount`로 fallback 한다.
- Android Google 로그인은 Credential Manager + Google ID를 사용한다. 승인된 계정, 전체 계정, Sign in with Google 순서로 재시도한다.
- Android Apple 로그인 actual은 현재 미지원 실패를 반환한다.
- iOS Apple 로그인은 AuthenticationServices의 Apple ID credential에서 identity token을 추출한다.
- iOS Kakao 로그인은 KakaoTalk OAuth URL을 우선 열고, 실패 시 `ASWebAuthenticationSession`으로 authorization code를 받은 뒤 Kakao token endpoint에서 access token을 발급받는다.
- iOS Google 로그인 actual은 현재 미지원 실패를 반환한다.
- Android Manifest에는 앱 딥링크(`oguri://open`), KakaoLink, Kakao OAuth, `www.oguri.kr/trip-invite`의 App Links를 등록한다. iOS는 `applinks:www.oguri.kr` Associated Domains와 앱 스킴을 사용한다.
- 서버 로그인 엔드포인트는 `POST /api/v1/auth/login/kakao`, `POST /api/v1/auth/login/google`, `POST /api/v1/auth/login/apple`이다.
- 서버 로그인 응답의 `onboardingCompleted` 값에 따라 `Home` 또는 `Onboarding`으로 이동한다.
- "로그인 없이 둘러보기"는 `Home`으로 이동하고, 이후 API는 guest header로 호출한다.
- 로그아웃 또는 회원 탈퇴 완료 시 토큰을 모두 삭제하고 graph/session state를 갱신한다.

## 자동 로그인/스플래시
- Splash는 `GetAutoLoginStateUseCase`로 로컬 토큰과 `/api/v1/members/me` 상태를 확인한다.
- 자동 로그인 확인은 5초 timeout을 둔다.
- 토큰이 없거나 확인 실패 시 `Login`으로 이동한다.
- 로그인 상태이고 온보딩 완료면 `Home`, 미완료면 `Onboarding`으로 이동한다.

## 온보딩
- 온보딩 완료는 서버 `POST /api/v1/members/onboarding`에 남은 연차와 선호 연차를 저장한다.
- 약관 Row 본문 클릭은 체크 토글, 오른쪽 화살표 클릭은 인앱 `WebDocument` 라우트 진입으로 분리한다.
- "모두 동의하고 다음으로" 클릭 시 필수 약관을 모두 체크한 뒤 연차 입력 단계로 이동한다.
- 연차 입력 검증은 `ValidateOnboardingLeaveDaysUseCase`로 관리한다.
- 남은 연차는 `1..40`, 선호 연차는 `1..남은 연차` 범위를 강제한다.
- 건너뛰기 기본값은 남은 연차 `17`, 선호 연차 `2`이다.
- 남은 연차와 선호 연차 입력란은 빈 값으로 시작하며, 각각 `15`와 `2`를 힌트로 표시한다. 유효한 남은 연차를 입력하면 선호 연차 입력란이 나타난다. 두 값이 유효하면 키보드의 완료나 포커스 이동 없이 완료 버튼을 표시한다.

## DI
- DI는 Metro를 사용한다.
- `core.di.AppGraph`가 remote data source, repository, ViewModel provider, deep link store를 제공한다.
- `NavDisplay`에서 토큰 저장소 초기화, Ktor client 생성, Metro graph 생성을 수행한다.
- 루트에서 UseCase를 직접 실행하지 않는다.
- ViewModel 생성자에 UseCase를 주입한다.
- ViewModel coroutine scope는 직접 만들지 않고 `androidx.lifecycle.viewModelScope`를 사용한다.

## 내비게이션
- 타입 세이프 라우팅(`@Serializable RouteModel`)을 사용한다.
- [RouteModel](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/navigation/RouteModels.kt)은 일반 화면과 `TravelPlans`, `CreateTravelPlan`, `TravelPlan`, `TravelMembers`, `TravelInvite`, `TravelLogin`, `TravelOnboarding`을 정의한다.
- 하단 탭은 `Home`, `Calendar`, `TravelPlans`, `MyPage` 순서다. 여행 생성·상세·멤버 화면에서는 하단 탭을 숨긴다.
- 메인 탭 이동은 `popUpTo(navGraph.id)`로 백스택을 초기화한다.
- 홈·캘린더·마이페이지는 탭 재선택 시 상단으로 스크롤한다.
- 메인 탭 루트와 루트 Login에서는 2회 뒤로가기로 종료한다.
- 상세 화면은 뒤로 가기로 이전 화면을 연다. 여행 화면에는 공통 스와이프 백을 적용하지 않는다.
- 외부 공유/딥링크 진입은 `oguri://open/place?placeId=...`, `oguri://open/period?startDate=...&endDate=...`를 지원한다.
- KakaoLink 진입은 `kakao{key}://kakaolink?deeplink={encodedOguriUrl}` 형태를 파싱한다.
- 공개 도시·기간 공유는 비회원도 열 수 있다. 여행 탭의 비회원에게는 로그인 안내를 표시한다. 링크의 여행 초대는 미리보기를 열고, 새 참여에는 로그인과 명시적 확인이 필요하다. 기존 멤버는 여행 상세로 바로 이동한다.
- 추천에서 시작한 생성과 초대는 `TravelLogin`·`TravelOnboarding`으로 인증 흐름을 거쳐 원래 목적지로 돌아온다. 초대 토큰은 라우트 인자에 넣지 않는다. [여행 구현 가이드](../plan/TRAVEL_PLANNER_IMPLEMENTATION.md)
- 외부 문서(건의하기/약관/개인정보)는 `WebDocument` route와 `PlatformWebView`로 표시한다.

## UI 분리 기준
- `Route`는 상태 수집, side effect, navigation callback 연결만 담당한다.
- `Screen`은 상태 분기와 레이아웃 조합을 담당한다.
- 카드/섹션/토글/버튼 등 반복 가능한 UI는 `ui/component` 또는 `core/ui/component`로 분리한다.
- Screen과 각 컴포넌트는 가능한 한 프리뷰를 제공한다.
- `Route`에서 `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다.
- 화면 전용 one-shot effect는 `ui/model/*SideEffect.kt`에 둔다.
- 네트워크 에러 재시도 UI는 `NetworkErrorRetryContent`를 사용한다.
- 네트워크 초기 로딩은 화면 구조와 유사한 skeleton UI를 우선한다.
- Skeleton animation은 `SkeletonBox` 기반 Shimmer를 사용한다.
- 로그인/제출/탈퇴 같은 짧은 blocking overlay는 `CenteredLoadingIndicator`를 사용할 수 있다.
- 삭제/로그아웃 확인 팝업은 `ConfirmAlertDialog`를 우선 사용한다.
- 스낵바는 `NavDisplay`의 단일 `SnackbarHostState`와 `OguriSnackBarHost`로 관리한다.
- 스낵바 표시는 `showOguriSnackbar(...)`를 사용하고 타입(`SUCCESS`, `ALERT`, `INFO`)을 명시한다.

## 광고 정책
- 서버 응답 기반 광고 카드는 `AdvertisementCard`로 표시한다.
- Home은 추천 기간의 `advertisements`를 `AdvertisementCard` 목록으로 표시한다.
- Place Detail은 항공권 `flightUrl`을 Skyscanner `AdvertisementCard`로 표시한다.
- AdMob은 플랫폼별 actual bridge에서 관리한다. Android는 Google Mobile Ads SDK를 직접 사용하고, iOS는 native host가 처리할 수 있도록 notification bridge를 사용한다.
- 홈에서 다른 메인 탭으로 이동하거나 여행지·기간 상세를 열 때 앱 오프닝 광고를 프로세스당 최대 한 번 시도한다. 첫 전환에 광고가 준비되지 않았으면 바로 이동한다. 백그라운드 복귀와 딥링크에는 광고를 띄우지 않는다.
- 앱 오프닝 광고는 디버그 빌드에서 Google 테스트 광고 단위를, 릴리즈 빌드에서 각 플랫폼에 등록된 오구리의 앱 오프닝 광고 단위를 사용한다.
- Calendar는 추천 카드 index `3` 뒤에 첫 inline banner를 노출하고 이후 12개 카드 간격으로 노출한다.
- Photo Detail은 하단 AdMob banner를 노출한다.
- 마이페이지와 여행 탭은 하단 AdMob 배너를 사용한다. 여행 탭은 비회원 안내 또는 목록 조회에 성공한 화면에서 배너를 표시한다.

## 분석/공유
- Analytics는 `core.analytics` expect/actual bridge를 사용하고, `amplitude.api.key`가 비어 있으면 초기화하지 않는다.
- Android와 iOS의 Debug 구성은 Amplitude를 초기화하지 않는다. 배포 구성은 자동 수집 없이 명시적으로 기록한 이벤트를 보낸다. Android 초기화 코드는 Session Replay 플러그인을 등록하지 않는다.
- iOS는 Swift 호스트에 초기화·이벤트 요청을 전달한다. 여행 생성·편집·재사용 지표는 [서버의 별도 집계](../plan/TRAVEL_PLANNER_METRICS.md)를 사용한다.
- 사용자 행동 event name/property는 `core.analytics.OguriAnalyticsEvent.kt`에 둔다.
- 공유는 `core.platform.ShareProvider`를 통해 처리한다.
- Android 공유는 Kakao share intent를 우선 준비하고, 실패하거나 사용할 수 없으면 system share text로 fallback 한다.

## 로컬라이제이션
- 로컬라이즈 대상 텍스트는 Kotlin `const val`이 아닌 `composeResources` 문자열 리소스로 분리한다.
- 현재 지원 리소스 폴더는 기본 영어(`values`), 한국어(`values-ko`), 일본어(`values-ja`), 중국어(`values-zh`)이다.
- 새 문자열 키는 언어별 파일에 모두 추가한다.
