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
