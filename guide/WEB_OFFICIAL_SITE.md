# WEB_OFFICIAL_SITE

## 목적
- 오구리 공식 웹(`/`)에서 앱 핵심 가치를 빠르게 전달하고 스토어 설치로 자연스럽게 전환한다.
- 앱 내 UX 라이팅 톤(`...해볼까요?`, `부담 없는 제안형`)을 웹에서도 일관되게 사용한다.

## 반영된 구조
- Top Navigation: 브랜드 + 스토어 CTA
- Hero: 풀블리드 인트로 + 대형 카피 + CTA
- Message Band: 핵심 가치 문장 강조
- Immersive Preview: 3개 화면 프리뷰(중앙 강조형)
- Experience Timeline: 카드 대신 단계형 스토리 흐름
- Share Flow: 공유 링크 동작 설명 + 비주얼
- FAQ: 아코디언
- Final CTA + Mobile Install Bar

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
- `web/public/assets/official/hero-travel-collage.svg`
- `web/public/assets/official/recommendation-cards-preview.svg`
- `web/public/assets/official/share-flow-preview.svg`
- `web/public/assets/official/screenshots/home-screen-placeholder.svg`
- `web/public/assets/official/screenshots/calendar-screen-placeholder.svg`
- `web/public/assets/official/screenshots/my-page-screen-placeholder.svg`
- 상세 가이드: `web/public/assets/official/README.md`

## 운영 체크리스트
- 실제 스토어 상세 페이지 URL로 `VITE_PLAY_STORE_URL`, `VITE_APP_STORE_URL` 설정
- 더미 SVG를 브랜드 홍보 이미지로 교체
- 배포 후 모바일/데스크톱에서 CTA 클릭 동작 확인

## 레퍼런스 반영 포인트
- 참고 사이트:
  - `https://www.websoso.kr/`
  - `https://hearit-landing.pages.dev/`
- 반영 의도:
  - 첫 화면에서 즉시 CTA가 보이는 구조
  - 카드 중심 정보 밀도와 섹션 리듬 강화
  - 스크린샷/리뷰/FAQ를 이용한 신뢰도 보강
