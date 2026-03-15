# MVP_WIREFRAME

## 포지셔닝
- 오구리는 `연차 전략 추천 앱`이다.
- 여행 앱/연차 기록 앱/단순 공휴일 달력이 아니다.

## 화면 목록
1. Splash
2. Login
3. Home
4. Place Detail
5. Calendar
6. Period Detail
7. MyPage
8. Web Document (건의하기/약관/개인정보)

## 바텀 네비게이션
- `Home` / `Calendar` / `MyPage`
- 상세 화면은 push 화면으로 진입한다.

## 핵심 UX 규칙
- Home은 "최고 전략 1개 강조"가 기본.
- 순위 토글은 슬라이딩 배경 인디케이터를 사용한다.
- 저장 토글 버튼은 재사용 가능한 공용 컴포넌트로 둔다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.

## 광고 정책
- Home / Strategy Detail / Strategy Calendar 하단 배너 1개.
- 배너 높이: 화면 높이 10~12% 이내.
- 콘텐츠와 배너는 Divider 등으로 분리.
- Splash / Login / MyPage는 광고 없음.

## BM
- 배너 광고: 운영비 보조
- Pro 구독: 핵심 수익
- 여행 제휴: 확장 수익

## 구현 메모
- 서버 연동은 Ktor 기반으로 구현하고, 스웨거 확정 API를 우선 사용한다.
- 문자열은 리소스로 분리하고 다국어(ko/en) 확장을 고려한다.
- 내비게이션은 타입 세이프 라우팅을 사용한다.
