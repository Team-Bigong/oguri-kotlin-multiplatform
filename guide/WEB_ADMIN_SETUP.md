# WEB_ADMIN_SETUP

## 목적
- React Native 기반 Web에서 공식 웹(`/`)과 어드민 웹(`/admin`)을 함께 운영한다.
- 어드민 웹은 장소/사용자/공휴일 DB를 CRUD로 관리한다.
- 장소 이미지는 업로드 시 가로 1280px 리사이즈 + 800KB 이하 압축 후 Firebase Storage로 업로드한다.
- Firebase Storage 저장 경로는 `places/{country}/{city}/{index}.jpg` 규칙을 사용한다.
- 장소 experience 썸네일은 `places/{country}/{city}/experiences/{index}.jpg` 경로로 업로드한다.
- 어드민 장소 폼의 국가/Storage 경로(`country`, `city`)는 각각 단일 입력칸에서 추천 목록 선택 + 직접 입력을 함께 지원한다.
- Storage 경로(`country`, `city`)는 영문 소문자 slug만 허용한다. (`a-z`, `0-9`, `-`)
- Storage 경로 추천 목록은 하드코딩이 아니라 DB에 저장된 장소 이미지/체험 썸네일 URL에서 추출한 실제 경로를 사용한다.
- 어드민 장소 폼에서 `체험 관리(Experience)` 섹션으로 제목/설명/링크/썸네일을 추가·수정·삭제할 수 있다.
- 어드민 장소 폼에서 추천 기간별 날씨(평균 온도, 강수량)를 추가·수정할 수 있다.
- 어드민 나라 폼에서 국가별 빅맥지수를 추가·수정할 수 있다.
- 장소 수정 시 폼에서 제거된 기존 이미지는 저장 완료 후 Firebase에서도 함께 삭제한다.
- 장소 수정 시 폼에서 제거된 기존 experience 썸네일도 저장 완료 후 Firebase에서 함께 삭제한다.
- 장소 삭제 시 연결된 모든 장소 이미지 + experience 썸네일을 Firebase에서 함께 삭제한다.
- 장소 편집 중 새로 업로드한 이미지는 폼 초기화/다른 장소 불러오기 시 Firebase에서 정리한다.
- 장소 편집 중 새로 업로드한 experience 썸네일도 폼 초기화/다른 장소 불러오기 시 Firebase에서 정리한다.
- 장소 수정 시 삭제 대상 판정은 URL 토큰이 아닌 Storage object path 기준으로 처리한다.

## 구조
```text
web/
├─ src/
│  ├─ features/
│  │  ├─ official/OfficialHomePage.tsx
│  │  └─ admin/AdminApp.tsx
│  ├─ lib/
│  │  ├─ apiClient.ts
│  │  ├─ firebase.ts
│  │  └─ imageProcessing.ts
│  ├─ config/env.ts
│  └─ types/admin.ts
├─ .env.example
└─ package.json
```

## 어드민 UI 레이아웃
- PC 최적화 대시보드형 UI:
  - 좌측 사이드바(기능 탭)
  - 상단 헤더(현재 섹션 + 빠른 액션)
  - 상단 요약 카드(장소/체험/이미지/사용자/공휴일)
  - 본문 작업 영역(CRUD 폼 + 목록)
- 컬러 톤은 오구리 공식 웹과 맞춘 블루/민트 계열로 통일한다.

## 서버 어드민 API
- Prefix: `/api/admin/v1`
- 인증: `POST /api/admin/v1/auth/login`으로 토큰 발급 후 `Authorization: Bearer {token}` 사용
- Swagger 노출 제외: 어드민 컨트롤러에 `@Hidden` 적용

### 인증
- `POST /api/admin/v1/auth/login`

### 장소
- `GET /api/admin/v1/countries`
- `GET /api/admin/v1/destinations`
- `POST /api/admin/v1/destinations`
- `PUT /api/admin/v1/destinations/{destinationId}`
- `DELETE /api/admin/v1/destinations/{destinationId}`

#### 장소 요청/응답 스펙(요약)
- `POST/PUT /api/admin/v1/destinations`의 body에 `experiences`를 포함한다.
- `POST/PUT /api/admin/v1/destinations`의 body에 `flightUrl`을 포함해 스카이스캐너 링크를 저장한다.
- `POST/PUT /api/admin/v1/destinations`의 body에 `weatherTemp1`, `weatherPrecipitationMm1`, `weatherTemp2`, `weatherPrecipitationMm2`를 포함해 추천 기간별 날씨를 저장한다.
- `experiences` 항목 필드:
  - `title`: 액티비티 제목
  - `description`: 액티비티 설명
  - `thumbnailUrl`: 액티비티 썸네일 URL
  - `link`: 외부 이동 링크
  - `sortOrder`: 정렬 순서(1 이상)
- `GET /api/admin/v1/destinations` 응답에도 `flightUrl`, `experiences`가 포함된다.
- `GET /api/admin/v1/destinations` 응답에도 추천 기간별 날씨가 포함된다.

### 사용자
- `GET /api/admin/v1/members`
- `POST /api/admin/v1/members`
- `PUT /api/admin/v1/members/{memberId}`
- `DELETE /api/admin/v1/members/{memberId}`

### 공휴일
- `GET /api/admin/v1/public-holidays`
- `POST /api/admin/v1/public-holidays`
- `PUT /api/admin/v1/public-holidays/{holidayId}`
- `DELETE /api/admin/v1/public-holidays/{holidayId}`

## 환경 변수 작성 위치
- 위치: 프로젝트 루트 `.env` 파일
- Vite 설정(`web/vite.config.ts`)에서 `envDir: ".."`로 지정하여 웹도 루트 `.env`를 사용한다.

### 서버 변수
- `SPRING_DATASOURCE_URL`: Render PostgreSQL 접속 URL
- `SPRING_DATASOURCE_USERNAME`: Render PostgreSQL 사용자명
- `SPRING_DATASOURCE_PASSWORD`: Render PostgreSQL 비밀번호
- `KAKAO_REST_API_KEY`: 카카오 로그인 REST API 키
- `APPLE_CLIENT_ID`: Apple 로그인 client id
- `ADMIN_USERNAME`: 어드민 로그인 아이디
- `ADMIN_PASSWORD`: 어드민 로그인 비밀번호
- `ADMIN_SESSION_SECRET`: 어드민 토큰 서명 시크릿(최소 32바이트)
- `ADMIN_SESSION_VALIDITY_SECONDS`: 어드민 토큰 만료 시간(초)

### 웹 변수
- `VITE_API_BASE_URL`: 서버 API 주소 (예: `https://api.example.com`)
- `VITE_FIREBASE_API_KEY`: Firebase Web API Key
- `VITE_FIREBASE_AUTH_DOMAIN`: Firebase Auth 도메인
- `VITE_FIREBASE_PROJECT_ID`: Firebase 프로젝트 ID
- `VITE_FIREBASE_STORAGE_BUCKET`: Firebase Storage 버킷
- `VITE_FIREBASE_MESSAGING_SENDER_ID`: Firebase Sender ID
- `VITE_FIREBASE_APP_ID`: Firebase App ID
- `VITE_FIREBASE_MEASUREMENT_ID`: (선택) Analytics ID

## Render Static Web 배포
- Root Directory: `web`
- Build Command: `npm ci && npm run build`
- Publish Directory: `dist`
- Rewrite Rule: `/* -> /index.html`
- 경로 fallback: rewrite 설정 전에는 `/#/admin` 해시 경로로도 어드민 진입 가능

## 주의 사항
- 어드민 API는 Swagger에 노출하지 않는다.
- 어드민 비밀값은 서버 `.env`에만 둔다.
- 보안 강화를 위해 추후 IP 제한 또는 별도 Admin Auth(예: Firebase Auth + 서버 검증)로 확장한다.

## Firebase 이미지 경로 예시
- 공개 URL 예시: `https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fbrisbane%2F1.jpg?alt=media&token=...`
- GS 경로 예시: `gs://oguri-74e46.firebasestorage.app/places/australia/brisbane`
- 파일명 번호는 업로드 시 경로 내 기존 최대 번호를 기준으로 `1, 2, 3, ...` 순차 증가 규칙을 사용한다.
- experience 썸네일 경로 예시: `gs://oguri-74e46.firebasestorage.app/places/australia/brisbane/experiences/1.jpg`
