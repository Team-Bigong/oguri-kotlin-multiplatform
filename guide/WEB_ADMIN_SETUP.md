# WEB_ADMIN_SETUP

## 목적
- Vite + React + React Native Web 기반 Web에서 공식 웹(`/`)과 어드민 웹(`/admin`, `#/admin`)을 함께 운영한다.
- 어드민 웹은 장소/국가/사용자/공휴일 DB를 CRUD로 관리한다.
- 어드민은 서버 API와 Firebase Storage를 함께 사용한다.
- 공식 사이트와 어드민은 한 React 진입점에서 경로에 따라 분기한다. 여행 초대 안내는 별도의 정적 진입점이다.

## 현재 구조

```text
web/
├─ public/
│  ├─ app-ads.txt
│  └─ assets/official/
├─ src/
│  ├─ App.tsx
│  ├─ main.tsx
│  ├─ config/env.ts
│  ├─ features/
│  │  ├─ routing/pathResolver.ts
│  │  ├─ official/
│  │  └─ admin/
│  │     ├─ AdminApp.tsx
│  │     ├─ components/
│  │     ├─ hooks/
│  │     ├─ styles/
│  │     ├─ types/
│  │     └─ utils/
│  ├─ lib/
│  │  ├─ apiClient.ts
│  │  ├─ firebase.ts
│  │  └─ imageProcessing.ts
│  └─ types/admin.ts
├─ trip-invite/index.html
├─ package.json
├─ tsconfig.json
└─ vite.config.ts
```

## 라우팅
- `web/src/App.tsx`에서 공식 웹과 어드민 웹을 분기한다.
- `window.location.pathname.startsWith("/admin")` 또는 `window.location.hash.startsWith("#/admin")`이면 `AdminApp`을 표시한다.
- React 진입점의 나머지 경로는 `OfficialHomePage`를 표시한다. `/trip-invite/`는 [앱 초대 안내](../web/README.md#여행-초대-링크-배포) 빌드 결과를 제공한다.
- Render rewrite 설정 전 직접 접근 fallback으로 `/#/admin`을 사용할 수 있다.

## 어드민 인증
- 어드민 로그인 API는 `POST /api/admin/v1/auth/login`이다.
- 로그인 응답의 `accessToken`을 `sessionStorage`의 `oguri_admin_access_token`에 저장한다.
- 이후 어드민 API 요청에는 `Authorization: Bearer {token}`을 주입한다.
- `401`이 포함된 API 에러를 받으면 token을 지우고 로그인 화면으로 되돌린다.
- 서버 어드민 컨트롤러는 `@Hidden`으로 Swagger에 노출하지 않는다.

## 어드민 UI 레이아웃
- PC 최적화 대시보드형 UI를 사용한다.
- 넓은 레이아웃 기준은 `windowWidth >= 1360`이다.
- 주요 탭은 `destinations`, `countries`, `members`, `holidays`이다.
- 각 목록은 검색어 state로 필터링한다.
- 장소 폼은 국가 선택, Storage slug, 추천 기간 2개, 비행 시간/링크, 기간별 날씨, 이미지, 체험을 함께 관리한다.

## Firebase 이미지 정책
- 장소 이미지는 crop/압축 후 Firebase Storage에 업로드한다.
- 장소 이미지 출력 폭은 1280px이다.
- 체험 썸네일 출력 폭은 1200px이다.
- 압축 목표는 800KB 이하이고, 최소 품질은 0.45, 최대 품질은 0.92이다.
- 장소 이미지 경로는 `places/{countrySlug}/{citySlug}/{index}.jpg`이다.
- 체험 썸네일 경로는 `places/{countrySlug}/{citySlug}/experiences/{index}.jpg`이다.
- `{countrySlug}`, `{citySlug}`는 `^[a-z0-9]+(?:-[a-z0-9]+)*$`만 허용한다.
- Storage slug 추천 목록은 하드코딩하지 않고 DB에 저장된 장소 이미지/체험 썸네일 URL에서 추출한다.
- 새 파일명 번호는 현재 폼의 이미지/체험 URL에서 읽은 최대 번호 + 1을 사용한다.
- 삭제 대상 판정은 URL token이 아니라 Firebase object path 기준으로 처리한다.

## 이미지 정리 정책
- 장소 수정 시 폼에서 제거된 기존 장소 이미지와 체험 썸네일은 저장 성공 후 Firebase에서 삭제한다.
- 장소 삭제 시 해당 장소의 모든 장소 이미지와 체험 썸네일을 Firebase에서 삭제한다.
- 장소 편집 중 새로 업로드한 이미지는 폼 초기화 또는 다른 장소 불러오기 시 Firebase에서 정리한다.
- 새로 업로드한 이미지를 저장 전 교체/삭제하면 즉시 Firebase에서 삭제한다.
- Firebase 삭제 실패는 장소 저장/삭제 완료 메시지에 실패 건수로 표시한다.

## 서버 어드민 API
- Prefix: `/api/admin/v1`
- 인증: `POST /api/admin/v1/auth/login`으로 토큰 발급 후 `Authorization: Bearer {token}` 사용

### 인증
- `POST /api/admin/v1/auth/login`

### 국가
- `GET /api/admin/v1/countries`
- `POST /api/admin/v1/countries`
- `PUT /api/admin/v1/countries/{countryId}`
- `DELETE /api/admin/v1/countries/{countryId}`

국가 요청에는 `name`, `currencyCode`, `bigMacIndex`가 들어간다. 응답은 `id`를 함께 반환한다.

### 장소
- `GET /api/admin/v1/destinations`
- `POST /api/admin/v1/destinations`
- `PUT /api/admin/v1/destinations/{destinationId}`
- `DELETE /api/admin/v1/destinations/{destinationId}`

장소 요청/응답 주요 필드:
- `countryId`
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
- `experiences`

`images` 항목:
- `imageUrl`
- `isThumbnail`
- `sortOrder`

`experiences` 항목:
- `title`
- `description`
- `thumbnailUrl`
- `link`
- `sortOrder`

장소 응답에는 `id`와 `countryName`이 있으며, 이미지와 체험 항목에도 각각 `id`가 있다.

### 사용자
- `GET /api/admin/v1/members`
- `POST /api/admin/v1/members`
- `PUT /api/admin/v1/members/{memberId}`
- `DELETE /api/admin/v1/members/{memberId}`

생성·수정 요청은 `nickname`, `preferredDayOff`, `remainingDayOff`, `onboardingCompleted`를 받는다. 생성에는 본문의 `id`, 수정에는 경로의 `memberId`를 사용한다. 응답은 `id`와 `updatedAt`을 함께 반환한다.

### 공휴일
- `GET /api/admin/v1/public-holidays`
- `POST /api/admin/v1/public-holidays`
- `PUT /api/admin/v1/public-holidays/{holidayId}`
- `DELETE /api/admin/v1/public-holidays/{holidayId}`

공휴일 요청에는 `holidayDate`, `name`, `isActualHoliday`가 들어간다. 응답은 `id`를 함께 반환한다.

## 환경 변수 작성 위치
- 웹은 Vite의 `envDir: ".."` 설정에 따라 프로젝트 루트 `.env`를 읽는다. `web/src/config/env.ts`는 React 진입점에 필요한 값을 초기화할 때 검사한다.
- 서버 설정은 실행 환경의 환경 변수 또는 `server/src/main/resources/application-local.yaml`에 둔다. Spring의 설정과 Vite의 `.env` 로딩은 별개다. [application.yaml](../server/src/main/resources/application.yaml)

### 서버 변수
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`
- `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE`
- `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT`
- `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME`
- `SPRING_DATASOURCE_HIKARI_LEAK_DETECTION_THRESHOLD`
- `SPRING_MAIN_LAZY_INITIALIZATION`
- `SPRING_DATA_JPA_REPOSITORIES_BOOTSTRAP_MODE`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `PORT`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_VALIDITY_SECONDS`
- `JWT_REFRESH_TOKEN_VALIDITY_SECONDS`
- `APPLE_CLIENT_ID`
- `GOOGLE_WEB_CLIENT_ID`
- `AUTH_HTTP_CONNECT_TIMEOUT_MILLIS`
- `AUTH_HTTP_READ_TIMEOUT_MILLIS`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `ADMIN_SESSION_SECRET`
- `ADMIN_SESSION_VALIDITY_SECONDS`

### 웹 변수
- `VITE_API_BASE_URL`
- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`
- `VITE_FIREBASE_MEASUREMENT_ID` (선택)
- `VITE_PLAY_STORE_URL` (선택)
- `VITE_APP_STORE_URL` (선택)
- `ANDROID_APP_LINKS_SHA256`, `IOS_UNIVERSAL_LINKS_APP_ID`는 초대 링크 연결 파일용 빌드 변수다. [초대 배포 안내](../web/README.md#여행-초대-링크-배포)

## Render Static Web 배포
- Root Directory: `web`
- Build Command: `npm ci && npm run build`
- Publish Directory: `dist`
- Rewrite Rule: `/* -> /index.html`. 여행 초대 경로와 연결 파일의 응답은 [웹 배포 가이드](../web/README.md)를 따른다.

## 주의 사항
- 어드민 비밀값은 서버 환경변수에만 둔다.
- 웹의 Firebase 값은 공개 가능한 client config이지만, Storage 보안 규칙은 별도로 관리해야 한다.
- 보안 강화를 위해 추후 IP 제한 또는 별도 Admin Auth로 확장할 수 있다.

## Firebase 이미지 경로 예시
- 공개 URL 예시: `https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fbrisbane%2F1.jpg?alt=media&token=...`
- object path 예시: `places/australia/brisbane/1.jpg`
- 체험 썸네일 object path 예시: `places/australia/brisbane/experiences/1.jpg`
