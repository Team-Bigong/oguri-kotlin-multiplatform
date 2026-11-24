# MVP_WIREFRAME

## 포지셔닝
- 오구리는 연차 조합과 그 시기에 맞는 여행지를 추천하고, 실제 여행 계획으로 이어 준다.
- 여행에서는 동행자와 일차별 장소·순서·메모를 함께 정리한다.

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
13. 여행 목록, 여행 만들기, 여행 상세
14. 여행 멤버·관리, 초대 확인, 여행 전용 로그인·온보딩

## 하단 내비게이션

- 메인 탭은 홈, 캘린더, 여행, 마이 순서다.
- 검색, 여행지·사진·기간 상세, 여행 생성·상세·관리, 화면 설정, 외부 문서는 현재 화면 위에 연다.
- 홈·캘린더·마이 탭을 다시 누르면 각 화면을 최상단으로 스크롤한다.
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
- 여행 탭은 회원의 예정·진행 중 여행과 접을 수 있는 지난 여행을 보여 준다. 비회원은 로그인 안내를 본다.
- 여행지·기간 상세에서 추천 도시나 날짜를 가져와 여행을 만들 수 있다. 홈에서 예정·진행 중 여행을 이어 본다.
- 여행 상세에서 일차별 지도·방문 목록을 보고, 검색하거나 직접 입력한 장소를 선택 날짜에 추가한다. 초대 링크로 동행자를 추가할 수 있다. [여행 화면 명세](../plan/TRAVEL_PLANNER_SPEC.md)
- 저장 토글 버튼은 재사용 가능한 공용 컴포넌트로 둔다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.

## 광고 정책
- 서버 응답 기반 광고 카드는 Home과 Place Detail에서 사용한다.
- AdMob App Open 광고는 메인 탭 진입 이후 화면 전환 시 foreground session당 최대 1회 노출한다.
- Calendar는 추천 카드 index 3 뒤에 첫 inline banner를 노출하고 이후 12개 카드마다 반복한다.
- Photo Detail은 하단 banner를 노출한다.
- 마이페이지와 여행 탭은 하단 배너를 사용한다. 여행 탭의 최초 로딩·조회 실패에는 배너를 표시하지 않는다.
- Splash, Login, Onboarding, Search, Web Document, TravelLogin, TravelOnboarding은 App Open 광고 전환에서 제외한다.

## BM
- 배너 광고: 운영비 보조
- 제휴 링크: 항공권/숙소/액티비티 전환 수익
- Pro 구독: 확장 수익 후보

## 구현 메모
- 서버 연동은 Ktor 기반으로 구현하고 실제 API 응답을 우선 사용한다.
- 인증 토큰이 없으면 guest header로 API를 호출한다.
- 문자열은 `composeResources`로 분리하고 영어/한국어/일본어/중국어 리소스를 함께 유지한다.
- 내비게이션은 타입 세이프 `RouteModel`을 사용한다.
- 도시·기간 공유는 `oguri://open/place`, `oguri://open/period`를 사용한다. 여행 초대는 `https://www.oguri.kr/trip-invite/`의 URL 조각에 토큰을 담는다. [초대 배포 안내](../web/README.md#여행-초대-링크-배포)

## Calendar 개편 메모
- 상단 헤더와 연차 선택 바텀시트 아래에 추천 카드 목록을 표시한다.
- 한 번에 1개 카드만 펼치고, 카드 전체 탭으로 확장/축소한다.
- 카드 내부 달력은 최소 3주 이상 노출하며, 추천 날짜가 가장 많은 주가 중앙에 오도록 배치한다.
- 추천 구간의 비휴일 날짜는 `Neutral70`, 휴일 날짜는 `Orange50`, 일반 날짜는 `Neutral50`을 사용한다.
- 추천 리스트는 페이지 단위로 누적 로딩한다.
- 다음 페이지 loading 중에는 `SkeletonBox` 기반 append loading UI를 노출한다.
