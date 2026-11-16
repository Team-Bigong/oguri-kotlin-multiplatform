# MVP_WIREFRAME

## 포지셔닝
- 오구리는 `연차 전략 추천 앱`이다.
- 여행 앱/연차 기록 앱/단순 공휴일 달력이 아니라, 연차 조합과 그 시기에 맞는 여행지를 한 흐름으로 추천한다.

## 현재 화면 목록
1. Splash
2. Login
3. Onboarding
4. Home
5. Search
6. Place Detail
7. Photo Detail
8. Calendar
9. Period Detail
10. MyPage
11. Display Settings
12. Web Document (건의하기/약관/개인정보)

## 바텀 네비게이션
- 메인 탭은 `Home` / `Calendar` / `MyPage`이다.
- `Search`, `Place Detail`, `Photo Detail`, `Period Detail`, `Display Settings`, `Web Document`는 push 화면으로 진입한다.
- 메인 탭 재선택은 해당 탭을 최상단으로 스크롤한다.
- 메인 탭 이동 시 백스택은 초기화한다.

## 핵심 UX 규칙
- Home은 "최고 전략 1개 강조"가 기본이다.
- 순위 토글은 슬라이딩 배경 인디케이터를 사용한다.
- Home은 추천 기간, 추천 여행지, 주간 인기 여행지, 월간 인기 일정, 광고 카드, Calendar 이동 CTA를 한 흐름으로 보여준다.
- Search는 최근 검색어, 인기 검색어, 이번 주 추천 장소, 자동완성 결과를 제공한다.
- Calendar는 추천 일정 카드 리스트와 무한 스크롤을 사용한다.
- Period Detail은 선택한 기간에 맞는 장소 목록을 페이지 단위로 보여준다.
- Place Detail은 이미지, 여행 정보, 액티비티, 항공권 카드, 같은 기간 추천 장소를 보여준다.
- Photo Detail은 이미지를 확대/축소할 수 있고, zoom 중에는 pager swipe를 막는다.
- MyPage는 guest/member 상태를 분기하고 저장 일정/저장 여행지/연차 설정/문서 링크/계정 작업을 제공한다.
- 저장 토글 버튼은 재사용 가능한 공용 컴포넌트로 둔다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.

## 광고 정책
- 서버 응답 기반 광고 카드는 Home과 Place Detail에서 사용한다.
- AdMob App Open 광고는 메인 탭 진입 이후 화면 전환 시 foreground session당 최대 1회 노출한다.
- Calendar는 추천 카드 index 3 뒤에 첫 inline banner를 노출하고 이후 12개 카드마다 반복한다.
- Photo Detail은 하단 banner를 노출한다.
- MyPage는 guest/member 상태 모두 하단 banner를 노출한다.
- Splash / Login / Onboarding / Search / Web Document에는 광고를 노출하지 않는다.

## BM
- 배너 광고: 운영비 보조
- 제휴 링크: 항공권/숙소/액티비티 전환 수익
- Pro 구독: 확장 수익 후보

## 구현 메모
- 서버 연동은 Ktor 기반으로 구현하고 실제 API 응답을 우선 사용한다.
- 인증 토큰이 없으면 guest header로 API를 호출한다.
- 문자열은 `composeResources`로 분리하고 영어/한국어/일본어/중국어 리소스를 함께 유지한다.
- 내비게이션은 타입 세이프 `RouteModel`을 사용한다.
- 딥링크는 `oguri://open/place`, `oguri://open/period`, KakaoLink nested deeplink를 지원한다.

## Calendar 개편 메모
- 상단 헤더 + 연차 입력 바텀시트 유지, 본문은 카드 리스트 기반으로 전환한다.
- 한 번에 1개 카드만 펼치고, 카드 전체 탭으로 확장/축소한다.
- 카드 내부 달력은 최소 3주 이상 노출하며, 추천 날짜가 가장 많은 주가 중앙에 오도록 배치한다.
- 추천 구간의 비휴일 날짜는 `Neutral70`, 휴일 날짜는 `Orange50`, 일반 날짜는 `Neutral50`을 사용한다.
- 추천 리스트는 페이지 단위로 누적 로딩한다.
- 다음 페이지 loading 중에는 `SkeletonBox` 기반 append loading UI를 노출한다.
