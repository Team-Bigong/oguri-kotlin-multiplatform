# 여행 디자인 작업 인계

초기 인계 2026. 11. 15. · 코드 대조 2026. 11. 22. (`develop-mobile`의 `fe856357`)

화면의 현재 구조와 토큰은 [여행 디자인 명세](plan/TRAVEL_PLANNER_DESIGN.md)에, 실행 절차는 [여행 검증 가이드](plan/TRAVEL_PLANNER_TESTING.md)에 정리했다.

## 현재 코드에 반영한 항목

- `처음 추천받은 기간`은 여행 정보 화면에서 요일 없이 월·일로 표시한다. 추천 출처가 있으면 원래 추천을 열 수 있다. [TravelManagementDialogs.kt](composeApp/src/commonMain/kotlin/com/bigong/oguri/feature/travelplanner/ui/component/TravelManagementDialogs.kt)
- 여행 탭과 홈 이어보기, 여행지·기간 상세의 생성 진입, 지도 접기·펼치기, 일별 직선거리와 전체 일정 보기를 제공한다.
- 초대 대화상자에 선택 도시를 모두 표시한다. 이미 참여한 사용자는 여행 상세를 바로 연다. 현재 관리 메뉴에서는 출발 전 알림 항목을 제거했다.
- 주요 변경 이력과 검증 결과는 [여행 진행 기록](plan/TRAVEL_PLANNER_PROGRESS.md)에 있다.

## 2026-11-15 실행 기록

- 기준 디자인은 Figma 오구리 파일 `0yWNBZYA5pNBRrHKjxr3gk`의 Screen 페이지, `여행 · ` 및 `여행 진입 · ` 프레임이었다.
- Pixel 9 Pro 에뮬레이터에서 초대 참여와 만료·철회·삭제 뒤 내 여행으로 복귀하는 흐름, 여행 정보의 원래 추천 보기, 홈의 여행 계획 만들기 진입을 확인했다.
- 당시 Android 단위 테스트 94개, 일반 Debug APK 빌드, iOS Kotlin 시뮬레이터 컴파일을 통과했다. 회원 여행 생성·삭제와 iOS 실행은 별도 검증 범위였다.

## 기기와 시안 대조에서 확인할 항목

- 최신 시안을 기준으로 홈 이어보기, 추천 상세에서 여행 열기·새 여행 만들기, 여행 정보의 추천 기간과 버튼 높이·글꼴을 대조한다.
- 라이트·다크, 큰 글자와 좁은 화면에서 지도·목록, 검색과 직접 추가, 저장 없이 닫기, 권한 종료·충돌·되돌리기를 확인한다.
- 초대 링크의 미설치·신규 참여·기존 멤버·만료 흐름과 여러 도시 표시를 Android·iOS에서 확인한다. [회귀 검증 항목](plan/TRAVEL_PLANNER_TESTING.md#앱과-배포-링크-회귀-검증)
