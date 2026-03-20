# WEB_OFFICIAL_SITE

## 목적
- 오구리 공식 웹(`/`)에서 앱 핵심 가치를 빠르게 전달하고 스토어 설치로 자연스럽게 전환한다.
- 앱 내 UX 라이팅 톤(`...해볼까요?`, `부담 없는 제안형`)을 웹에서도 일관되게 사용한다.

## 반영된 구조
- Hero: 브랜드 핵심 메시지 + Play Store/App Store CTA
- Highlight Strip: 짧은 가치 문구
- How Oguri Works: 3단계 소개 카드
- Trust Signals: 신뢰 포인트 체크리스트
- Share Section: 공유 링크에서 앱/스토어로 이어지는 흐름 소개
- Asset Plan: 홍보용 이미지 교체 위치 안내
- Final CTA: 스토어 이동 버튼 재노출

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
- 상세 가이드: `web/public/assets/official/README.md`

## 운영 체크리스트
- 실제 스토어 상세 페이지 URL로 `VITE_PLAY_STORE_URL`, `VITE_APP_STORE_URL` 설정
- 더미 SVG를 브랜드 홍보 이미지로 교체
- 배포 후 모바일/데스크톱에서 CTA 클릭 동작 확인
