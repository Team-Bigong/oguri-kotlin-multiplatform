# MOBILE_CONVENTIONS

## 아키텍처
- 의존성 방향은 `data -> domain <- ui`를 사용한다.
- `ui`는 `domain`만 참조한다.
- `data`는 `domain` 구현체를 제공한다.
- `domain`은 어떤 레이어도 import하지 않는다.

## 데이터/네트워크
- 서버 미구현 상태에서는 `Ktor` 기반 네트워크 호출 형태를 유지하고 더미 응답을 반환한다.
- 더미 데이터는 `data/remote`에서 관리한다.
- 요청/응답 모델은 각각 `data/remote/model/request`, `data/remote/model/response` 패키지에 둔다.
- UI 모델이 아닌 도메인 모델을 `domain/model`에 둔다.
- CRUD(수정/삭제) 요청도 서버 호출 형태를 유지하고 실패 시 로컬 더미 상태를 갱신해 화면 흐름을 보장한다.
- 스웨거 명세가 확정된 엔드포인트(`home`, `calendar`, `member`)는 더미 fallback 없이 실제 API 응답만 사용한다.
- 인증 토큰은 공용 스토어에서 관리하고, 네트워크 계층에서 `Authorization` 헤더를 주입한다.
- 헤더 주입 정책(`Authorization`/`X-USER-ID`)은 UI 레이어가 아닌 Ktor `ClientPlugin`에서 전역으로 관리한다.
- 토큰이 없으면 모든 API 요청에 `X-USER-ID: GUEST`를 자동 주입한다.
- `401` 또는 `403` 응답이 발생하면 토큰 재발급 후 요청을 1회 재시도한다.
- 재발급 동시성은 `Mutex`로 보호해 중복 재발급을 방지한다.

## 로그인
- 카카오 로그인 키는 `local.properties`의 `kakao.key`를 사용한다.
- Android는 Kakao SDK 기반 네이티브 로그인(`loginWithKakaoTalk` 우선, 실패 시 `loginWithKakaoAccount`)을 사용한다.
- Android Manifest에 `AuthCodeHandlerActivity` 리다이렉트 스킴(`kakao{NATIVE_APP_KEY}://oauth`)을 등록한다.
- 카카오 SDK 액세스 토큰으로 서버 `POST /api/v1/auth/login/kakao`를 호출해 서비스 토큰(`accessToken`, `refreshToken`)을 발급받는다.
- 서비스 토큰 재발급은 `POST /api/v1/auth/refresh`를 사용한다.
- 로그아웃 시 저장된 서비스 토큰(메모리/영속 저장소)을 모두 삭제한다.
- 로그인 성공 후 최초 진입은 `Home`가 아니라 `Onboarding`으로 라우팅한다.

## 온보딩
- 현재 온보딩은 서버 API 연동 전 단계이므로 UI 상태 기반 더미 플로우로 구현한다.
- 약관 Row 본문 클릭은 체크 토글, 오른쪽 화살표 클릭은 인앱 `WebDocument` 라우트 진입으로 분리한다.
- "모두 동의하고 다음으로" 클릭 시 필수 약관 상태를 모두 체크 처리한 뒤 연차 입력 단계로 이동한다.
- 연차 입력 검증은 도메인 UseCase로 관리한다.
- 남은 연차는 `1..40`, 선호 연차는 `1..남은 연차` 범위를 강제한다.
- 연차 입력 단계 전환은 타이핑 즉시가 아니라 `IME 완료` 또는 `포커스 이탈(키보드 바깥 터치)` 시점에만 확정한다.

## DI
- DI는 Metro를 사용한다.
- 루트에서 UseCase를 직접 실행하지 않는다.
- ViewModel 생성자에 UseCase를 주입한다.
- `core.di`를 중심으로 그래프를 구성한다.
- ViewModel 코루틴 스코프는 직접 생성하지 않고 `androidx.lifecycle.viewModelScope`를 사용한다.

## 내비게이션
- 타입 세이프 라우팅(`@Serializable`)을 사용한다.
- `app.navigation` 역할은 `core.navigation`에 둔다.
- 외부 공유 진입은 앱 딥링크(`oguri://open/...`)로 처리하고, 딥링크 파싱은 `core.deeplink`에서 단일 책임으로 관리한다.
- 딥링크 저장소는 `object`가 아닌 DI로 제공되는 싱글톤 인스턴스를 사용한다.
- 딥링크 진입은 로그인 여부와 무관하게 허용한다. 토큰이 없으면 `GUEST`로 API를 호출한다.
- 외부 문서(건의하기/약관/개인정보)는 `WebDocument` 라우트로 진입해 인앱 WebView로 표시한다.
- 메인 탭 이동 시 백스택은 초기화한다.
- 메인 탭에서는 뒤로가기/스와이프 백 제스처를 무시하고, 2회 뒤로가기로 종료한다.
- 메인 탭이 아닌 화면에서는 뒤로가기/스와이프 백으로 이전 스택으로 이동한다.

## UI 분리 기준
- Screen 파일은 상태 분기와 레이아웃 조합만 담당한다.
- 카드/섹션/토글/버튼 등 반복 가능한 UI는 `ui/component`로 분리한다.
- Screen과 각 컴포넌트는 프리뷰를 반드시 제공한다.
- 로딩은 화면별 스켈레톤으로 처리하고, 공용 Shimmer 컴포넌트(`SkeletonBox`)를 재사용한다.

## 광고 정책
- 홈/상세/캘린더에 하단 배너 1개만 노출한다.
- 스플래시/로그인/온보딩/마이페이지에는 광고를 노출하지 않는다.
- 스크롤 중간 광고는 금지한다.

## 로컬라이제이션
- 로컬라이즈 대상 텍스트는 `const val`이 아닌 문자열 리소스로 분리한다.
- 한국어/영어 확장을 기본 전제로 키를 설계한다.
