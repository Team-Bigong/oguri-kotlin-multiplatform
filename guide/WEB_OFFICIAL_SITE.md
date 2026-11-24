# WEB_OFFICIAL_SITE

## 목적
- 오구리 공식 웹(`/`)에서 앱 핵심 가치를 빠르게 전달하고 스토어 설치로 자연스럽게 전환한다.
- 앱 내 UX 라이팅 톤(`...해볼까요?`, 부담 없는 제안형)을 웹에서도 일관되게 사용한다.
- 공식 사이트와 어드민이 React 진입점을 공유한다. `/trip-invite/`는 앱 열기·설치를 안내하는 별도 HTML 진입점이다. [초대 배포 안내](../web/README.md#여행-초대-링크-배포)

## 현재 구조
- 라우팅 분기: `web/src/App.tsx`, `web/src/features/routing/pathResolver.ts`
- 여행 초대: [trip-invite/index.html](../web/trip-invite/index.html), [travelInviteModel.ts](../web/src/features/routing/travelInviteModel.ts)
- 공식 홈: `web/src/features/official/OfficialHomePage.tsx`
- 스토어 버튼: `web/src/features/official/components/StoreButtons.tsx`
- 문구/링크 데이터: `web/src/features/official/data/officialContent.ts`
- 스타일: `web/src/features/official/official.css`

## 반영된 화면 구성
- Sticky Navigation: 브랜드 + 섹션 앵커 + 스토어 CTA
- Dark Hero: 대형 카피 + 이중 디바이스 비주얼
- Highlight Band: 핵심 메시지 강조
- Feature Grid: 3단계 핵심 기능
- Preview Section: 앱 화면 카드 3개
- FAQ: `details/summary` 기반 아코디언
- Final CTA + Footer

## UX 라이팅 기준
- 참고 문자열 리소스:
  - `composeApp/src/commonMain/composeResources/values-ko/strings.xml`
  - 예시 키: `home_greeting_question`, `share_period_description`, `share_default_fallback_message`
- 톤 가이드:
  - 명령형보다 제안형 문장 사용
  - 과장보다 "쉽게/부담 없이" 뉘앙스 유지
  - 한 문장 길이를 짧게 유지
  - 앱의 "연차 조합 추천" 포지셔닝을 흐리지 않는다.

## 스토어 링크 설정
- 공식 웹은 아래 환경변수를 읽는다.
  - `VITE_PLAY_STORE_URL`
  - `VITE_APP_STORE_URL`
- 값이 없으면 코드의 실제 스토어 상세 페이지 URL로 fallback 된다.
- 현재 Google Play fallback은 `com.bigong.oguri` package 상세 페이지이다.
- 현재 App Store fallback은 오구리 App Store 상세 페이지이다.
- 코드 위치: `web/src/config/env.ts`, `web/src/features/official/data/officialContent.ts`

## 환경 변수 주의
- `web/src/config/env.ts`는 `VITE_API_BASE_URL`과 Firebase 관련 필수 env도 함께 검증한다.
- 공식 웹만 열더라도 현재 구조상 필수 env가 비어 있으면 앱 초기화가 실패할 수 있다.
- Render 배포 환경에는 공식 웹/어드민 웹에서 쓰는 env를 모두 설정한다.

## 홍보 에셋 교체 경로
- `web/public/assets/official/screenshots/home-screen.png`
- `web/public/assets/official/screenshots/calendar-screen.png`
- `web/public/assets/official/screenshots/my-page-screen.png`
- `web/public/assets/official/screenshots/hero-back-card.png`
- `web/public/assets/official/store/play-store-logo.png`
- `web/public/assets/official/store/app-store-logo.png`
- `web/public/assets/official/app-icon.png`
- `web/public/assets/official/share-thumbnail.png`
- 앱 여행 초대 공유는 같은 에셋 디렉터리의 `travel-invite-thumbnail.png`를 사용한다. 초대 전용 이미지와 Open Graph 태그는 웹 배포 브랜치 `release-web`에서 관리한다. [에셋 가이드](../web/public/assets/official/README.md)

## 푸터 외부 링크
- 건의하기: Google Form
- 서비스 이용약관: Notion
- 개인정보 처리방침: Notion
- 링크 상수는 `web/src/features/official/data/officialContent.ts`에 둔다.

## 운영 체크리스트
- 실제 스토어 상세 페이지 URL로 `VITE_PLAY_STORE_URL`, `VITE_APP_STORE_URL` 설정
- 배포 후 모바일/데스크톱에서 CTA 클릭 동작 확인
- `/admin` 직접 접근을 위해 Render rewrite rule 확인
- `/#/admin` fallback 진입 확인
- `/trip-invite/` 페이지, Android·iOS 연결 파일과 초대 공유 썸네일 응답 확인
- `/app-ads.txt` 배포 결과 확인
- 푸터 외부 링크 확인

## 레퍼런스 반영 포인트
- 참고 사이트:
  - `https://www.websoso.kr/`
  - `https://hearit-landing.pages.dev/`
  - `https://perfect-break-finder.lovable.app`
- 반영 의도:
  - 첫 화면에서 즉시 CTA가 보이는 구조
  - Hero/Feature/Preview/CTA/Footer의 명확한 랜딩 흐름
  - 다크 히어로와 라이트 본문 대비
  - 문구는 오구리 앱 `strings.xml` 톤(제안형, 부담 없는 문장) 유지
