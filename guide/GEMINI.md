# Oguri Server - Development Guide

> 이 문서는 Oguri 서버 모듈에서 AI가 코드를 제안하거나 수정할 때 반드시 따를 기준을 정의한다.

> Kotlin + Spring Boot 기반 서버 코드는 "동작하는 코드"가 아니라 **운영 가능한 코드**여야 한다.

------------------------------------------------------------------------

## 1. 기본 철학

- 요청된 문제만 해결한다.
- 수정은 최소 범위로 한다.
- 기존 구조를 존중한다.
- 불필요한 추상화, 리팩터링, 구조 변경은 하지 않는다.
- 설계 변경이 필요하면 먼저 이유와 영향 범위를 설명한다.

코드는 우아하되 과시적이면 안 된다.

------------------------------------------------------------------------

## 2. 현재 기술 스택

- Kotlin
- Spring Boot
- Gradle Kotlin DSL
- Java 17 toolchain
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- springdoc-openapi
- Spring Boot Actuator와 Micrometer
- JWT(`jjwt`)
- Google/Apple/Kakao 인증 연동
- `shared` Gradle 모듈 의존

------------------------------------------------------------------------

## 3. 서버 아키텍처

단순한 Layered Architecture를 유지한다.

```text
Controller -> Service -> Repository -> Domain(Entity)
```

- Controller: 요청/응답 매핑, HTTP 파라미터/바디 수신
- Service: 비즈니스 로직, 트랜잭션, 검증, DTO 조립
- Repository: 데이터 접근 전용
- Domain: JPA Entity와 Entity가 스스로 수행해야 할 최소 도메인 행동
- DTO: API request/response 모델
- Config: interceptor, swagger, CORS, auth context, HTTP client 설정
- Util: JWT, 외부 identity token verifier 등 기술 유틸리티

비즈니스 로직은 Repository에 두지 않는다.

------------------------------------------------------------------------

## 4. 코드 수정 원칙

### 4.1 최소 수정 원칙

- 전체 파일 재작성 금지
- 기존 로직 보존
- 필요한 부분만 정확히 수정
- import 정리 등 불필요한 변경 금지
- 요청 없는 함수 시그니처 변경 금지
- 요청 없는 의존성 추가 금지

문제 해결이 목적이지 구조 개선이 목적이 아니다.

### 4.2 안정성과 운영 고려

- Null 안전성을 고려한다.
- 예외 처리를 누락하지 않는다.
- 트랜잭션은 Service 계층에서 관리한다.
- 로그는 의미 있게 남긴다.
- 테스트를 깨뜨리지 않는다.
- 성능 영향이 있으면 반드시 명시한다.
- N+1 가능성을 항상 고려한다.

------------------------------------------------------------------------

## 5. 응답 스타일

- 모든 설명은 한국어로 작성한다.
- 불확실하면 추측하지 않는다.
- 모르면 모른다고 말한다.
- 설계 변경이 필요하면 먼저 질문하거나 선택지를 제안한다.
- 대안이 있으면 장단점을 설명한다.

------------------------------------------------------------------------

## 6. 금지 사항

- 과도한 리팩터링
- 전역 설정 변경
- 빌드 설정 대규모 수정
- 요청 없는 의존성 추가
- 불필요한 디자인 패턴 도입
- 헥사고날/DDD 풀구조 전환
- 멀티 모듈 구조 변경
- feature 단위 서버 패키지 재구성
- Entity 직접 반환
- 이미 적용된 Flyway migration 수정

------------------------------------------------------------------------

## 7. 코드 스타일

- 명확한 변수명을 사용한다. 축약어를 지양한다.
- Early return을 선호한다.
- 불필요한 nullable을 지양한다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- 확장 함수를 남용하지 않는다.
- 단순 변환은 현재 구조에 맞춰 Service 내부 private 함수로 처리한다. 요청 없이 Mapper 패키지를 분리하지 않는다.
- HTTP 상태 코드는 `ResponseStatusException` 등 기존 방식에 맞춰 명확히 구분한다.

------------------------------------------------------------------------

## 8. DB 원칙

- 트랜잭션은 Service에서 관리한다.
- Repository는 비즈니스 로직을 가지지 않는다.
- Flyway를 사용한다.
- 이미 배포/적용된 migration은 수정하지 않고 새 migration을 추가한다.
- JPA `open-in-view`는 false이다. Lazy loading 범위와 fetch 전략을 명확히 고려한다.
- N+1이 예상되면 repository query 또는 service 조립 방식을 근거와 함께 조정한다.

------------------------------------------------------------------------

## 9. 현재 프로젝트 컨텍스트

Oguri 서버는 모바일 앱과 웹 어드민의 JSON API를 제공한다. 여행 초대 웹 안내는 [별도의 정적 페이지](../web/trip-invite/index.html)에서 제공한다.

### 핵심 패키지

```text
server/src/main/kotlin/com/bigong/oguri
├─ Application.kt
├─ config
├─ controller
├─ domain
├─ dto
│  ├─ request
│  └─ response
├─ repository
├─ service
└─ util
```

현재 실제 base package는 `com.bigong.oguri`이다.

### 핵심 도메인

#### Destination

- `country`
- `name`
- `summary`
- `description`
- `recommendStartMonth1`, `recommendEndMonth1`
- `recommendStartMonth2`, `recommendEndMonth2`
- `flightTimeMinutes`
- `flightUrl`
- `weatherTemp1`, `weatherPrecipitationMm1`
- `weatherTemp2`, `weatherPrecipitationMm2`
- `images`

추천 기간은 문자열이 아니라 시작/종료 월 정수 컬럼 쌍으로 관리한다.

#### DestinationImage

- `destination`
- `imageUrl`
- `isThumbnail`
- `sortOrder`

이미지 URL을 문자열로 합쳐 저장하지 않는다. 별도 테이블로 정규화한다.

#### DestinationExperience

- `destinationId`
- `title`
- `description`
- `thumbnailUrl`
- `link`
- `sortOrder`

장소별 액티비티/체험 카드는 destination과 별도 테이블로 관리한다.

#### Country

- `name`
- `currencyCode`
- `bigMacIndex`

국가별 통화 코드와 빅맥 지수는 국가 엔티티에서 관리한다.

#### Member

- `id`
- `nickname`
- `refreshToken`
- `preferredDayOff`
- `remainingDayOff`
- `onboardingCompleted`
- `updatedAt`

`updateDayOffInfo`, `completeOnboarding`, `setNicknameOnce`, `updateRefreshToken`처럼 멤버가 스스로 수행해야 하는 상태 변경은 Entity method로 둔다.

#### SavedRecommendation / SavedDestination / SearchLog / PublicHoliday

- 저장 일정, 저장 여행지, 검색 로그, 공휴일은 별도 엔티티로 관리한다.
- guest 요청은 인증 context에서 `GUEST` member id로 해석될 수 있다.

#### Adjective / Noun

- 랜덤 닉네임 생성을 위한 단어 테이블이다.
- 닉네임 형식은 `형용사 명사_숫자5자리`이다.

#### 여행 플래너

- `TravelPlan`이 여행 기간·시간대·정렬된 도시와 방문 변경 규칙을 관리한다. `TravelPlanMembership`이 멤버십을, `TravelVisit`가 일차·순서·시각·메모·장소를 나타낸다.
- `TravelPlanService`와 `TravelCollaborationService`가 회원·여행 권한을 확인하고 행 잠금 안에서 변경과 중복 방지 기록을 함께 처리한다.
- `TravelCatalogCity`와 `city_aliases`가 GeoNames 도시 검색을 담당한다. 여행의 방문 장소 검색과 상세 조회는 Google Places를 사용한다.
- 계정 삭제와 관리자 삭제는 여행 소유권·멤버십 정리를 거친다. [여행 구현 가이드](../plan/TRAVEL_PLANNER_IMPLEMENTATION.md)

------------------------------------------------------------------------

## 10. 현재 API 구조

### Public/Auth API

- `POST /api/v1/auth/login/kakao`
- `POST /api/v1/auth/login/apple`
- `POST /api/v1/auth/login/google`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/login/mock`

### Home API

- `GET /api/v1/home`
- `GET /api/v1/home/weekly-top`
- `GET /api/v1/home/monthly-top-periods`
- `POST /api/v1/home/search-logs`
- `GET /api/v1/home/trending-searches`
- `GET /api/v1/home/search/autocomplete`

### Calendar/Destination/Member API

- `GET /api/v1/calendar`
- `GET /api/v1/calendar/detail`
- `GET /api/v1/destinations/{id}`
- `GET /api/v1/members/me`
- `POST /api/v1/members/day-off`
- `POST /api/v1/members/onboarding`
- `POST /api/v1/members/saved-recommendations`
- `DELETE /api/v1/members/saved-recommendations`
- `POST /api/v1/members/saved-destinations/{id}`
- `DELETE /api/v1/members/saved-destinations/{id}`
- `DELETE /api/v1/members/me`

### 여행 API

- `/api/v1/trips`는 생성·목록·상세, 홈 이어보기, 방문 변경, 멤버·최근 변경과 장소 검색을 제공한다.
- `GET /api/v1/trips/{tripId}?knownRevision=…`는 현재 권한을 다시 확인한 뒤 변경이 없으면 `204 No Content`를 반환한다. 최초 조회와 변경된 상태에는 `200`으로 일정을 반환한다.
- `/api/v1/trip-invites/{preview,join}`은 초대 토큰을 요청 본문에서 받아 각각 미리보기·참여를 처리한다. 미리보기는 비회원도 가능하며, 참여는 회원 전용이다.
- `/api/v1/travel-places/cities`는 도시 검색과 시간대 확인을 제공한다. 전체 API와 기존 알림 경로는 [구현 가이드](../plan/TRAVEL_PLANNER_IMPLEMENTATION.md)에 있다.

### Admin API

- Prefix: `/api/admin/v1`
- Admin controller는 `@Hidden`으로 Swagger에서 숨긴다.
- 인증은 `POST /api/admin/v1/auth/login` 후 `Authorization: Bearer {token}`을 사용한다.
- 장소/국가: `GET/POST/PUT/DELETE /countries`, `GET/POST/PUT/DELETE /destinations`
- 사용자: `GET/POST/PUT/DELETE /members`
- 공휴일: `GET/POST/PUT/DELETE /public-holidays`

------------------------------------------------------------------------

## 11. 인증/인터셉터

- `AuthInterceptor`는 `/api/**`에 적용하되 `/api/admin/**`, 카카오/애플 로그인, refresh 등 일부 경로는 제외한다.
- Authorization header가 없으면 guest member id로 처리한다.
- 보호된 member 저장/삭제/온보딩/회원 정보 엔드포인트는 guest 접근 시 `401`을 반환한다.
- `AdminAuthInterceptor`는 `/api/admin/**`에 적용하고 admin login만 예외로 둔다.
- CORS는 모든 출처·헤더에서 `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`를 허용한다. [WebConfig.kt](../server/src/main/kotlin/com/bigong/oguri/config/WebConfig.kt)
- 운영 설정과 PostgreSQL 검증은 [여행 검증 가이드](../plan/TRAVEL_PLANNER_TESTING.md)를 따른다.

------------------------------------------------------------------------

## 12. 최종 기준

코드는 "멋있게"가 아니라 **10년 후에도 읽힐 수 있게** 작성한다.

단순하지만 견고한 코드를 선호한다.
