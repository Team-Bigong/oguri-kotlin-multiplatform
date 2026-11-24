# 여행 플래너 구현 가이드

코드 대조 2026. 11. 22. (`develop-mobile`의 `fe856357`)

[통합 명세](./TRAVEL_PLANNER_SPEC.md)는 화면 동작과 인수 기준 AC01–AC22를 정한다. 이 가이드는 앱·서버·웹이 연결되는 위치와 현재 API·저장 계약을 설명한다. 테스트 실행과 운영 설정은 [검증 가이드](./TRAVEL_PLANNER_TESTING.md), 이전 구현 단계와 실행 결과는 [진행 기록](./TRAVEL_PLANNER_PROGRESS.md)에서 확인한다.

## 1. 코드 위치와 역할

| 위치 | 책임 |
| --- | --- |
| [모바일 domain](../composeApp/src/commonMain/kotlin/com/bigong/oguri/domain) | `TravelPlan`, `TravelDates`, `TravelVisit`와 여행 조회·편집·장소·협업·복구 인터페이스 |
| [모바일 data](../composeApp/src/commonMain/kotlin/com/bigong/oguri/data) | `KtorTravelPlanRemoteDataSource`, DTO와 `DefaultTravelPlanRepository`의 변환, 계정 범위의 `DefaultTravelRecoveryRepository` |
| [여행 화면](../composeApp/src/commonMain/kotlin/com/bigong/oguri/feature/travelplanner/ui) | 목록·생성·상세·초대·관리의 Route·Screen·ViewModel, 상태 모델, 공용 여행 컴포넌트 |
| [AppGraph.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/di/AppGraph.kt), [내비게이션](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/navigation), [딥링크](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/deeplink) | Metro 의존성 주입, 여행 탭과 상세 경로, 인증 복귀, 공유 링크와 초대 의도 |
| [서버 컨트롤러](../server/src/main/kotlin/com/bigong/oguri/controller), [서비스](../server/src/main/kotlin/com/bigong/oguri/service), [도메인](../server/src/main/kotlin/com/bigong/oguri/domain) | 컨트롤러는 HTTP 매핑과 인증 주체 전달, 서비스는 권한·트랜잭션·응답 조립, `TravelPlan`은 일차·순서·방문 변경 규칙을 맡는다. |
| [Flyway](../server/src/main/resources/db/migration), [여행 초대 웹](../web/trip-invite/index.html) | DB 스키마와 기존 데이터의 호환성, 앱 열기·설치 안내와 도메인 연결 파일 |

모바일의 의존성은 `data → domain ← feature/ui`다. UI는 도메인 인터페이스를 사용하며 `AppGraph`가 구현을 주입한다. [Android 공식 도메인 가이드](https://developer.android.com/topic/architecture/domain-layer)는 선택적 도메인 계층이 데이터 계층의 저장소에 의존하는 구조를 설명한다. 오구리는 저장소 인터페이스를 도메인에 두고 데이터 계층에서 구현한다. 단방향 데이터 흐름과 생명주기 기반 수집을 공통 원칙으로 적용한다. [모바일 구조](../guide/MOBILE_PROJECT_STRUCTURE.md) · [컨벤션](../guide/MOBILE_CONVENTIONS.md) · [코드 스타일](../guide/MOBILE_CODE_STYLE.md)

서버는 [서버 가이드](../guide/GEMINI.md)의 Spring MVC·Service·Repository·JPA Entity 구조를 따른다. `TravelPlanService`와 `TravelCollaborationService`가 여행을 잠그고 권한과 기준 버전을 확인한다. 일차 이동과 독립 필드 병합은 `TravelPlan`의 행동으로 처리한다. HTTP 응답에는 DTO를 사용한다.

## 2. 여행 데이터와 저장 계약

| 데이터 | 현재 규칙 |
| --- | --- |
| 여행 | `travel_plans`에는 제목·기간·시간대·소유자·추천 출처가 있다. 기간은 당일을 포함해 최대 366일이다. 추천 출처의 도시 ID와 날짜는 실제 여행 날짜와 별도로 저장한다. |
| 도시 | V50의 `travel_plan_cities`는 도시를 최대 10개까지 순서대로 저장한다. 첫 도시가 기준 도시이며 기존 단일 도시 요청을 계속 읽는다. V52에서 GeoNames 카탈로그 참조를 추가했다. |
| 방문 | `travel_visits`는 방문 ID, 일차·정렬 위치, 선택 시각·메모·카테고리, 직접 입력 정보 또는 Google 장소 ID를 저장한다. 같은 장소의 재방문도 방문 ID가 다르다. 일차별 Google 장소 방문 한도는 30개다. |
| 버전 | 여행 전체 수정 번호는 조회와 변경 기록의 기준이다. 구조 버전은 기간·도시 변경, 방문 버전은 항목 변경, 일차 버전은 순서 충돌을 구별한다. |
| 협업 | `travel_plan_members`, `travel_plan_invites`, `travel_plan_changes` 등이 멤버십·초대·변경을 저장한다. 최근 변경에는 방문 ID와 종류를 남기고 조회 시 현재 방문 상태를 읽는다. |
| 호환성 | V49는 기존 미배정 방문과 마지막 열람 일차를 여행 마지막 날로 옮기고 `FLIGHT` 카테고리를 `TRANSPORT`로 변환한다. 새 방문은 유효한 일차에만 저장한다. |

실제 날짜는 `LocalDate`, 공유 방문 시각은 `LocalTime`, 변경·발송 시각은 `Instant`로 다룬다. 첫 도시의 시간대를 여행 전체의 기준으로 사용한다. 시작일을 옮겨도 상대적인 일차는 유지한다. 기간 단축을 확정하면 범위 밖 방문을 새 마지막 날의 기존 방문 뒤로 이동한다. 서버는 미리보기 뒤 계획이 바뀌었는지 다시 확인한다.

- 생성과 방문 변경의 중복 적용을 요청 ID와 내용으로 막는다. 응답을 확인하지 못했을 때는 같은 ID와 내용으로 다시 보낸다. 같은 ID로 내용이 달라지면 충돌로 처리한다.
- 추가는 해당 일차의 맨 뒤에 붙인다. 겹치지 않는 방문 필드는 기준값을 확인해 합치고, 같은 값이나 일차 순서의 충돌은 `409`로 처리한다. 삭제된 방문을 수정해도 방문이 다시 생기지 않는다.
- 방문 삭제·날짜 이동은 원래 명령을 참조해 되돌린다. 회원·여행·후속 변경을 확인하며, 성공하면 되돌리기 기록을 소모한다.
- 방문·변경 기록·중복 방지 기록·확정 사용 지표는 같은 트랜잭션에서 저장한다. 장소 공급자 조회와 푸시 전송은 여행 편집 트랜잭션 밖에서 수행한다.
- 소유자가 일반 회원 탈퇴를 요청하면 공동 여행의 소유권을 먼저 처리해야 한다. 관리자 삭제는 다른 참여자에게 소유권을 넘길 수 있으며, 혼자 보유한 여행은 제거한다.

## 3. 서버 API

아래 경로에는 `/api/v1` 접두사가 붙는다. 여행 조회·편집·도시 조회와 초대 참여는 회원을 확인한다. 초대 미리보기는 비회원도 사용할 수 있다. 여행 상세 및 초대 응답은 `Cache-Control: no-store`를 사용한다.

| 동작 | 경로와 계약 |
| --- | --- |
| 생성·목록·이어보기 | `POST /trips`, `GET /trips`, `GET /trips/resume` |
| 여행 상세 | `GET /trips/{tripId}`. `knownRevision`이 같으면 접근을 확인한 뒤 `204 No Content`, 최초 조회와 변경 상태는 `200`과 일정 응답 |
| 방문 변경 | `POST /trips/{tripId}/commands`. `TravelPlanCommand`의 추가·수정·삭제·일차 정렬·되돌리기 |
| 여행 정보·보기·활동 | `PUT /trips/{tripId}/details`, `PUT /trips/{tripId}/view`, `POST /trips/{tripId}/activity` |
| 최근 변경 | `GET /trips/{tripId}/changes`, `PUT /trips/{tripId}/changes/seen` |
| 멤버·소유권 | `GET /trips/{tripId}/members`, `DELETE /trips/{tripId}/members/{memberId}`, `DELETE /trips/{tripId}/membership`, `POST /trips/{tripId}/ownership`, `POST /trips/{tripId}/ownership/accept` |
| 삭제·초대 | `DELETE /trips/{tripId}`, `POST /trips/{tripId}/invites`, `DELETE /trips/{tripId}/invites`, `POST /trip-invites/preview`, `POST /trip-invites/join` |
| 도시 검색·시간대 | `POST /travel-places/cities/search`, `POST /travel-places/cities/time-zone` |
| 장소 검색·상세 | `POST /trips/{tripId}/places/search`, `GET /trips/{tripId}/days/{dayNumber}/places`, `POST /trips/{tripId}/places/details`. 이전 앱의 `GET /trips/{tripId}/places`도 유지한다. |
| 추천 진입 지표 | `POST /travel-recommendation-entries`. 비회원 추천에서도 진입 이벤트를 기록한다. 수집 범위는 [지표 가이드](./TRAVEL_PLANNER_METRICS.md)에 있다. |

여행 요청은 기존 액세스 토큰과 계정 세션을 사용한다. `AuthRequestExecutor.executeForSession`은 `401`에서만 토큰을 갱신해 같은 세션의 요청을 한 번 다시 보낸다. 여행 접근이 사라졌거나 계정이 바뀌면 이전 응답을 적용하지 않는다. 초대 미리보기는 로그인 상태가 포함되면 기존 멤버인지 확인한다.

## 4. 모바일 상태·복귀

- `Route`는 상태와 내비게이션을 연결하고, `Screen`은 전달받은 상태를 그린다. ViewModel의 상태는 `collectAsStateWithLifecycle()`로 수집한다. 데이터 계층이 Compose나 지도 SDK를 가져오지 않으며 목록·핀은 같은 방문 ID로 연결한다.
- 상세는 `RESUMED`에 진입하면 계획을 새로 확인하고, 활성화된 동안 5초 간격으로 수정 번호를 보낸다. `204`는 접근 확인 시각만 갱신한다. 변경된 계획을 받아도 작성 중인 입력은 유지한다.
- 도시·장소 검색은 새 입력이 들어오면 기존 요청을 취소한다. 목록에 남는 초안·충돌·저장 결과는 UI 상태로 처리한다. 사용자가 보낸 저장 요청은 취소 가능한 검색·조회와 분리한다.
- 보관소는 계정에 묶인 확정 일정(15분), 초안·저장 결과를 모르는 요청·미적용 순서(24시간)를 구분한다. Android는 Keystore 기반 암호화 파일을, iOS는 파일 보호가 적용된 캐시 파일을 사용한다. 오프라인에서는 남은 확정 일정과 메모를 읽을 수 있다. 재시도는 현재 권한과 같은 계정 세션을 확인한다.
- 접근 종료를 확인하면 화면에서 일정·초대 정보를 비우고 해당 여행 캐시와 초안을 정리한다. 로그인 세션이 바뀌면 이전 계정의 복구 데이터에 접근할 수 없다.
- 상세·초대 전용 로그인은 추천 도시·기간과 원래 내비게이션을 보존한다. 홈 이어보기는 별도 요약 응답을 사용하며 조회 실패의 영향은 해당 영역에 한정한다.

## 5. 지도·장소·초대 연동

- Android는 [Google Maps SDK actual](../composeApp/src/androidMain/kotlin/com/bigong/oguri/feature/travelplanner/ui/component/PlatformTravelMap.android.kt), iOS는 [Swift 지도 브리지](../iosApp/iosApp/Bridge/Maps/TravelMapBridge.swift)를 사용한다. 공통 지도 상태에는 핀·순서 점선·카메라용 좌표를 전달한다.
- 도시 검색은 서버의 GeoNames 카탈로그를 사용한다. Google Places는 선택 도시의 방문 장소 조회에 사용한다. 상세 배치는 최대 30개이며 여행에 포함된 장소 ID만 조회할 수 있다. 모바일은 현재 언어로 이미 조회한 정보는 재사용하고 일시적인 공급자 응답은 메모리에 보관한다.
- 장소 검색의 범위는 선택한 도시로 정한다. 카탈로그 좌표가 있으면 좌표를, 없는 도시에는 이름을 사용한다. 점선·거리 레이블은 직선거리를 표시하며 실제 길찾기는 Google Maps URL을 연다.
- 여행 링크는 `https://www.oguri.kr/trip-invite/#<토큰>`을 사용한다. 토큰은 URL 조각과 앱 내부 초대 의도에 보관하고, 서버에는 미리보기·참여 요청 본문으로 전달한다. Android App Links와 iOS Universal Links의 연결 파일은 웹 빌드가 생성한다. [웹 배포 안내](../web/README.md#여행-초대-링크-배포)
- 비회원과 새 멤버에게는 참여 전 미리보기를 제공한다. 기존 멤버는 확인된 여행 상세로 곧바로 이동한다. 한 링크의 기본 유효 기간은 7일이다. 소유자가 링크를 다시 발급하거나 여행에서 멤버가 나가거나 제외되면 기존 링크를 철회한다.

## 6. 출발 전 알림과 운영

현재 앱은 여행 알림 설정·예약·알림 진입을 제공하지 않는다. 서버의 `TravelReminderController`와 V39 기록은 기존 앱 호환을 위해 유지하며, `TRAVEL_REMINDERS_ENABLED`의 기본값은 `false`다. 기존 예약의 해제·발송 인계 검증과 쓰기·초대 중지 설정은 [검증 가이드](./TRAVEL_PLANNER_TESTING.md)에서 확인한다.

[Android CI](../.github/workflows/android-ci.yml)는 단위 테스트와 Debug APK, [iOS CI](../.github/workflows/ios-ci.yml)는 iOS 시뮬레이터 공통 테스트, [백엔드 CI](../.github/workflows/backend-ci.yml)는 PostgreSQL을 연결한 서버 테스트와 `bootJar`, [웹 CI](../.github/workflows/web-ci.yml)는 초대 테스트·빌드와 연결 파일을 검사한다. Swift 호스트·실기기·배포 응답의 검증 방법은 검증 가이드에 있다.

[진행 기록](./TRAVEL_PLANNER_PROGRESS.md)의 P0–P7은 연동 검증, 개인 저장, 장소·지도, 기존 화면 연결, 협업, 복구, 당시의 선택 알림, 통합 검증 순서의 작업 이력이다. 현재 변경을 검증할 때는 [통합 명세의 인수 기준](./TRAVEL_PLANNER_SPEC.md#152-인수-기준)에서 해당 사례를 골라 실행 조건과 결과를 남긴다.
