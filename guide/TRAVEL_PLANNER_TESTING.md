# 여행 플래너 자동 검증

## 단위 테스트와 앱 빌드

```sh
./gradlew :server:test :composeApp:testDebugUnitTest :composeApp:assembleDebug :composeApp:iosSimulatorArm64Test
npm --prefix web ci
npm --prefix web test
npm --prefix web run build
```

Android 빌드에는 로컬 Firebase 설정이 필요하다. iOS 공통 테스트는 macOS arm64와 iOS 시뮬레이터에서 실행한다. 이 테스트와 Swift 호스트 앱 전체 빌드는 별도로 실행한다.

## 임시 PostgreSQL에서 전체 서버 테스트

```sh
python3 scripts/verify_travel_postgres.py
```

이 도구는 새 임시 데이터 디렉터리와 로컬 포트를 사용한다. 기존 DB 접속 설정을 사용하지 않는다. 테스트 후 서버를 중지하고 자체 임시 파일을 삭제한다. DB 테스트가 켜진 실행에서는 Gradle의 이전 테스트 결과를 재사용하지 않는다.

- macOS Apple Silicon에서 서버 바이너리가 없으면 Python 3.12 이상과 `curl`을 사용해 테스트용 PostgreSQL 17.11 번들을 Maven Central에서 받아 SHA-256을 대조한다. [번들 프로젝트](https://github.com/zonkyio/embedded-postgres-binaries)의 Darwin ARM64 배포를 사용하며 시스템 설치·서비스 등록은 하지 않는다.
- 이미 설치된 PostgreSQL을 사용하려면 `TRAVEL_POSTGRES_BIN`에 `postgres`, `initdb`, `pg_ctl`이 들어 있는 디렉터리를 지정한다. 이 경우에도 기존 데이터 디렉터리를 열지 않는다.
- 데이터는 테스트 전용이며 서버는 `127.0.0.1`에서만 요청을 받는다. 운영·개인 DB 접속 문자열은 필요하지 않다.

CI처럼 전용 PostgreSQL 서버가 있는 환경에서는 아래 변수를 지정하고 `:server:test`를 실행할 수 있다.

```sh
TRAVEL_TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/oguri_test \
TRAVEL_TEST_DATABASE_USER=postgres \
TRAVEL_TEST_DATABASE_PASSWORD=postgres \
./gradlew :server:test
```

통합 테스트는 실행별 별도 스키마에서 전체 마이그레이션, V39 데이터의 V40 업그레이드, 기존 경험·변경 데이터 보존, 현재 방문·삭제 상태 조회, 변경 확인 범위, 지연 순서 제약을 확인하고 그 스키마만 정리한다. 별도 트랜잭션 테스트는 실제 Hibernate 매핑과 JPA/JDBC 저장소를 같은 트랜잭션 관리자로 연결한다. 첫 요청의 커밋을 보류한 동안 두 번째 요청이 PostgreSQL 잠금에서 대기함을 `pg_locks`로 확인한 뒤, 생성/명령 재시도·추가/추가·메모/메모·독립 필드·순서/순서·삭제/수정·제외/저장·전체 롤백을 검증한다. 위 접속 정보는 전용 테스트 DB의 예시다. 운영 DB를 지정하지 않는다.

V24 이전에는 `destination_experiences` 생성 DDL이 버전 관리에 없었다. 새 Flyway 콜백이 여행지 테이블 생성 후 누락된 경험 테이블만 보완한다. 기존 테이블·데이터와 적용된 마이그레이션의 체크섬은 변경하지 않는다.

V41은 회원 조회에 필요한 `onboarding_completed` 열을 추가한다. 새 DB의 기본값은 `false`이며, 이미 수동으로 추가한 열과 기존 완료 값은 보존한다. 관련 통합 테스트는 새 스키마의 Hibernate 검증과 기존 완료 값 보존을 각각 확인한다.

알림 검증도 같은 임시 DB 실행에 포함된다. 실제 트랜잭션으로 취소/인계·인계/취소·새 예약/이전 작업·기기 해제/인계·멤버 제외/인계·기간 변경/인계·두 작업의 동일 예약 인계를 겹쳐 실행한다. 발송 공급자 인증 실패·거절·예외·중단은 공급자 모형을 사용하는 단위 테스트로 확인한다. 이 결과는 실제 FCM/APNs 수락·기기 표시·OS 권한·앱 종료 상태 수신을 증명하지 않는다.

## iOS Swift 호스트 시뮬레이터 빌드

```sh
bash scripts/verify_ios_simulator.sh
```

전체 Xcode와 Java 17이 필요하다. 이 명령은 `iosApp-Debug`의 arm64 시뮬레이터 앱을 서명 없이 빌드하며, Kotlin 프레임워크 연결과 Swift 지도·알림 브리지까지 컴파일한다. 명시한 `DEVELOPER_DIR`를 우선 사용하고, 기본 선택이 Command Line Tools이면 설치된 `/Applications/Xcode.app`을 사용한다.

`Package.resolved`에 고정된 버전을 사용한다. 공개 패키지의 바이너리 다운로드 전 인증 저장소 조회에서 대기하는 경우를 피하도록 Xcode의 `-packageAuthorizationProvider netrc` 옵션을 해당 명령에 적용한다. [관련 도구 이슈](https://github.com/actions/runner-images/issues/13175)는 같은 대기 증상을 보고한다. 전역 키체인 설정을 변경하지 않는다.

빌드는 앱 실행·지도 표시·실제 푸시 전달의 검증과 구분한다. 실제 앱 실행에는 해당 iOS Firebase 앱의 `GoogleService-Info-Debug.plist`가 앱 리소스에 포함되어야 하며, 배포 설정은 `GoogleService-Info-Release.plist`를 사용한다. Android의 `google-services.json`으로 이 파일을 대신할 수 없다. 두 plist는 Git 제외 대상이다.

로컬 설정은 `iosApp/iosApp/GoogleService-Info-Debug.plist`와 `iosApp/iosApp/GoogleService-Info-Release.plist`에 둔다. 각 파일의 `BUNDLE_ID`가 해당 `iosApp/Configuration/Debug.xcconfig` 또는 `Release.xcconfig`의 `PRODUCT_BUNDLE_IDENTIFIER`와 일치해야 한다. Xcode의 동기화 리소스 폴더에서 앱에 포함되므로 별도 참조를 추가하지 않는다. 설정 파일의 내용은 로그나 커밋에 남기지 않는다.

빌드 후 선택한 시뮬레이터에 `Oguri.app`을 설치하고 Debug 번들 식별자로 실행한다. 앱 번들에 Debug plist가 실제로 존재하고 앱과 Firebase 번들 식별자가 같은지 확인한 뒤 로그인 화면 표시까지 검증한다. 이 단계만으로 회원 로그인·Google 지도 표시·실제 APNs/FCM 수신이 검증되지는 않는다.

여행 지표의 수집 경계와 읽기 전용 기초 집계는 [지표 가이드](TRAVEL_PLANNER_METRICS.md)를 따른다. PostgreSQL 검증에는 생성·참여 재시도/재참여 중복 방지, 날짜 미배정/배정 저장, 참여 전후 편집, 기간 변경·삭제·탈퇴 후 관찰 분모 보존, 전체 롤백, 기존 여행의 시작 미상 구분과 실제 보고서 SQL 실행이 포함된다.

세션별 준비 기간 집계도 같은 PostgreSQL 실행에서 실제 SQL로 검증한다. 같은 세션·표시만 된 재방문·24시간 미만·7일 초과 행동을 목적 재사용으로 세지 않고, 정확한 24시간/7일 경계·다른 세션 편집·시작 세션 미상을 구분한다. 모바일 공통 테스트는 30분 유휴 경계와 계정/프로세스 변경·동시 세션 생성·기록 오류가 편집 상태를 바꾸지 않음을 확인한다.

## 운영 사용량 확인

서버의 Micrometer 지표로 장소 검색·상세·도시 검색·시간대 조회의 요청 횟수와 응답 시간을 구분한다. `oguri.travel.places.request`의 `operation`과 `outcome`을 사용한다. `oguri.travel.places.rejected`는 외부 호출 전 거절된 요청 수이며 `reason=budget`은 분당 한도, `reason=concurrency`는 동시 호출 한도다. 장소 상세 묶음은 한 요청으로 합치지 않고 개별 조회 수로 센다.

기존 `http.server.requests`에서 여행 상세 경로의 204와 200 응답을 비교해 변경 없는 자동 갱신의 비중을 확인할 수 있다. 최근 변경 조회는 별도 경로로 관측한다. `oguri.travel.reminder.handoff`의 `accepted`는 발송 공급자가 요청을 수락한 횟수이며, 실제 기기 표시 여부는 기기에서 확인한다.

지표 태그에는 회원·여행·장소 식별자나 사용자 입력을 넣지 않는다. 기본 Actuator 웹 공개 범위는 헬스 체크다. 운영 검증에서 지표를 조회할 때는 내부 관리 경로를 사용한다. 지도 SDK 사용량과 청구액은 공급자 측에서 확인한다.

## 운영 중지

- 서버의 `TRAVEL_WRITES_ENABLED=false`는 여행 생성, 방문 편집 명령, 여행 상세 수정과 소유권 제안·수락을 중지한다. 기존 일정 조회, 여행 삭제, 멤버 제외와 나가기는 계속 가능하다.
- `TRAVEL_INVITES_ENABLED=false`는 초대 발급과 참여를 중지한다. 기존 링크 미리보기와 초대 철회는 계속 가능하다.
- 초대 링크 재복사에는 서버 재시작과 인스턴스 간에 같은 `TRAVEL_INVITE_TOKEN_SECRET` 또는 `JWT_SECRET`이 필요하다. 키를 변경하면 이미 공유한 링크는 만료·철회 전까지 쓸 수 있고, 그 링크의 재복사는 할 수 없다. V51 이전에 발급된 링크도 같은 방식으로 처리한다.
- `TRAVEL_REMINDERS_ENABLED=false`는 새 예약과 기기 등록, 서버의 발송 작업을 중지한다. 예약·기기 해제와 이미 전달된 알림의 현재 상태 확인은 계속 가능하다. 아직 유효한 대기 예약은 발송을 다시 활성화하면 처리될 수 있다.

서버 설정은 기본적으로 켜져 있으며 환경 변수 변경은 다음 서버 시작 시 적용된다.


## CI의 여행 지도와 Firebase 설정

- Android CI 디버그 지도에는 `GOOGLE_MAPS_ANDROID_API_KEY_DEBUG`, Android 배포 지도에는 `GOOGLE_MAPS_ANDROID_API_KEY`를 GitHub Actions 시크릿으로 전달한다.
- Xcode Cloud에는 각각 `GOOGLE_MAPS_IOS_API_KEY_DEBUG`와 `GOOGLE_MAPS_IOS_API_KEY`를 전달한다. `ci_post_clone.sh`에서 각 환경의 `GOOGLE_MAPS_IOS_API_KEY` 빌드 설정에 적용한다.
- Xcode Cloud의 `OGURI_IOS_FIREBASE_DEBUG_PLIST_BASE64`와 `OGURI_IOS_FIREBASE_RELEASE_PLIST_BASE64`에는 해당 plist의 Base64 값을 전달한다. 파일 구조와 번들 식별자가 일치하면 앱 리소스 경로에 기록한다.

## Galaxy S25 API 36 회귀 검증

- 홈 추천 카드를 확인하고, 하단 `여행`으로 내 여행을 연다. 사진이 등록된 여행 카드와 상단 추가 아이콘을 확인한다. 추가 아이콘으로 여행 만들기를 열고 도시와 날짜를 고르면 버튼이 활성화되는지 확인한다. 로그인 전에는 여행 탭의 설명과 로그인 버튼을 확인한다.
- 추천 도시로 시작하는 여행 양식에 도시와 추천 날짜가 전달되는지 확인한다. 기간 상세에서 직접 시작하면 추천 날짜가 전달되고 도시는 선택할 수 있어야 한다.
- 점 세 개 아이콘으로 여행 관리를 열고 소유자는 연필 아이콘으로 여행 정보를 편집할 수 있는지 확인한다.
- 최초 조회는 기존 스켈레톤을, 전체 화면 저장은 오구리 중앙 로딩을 표시하는지 확인한다.
- 장소가 없는 여행을 열어 지도와 `장소 추가`가 동시에 보이는지 확인한다. 지도를 접은 뒤 다른 날짜를 선택하고 다시 펼친다. 전체 날짜 창의 지도와 목록도 확인한다.
- 여행 도시의 장소를 검색해 결과, 상세, 지도 핀, 추가 후 새로고침까지 확인한다. 검색에 실패하면 재시도와 직접 추가를 각각 확인한다. 배포 환경에서 검색 오류를 재현하면 해당 환경의 장소 검색 응답과 Google Places 요청 지표를 함께 확인한다.

## 2026-11-14 Figma 여행 화면 대조

대상 파일 `0yWNBZYA5pNBRrHKjxr3gk`의 12개 노드에 대해 디자인 컨텍스트와 렌더를 확인했다.

| 노드 | 화면 | 확인·반영 |
| --- | --- | --- |
| 874:590, 874:630 | 내 여행 / 지난 여행 펼침 | Mint70 제목 띠와 흰 전경, 지난 여행의 오른쪽 화살표, 날짜·메타 정보 배치 |
| 874:683, 874:740 | 여행 상세 / 지도 접힘 | 57dp 날짜 탭 영역, 지도 프레임·접힘 버튼, 45dp 하단 행동 |
| 874:787, 874:852 | 장소 검색 / 직접 추가 | 원본 검색·닫기·선택 아이콘, 주소 한 줄 표시, 40×24dp 토글, 카테고리 타이포그래피 |
| 874:899 | 더보기 | 멤버·초대 아이콘, 섹션 사이 간격, 관리 행과 삭제 행의 구분선 |
| 874:938 | 기간 상세 진입 | 여행 만들기 버튼의 18sp SemiBold와 Mint70 전경 |
| 874:972, 874:1012 | 도시 상세 진입 / 기존 여행 | 정보 카드의 130dp 기본 높이와 본문 크기, 여행 만들기·새 여행 아이콘 |
| 874:1058, 874:1117 | 홈 이어보기 / 여행 만들기 | 기존 진입 위치 확인, 이어보기 카드의 라벨·안쪽 여백 보정 |

추가한 아이콘 16개는 해당 Figma 노드의 SVG 원본을 Android SDK의 `Svg2Vector`로 변환했다. 루트 치수와 경로를 유지하며, 여러 색을 사용하는 검색 선택 표시는 tint를 적용하지 않는다. 실제 도시 사진·추천 데이터와 Google 지도는 계속 동적 데이터로 표시한다.

Android API 36 에뮬레이터에서 360dp 폭, 한국어, 기본 글자 크기의 내 여행, 지난 여행 펼침, 빈 일정의 실제 Google 지도, 지도 접기, 장소 검색·직접 추가 시트를 임시 데이터로 렌더링해 대조했다. 검증용 Activity와 manifest는 최종 소스에 포함하지 않는다. 검색 결과의 Google Maps 출처와 직접 추가 장소의 실제 지도 표시 제약 안내는 유지한다. 다크 모드의 주요 버튼은 기존 대비 조합을 유지한다.

최종 검증은 `:composeApp:assembleDebug`, `:composeApp:testDebugUnitTest`, `git diff --check`로 수행한다. 더보기·홈·기간/도시 상세는 레이어 사양과 소스 대조이며, 이번 에뮬레이터 확인은 실제 로그인·초대·저장 요청, iOS 실행, 큰 글자와 다크 모드 전체 화면 검증을 포함하지 않는다.
