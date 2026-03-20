# WEB_ADMIN_SETUP

## 목적
- React Native 기반 Web에서 공식 웹(`/`)과 어드민 웹(`/admin`)을 함께 운영한다.
- 어드민 웹은 장소/사용자/공휴일 DB를 CRUD로 관리한다.
- 장소 이미지는 업로드 시 가로 1280px 리사이즈 + 800KB 이하 압축 후 Firebase Storage로 업로드한다.
- Firebase Storage 저장 경로는 `places/{country}/{city}/{index}.jpg` 규칙을 사용한다.
- 어드민 장소 폼에서 Storage 경로(`country`, `city`)는 드롭다운으로 선택하거나 직접 입력할 수 있다.
- 장소 수정 시 폼에서 제거된 기존 이미지는 저장 완료 후 Firebase에서도 함께 삭제한다.
- 장소 삭제 시 연결된 모든 이미지도 Firebase에서 함께 삭제한다.
- 장소 편집 중 새로 업로드한 이미지는 폼 초기화/다른 장소 불러오기 시 Firebase에서 정리한다.
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
