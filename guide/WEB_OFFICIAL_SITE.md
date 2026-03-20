# WEB_OFFICIAL_SITE

## 목적
- 오구리 공식 웹(`/`)에서 앱 핵심 가치를 빠르게 전달하고 스토어 설치로 자연스럽게 전환한다.
- 앱 내 UX 라이팅 톤(`...해볼까요?`, `부담 없는 제안형`)을 웹에서도 일관되게 사용한다.

## 반영된 구조
- Sticky Navigation: 브랜드 + 섹션 앵커 + 스토어 CTA
- Dark Hero: 강한 대비의 대형 카피 + 이중 디바이스 비주얼
- Highlight Band: 핵심 한 줄 메시지 강조
- Feature Grid: 3단계 핵심 기능
- Preview Split Section: 설명 + 실제 화면 카드
- FAQ: 아코디언
- Final CTA + Footer

## UX 라이팅 기준
- 참고 문자열 리소스:
  - `composeApp/src/commonMain/composeResources/values-ko/strings.xml`
  - 예시 키: `home_greeting_question`, `share_period_description`, `share_default_fallback_message`
- 톤 가이드:
  - 명령형보다 제안형 문장 사용
  - 과장보다 "쉽게/부담 없이" 뉘앙스 유지
  - 한 문장 길이를 짧게 유지

## 스토어 링크 설정
- 공식 웹은 아래 환경변수를 읽는다. 값이 없으면 각 스토어 메인으로 fallback 된다.
  - `VITE_PLAY_STORE_URL`
  - `VITE_APP_STORE_URL`
- 코드 위치: `web/src/config/env.ts`, `web/src/features/official/OfficialHomePage.tsx`

## 홍보 에셋 교체 경로
- `web/public/assets/official/screenshots/home-screen.png`
- `web/public/assets/official/screenshots/calendar-screen.png`
- `web/public/assets/official/screenshots/my-page-screen.png`
- `web/public/assets/official/screenshots/hero-back-card.png`
- `web/public/assets/official/store/play-store-logo.png`
- `web/public/assets/official/store/app-store-logo.png`
- `web/public/assets/official/app-icon.png`
- 상세 가이드: `web/public/assets/official/README.md`

## 운영 체크리스트
- 실제 스토어 상세 페이지 URL로 `VITE_PLAY_STORE_URL`, `VITE_APP_STORE_URL` 설정
- 배포 후 모바일/데스크톱에서 CTA 클릭 동작 확인
- 푸터 외부 링크 확인:
  - 건의하기(Google Form)
  - 서비스 이용약관(Notion)
  - 개인정보 처리방침(Notion)

## 레퍼런스 반영 포인트
- 참고 사이트:
  - `https://www.websoso.kr/`
  - `https://hearit-landing.pages.dev/`
  - `https://perfect-break-finder.lovable.app`
- 반영 의도:
  - 첫 화면에서 즉시 CTA가 보이는 구조
  - Hero/Feature/Preview/CTA/Footer의 명확한 랜딩 흐름
  - 다크 히어로와 라이트 본문 대비로 임팩트 강화
  - 문구는 오구리 앱 `strings.xml` 톤(제안형, 부담 없는 문장) 유지
