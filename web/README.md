# Oguri Web (React Native Web)

## 시작하기

```bash
cd web
npm install
npm run dev
```

## 빌드

```bash
npm run build
```

정적 결과물은 `web/dist`에 생성됩니다.

## Render Static Web 배포

- Root Directory: `web`
- Build Command: `npm ci && npm run build`
- Publish Directory: `dist`
- Rewrite Rule: `/* -> /index.html`

`/admin` 경로 직접 접근을 위해 Rewrite Rule이 필요합니다.

## 여행 초대 링크 배포

Render의 빌드 환경에 `TRAVEL_ANDROID_SIGNING_SHA256`과 `TRAVEL_IOS_APP_ID`를 설정합니다. Android 값은 실제 배포 앱의 서명 인증서 지문입니다. iOS 값은 배포 앱의 전체 애플리케이션 식별자입니다. 설정 값을 넣고 빌드하면 `dist/.well-known/`에 Android와 iOS 연결 파일이 생성됩니다. 웹 CI는 검증용 식별자로 파일 생성과 형식을 검사합니다.

초대 링크는 `https://www.oguri.kr/trip-invite/`에서 열립니다. 초대 페이지가 포함된 웹 빌드를 `release-web`에서 배포합니다. Render에서 필요한 경우 `/trip-invite`와 `/trip-invite/`를 각각 `/trip-invite/index.html`로 연결하는 Rewrite 규칙을 기존 포괄 규칙보다 앞에 둡니다. `/.well-known/apple-app-site-association`과 `/.well-known/assetlinks.json`은 리디렉션 없이 `application/json`으로 제공해야 합니다. Render의 [정적 사이트 헤더](https://render.com/docs/static-site-headers)에서 경로별 `Content-Type`을 지정할 수 있습니다.

배포 빌드와 동일한 환경 값을 설정한 뒤 웹 폴더에서 실행합니다.

```sh
npm run build
node scripts/verifyTravelInvite.mjs
node scripts/verifyTravelInvite.mjs https://www.oguri.kr
```

첫 검증은 빌드 결과를 확인합니다. 주소를 넣은 검증은 배포된 웹 페이지와 연결 파일의 응답 상태, 콘텐츠 유형, 식별자를 확인합니다.
