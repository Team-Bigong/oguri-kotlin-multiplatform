# 여행 플래너 검증과 운영

코드 대조 2026. 11. 22. (`develop-mobile`의 `fe856357`)

## 단위 테스트와 앱 빌드

```sh
./gradlew :server:test :composeApp:testDebugUnitTest :composeApp:assembleDebug :composeApp:iosSimulatorArm64Test
npm --prefix web ci
npm --prefix web test
```

Gradle은 JDK 17로 실행한다. Android Debug 빌드에는 `composeApp/src/debug/google-services.json`이 필요하며 지도 키는 `GOOGLE_MAPS_ANDROID_API_KEY` 또는 로컬 `google.maps.android.api.key`로 설정한다. iOS 공통 테스트는 macOS arm64와 Xcode 시뮬레이터에서 실행한다. Swift 호스트 전체 빌드와 웹 빌드·연결 파일 검증은 각각 아래의 [iOS 빌드](#ios-swift-호스트-시뮬레이터-빌드)와 [웹 배포 가이드](../web/README.md#여행-초대-링크-배포)를 따른다.

`:server:test`에 `TRAVEL_TEST_DATABASE_URL`을 제공하면 PostgreSQL 통합 테스트도 실행한다. 전용 DB가 없으면 아래 스크립트를 사용한다.

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

통합 테스트는 실행별 스키마에서 Flyway 전체 적용, V39에서 만든 데이터의 최신 버전 업그레이드, 기존 경험·변경 데이터 보존, 현재 방문·삭제 상태 조회, 변경 확인 범위, 지연 순서 제약을 확인한다. HTTP 통합 테스트는 테스트 서버의 응답에서 초대 도시 목록, 기존 멤버 식별, 권한 종료, 일정 수정 번호 `200`·`204` 응답도 확인한다. 별도 트랜잭션 테스트는 실제 Hibernate 매핑과 JPA/JDBC 저장소를 같은 트랜잭션 관리자로 연결한다. 첫 요청의 커밋을 보류한 동안 두 번째 요청이 PostgreSQL 잠금에서 대기함을 `pg_locks`로 확인한 뒤, 생성/명령 재시도·추가/추가·메모/메모·독립 필드·순서/순서·삭제/수정·제외/저장·전체 롤백을 검증한다. 위 접속 정보는 전용 테스트 DB의 예시다. 운영 DB를 지정하지 않는다.

V24 이전에는 `destination_experiences` 생성 DDL이 버전 관리에 없었다. 새 Flyway 콜백이 여행지 테이블 생성 후 누락된 경험 테이블만 보완한다. 기존 테이블·데이터와 적용된 마이그레이션의 체크섬은 변경하지 않는다.

V41은 회원 조회에 필요한 `onboarding_completed` 열을 추가한다. 새 DB의 기본값은 `false`이며, 이미 수동으로 추가한 열과 기존 완료 값은 보존한다. 관련 통합 테스트는 새 스키마의 Hibernate 검증과 기존 완료 값 보존을 각각 확인한다.

기존 알림 API의 회귀 검증도 같은 임시 DB 실행에 포함된다. 실제 트랜잭션으로 취소/인계·인계/취소·새 예약/이전 작업·기기 해제/인계·멤버 제외/인계·기간 변경/인계·두 작업의 동일 예약 인계를 겹쳐 실행한다. 발송 공급자 인증 실패·거절·예외·중단은 공급자 모형을 사용하는 단위 테스트로 확인한다. 이 결과는 실제 FCM/APNs 수락·기기 표시·OS 권한·앱 종료 상태 수신을 증명하지 않는다.

## iOS Swift 호스트 시뮬레이터 빌드

```sh
bash scripts/verify_ios_simulator.sh
```

전체 Xcode와 Java 17이 필요하다. 이 명령은 `iosApp-Debug`의 arm64 시뮬레이터 앱을 서명 없이 빌드하며, Kotlin 프레임워크·Swift 지도 브리지와 남아 있는 알림 연결 코드를 컴파일한다. 명시한 `DEVELOPER_DIR`를 우선 사용하고, 기본 선택이 Command Line Tools이면 설치된 `/Applications/Xcode.app`을 사용한다.

`Package.resolved`에 고정된 버전을 사용한다. 공개 패키지의 바이너리 다운로드 전 인증 저장소 조회에서 대기하는 경우를 피하도록 Xcode의 `-packageAuthorizationProvider netrc` 옵션을 해당 명령에 적용한다. [관련 도구 이슈](https://github.com/actions/runner-images/issues/13175)는 같은 대기 증상을 보고한다. 전역 키체인 설정을 변경하지 않는다.

빌드는 앱 실행·지도 표시·실제 푸시 전달의 검증과 구분한다. 실제 앱 실행에는 해당 iOS Firebase 앱의 `GoogleService-Info-Debug.plist`가 앱 리소스에 포함되어야 하며, 배포 설정은 `GoogleService-Info-Release.plist`를 사용한다. Android의 `google-services.json`으로 이 파일을 대신할 수 없다. 두 plist는 Git 제외 대상이다.

로컬 설정은 `iosApp/iosApp/GoogleService-Info-Debug.plist`와 `iosApp/iosApp/GoogleService-Info-Release.plist`에 둔다. 각 파일의 `BUNDLE_ID`가 해당 `iosApp/Configuration/Debug.xcconfig` 또는 `Release.xcconfig`의 `PRODUCT_BUNDLE_IDENTIFIER`와 일치해야 한다. Xcode의 동기화 리소스 폴더에서 앱에 포함되므로 별도 참조를 추가하지 않는다. 설정 파일의 내용은 로그나 커밋에 남기지 않는다.

빌드 후 선택한 시뮬레이터에 `Oguri.app`을 설치하고 Debug 번들 식별자로 실행한다. 앱 번들에 Debug plist가 실제로 존재하고 앱과 Firebase 번들 식별자가 같은지 확인한 뒤 로그인 화면 표시까지 검증한다. 이 단계만으로 회원 로그인·Google 지도 표시·실제 APNs/FCM 수신이 검증되지는 않는다.

여행 지표의 수집 경계와 읽기 전용 기초 집계는 [지표 가이드](./TRAVEL_PLANNER_METRICS.md)를 따른다. PostgreSQL 검증에는 생성·참여 재시도/재참여 중복 방지, 첫 일차 배정 저장, 참여 전후 편집, 기간 변경·삭제·탈퇴 후 관찰 분모 보존, 전체 롤백, 기존 여행의 시작 미상 구분과 실제 보고서 SQL 실행이 포함된다.

세션별 준비 기간 집계도 같은 PostgreSQL 실행에서 실제 SQL로 검증한다. 같은 세션·표시만 된 재방문·24시간 미만·7일 초과 행동을 목적 재사용으로 세지 않고, 정확한 24시간/7일 경계·다른 세션 편집·시작 세션 미상을 구분한다. 모바일 공통 테스트는 30분 유휴 경계와 계정/프로세스 변경·동시 세션 생성·기록 오류가 편집 상태를 바꾸지 않음을 확인한다.

## 운영 사용량 확인

Google Places 검색·상세의 요청 횟수와 응답 시간은 `oguri.travel.places.request`의 `operation=search|details`와 `outcome`으로 확인한다. `oguri.travel.places.rejected`의 `reason=budget`은 분당 한도, `reason=concurrency`는 동시 호출 한도에서 거절된 수다. 배치 상세는 개별 장소 조회 수로 센다. GeoNames 도시 검색과 시간대 확인은 카탈로그 쿼리이며 일반 HTTP 경로 지표에서 확인한다.

방문 장소 검색에는 서버의 `GOOGLE_PLACES_API_KEY`를 사용한다. `GOOGLE_PLACES_REQUESTS_PER_MINUTE`의 기본값은 서버 인스턴스당 300회, 회원별 한도는 90회다.

기존 `http.server.requests`에서 여행 상세 경로의 204와 200 응답을 비교해 변경 없는 자동 갱신의 비중을 확인할 수 있다. 최근 변경 조회는 별도 경로로 관측한다. `oguri.travel.reminder.handoff`의 `accepted`는 발송 공급자가 요청을 수락한 횟수이며, 실제 기기 표시 여부는 기기에서 확인한다.

지표 태그에는 회원·여행·장소 식별자나 사용자 입력을 넣지 않는다. 기본 Actuator 웹 공개 범위는 헬스 체크다. 운영 검증에서 지표를 조회할 때는 내부 관리 경로를 사용한다. 지도 SDK 사용량과 청구액은 공급자 측에서 확인한다.

## 운영 중지

- 서버의 `TRAVEL_WRITES_ENABLED=false`는 여행 생성, 방문 편집 명령, 여행 상세 수정과 소유권 제안·수락을 중지한다. 기존 일정 조회, 여행 삭제, 멤버 제외와 나가기는 계속 가능하다.
- `TRAVEL_INVITES_ENABLED=false`는 초대 발급과 참여를 중지한다. 기존 링크 미리보기와 초대 철회는 계속 가능하다.
- 초대 링크 재복사에는 서버 재시작과 인스턴스 간에 같은 `TRAVEL_INVITE_TOKEN_SECRET` 또는 `JWT_SECRET`이 필요하다. 키를 변경하면 이미 공유한 링크는 만료·철회 전까지 쓸 수 있고, 그 링크의 재복사는 할 수 없다. V51 이전에 발급된 링크도 같은 방식으로 처리한다.
쓰기·초대는 기본값이 `true`이며 환경 변수 변경은 다음 서버 시작 시 적용된다.

## 출발 전 알림의 현재 상태

- 앱에서 출발 전 알림 설정 화면과 예약·알림 진입을 제거했다. 기본 서버 설정인 `TRAVEL_REMINDERS_ENABLED=false`에서는 예약·기기 등록 요청이 `503`을 반환하고 발송 인계를 중지한다.
- 기존 앱과의 호환을 위해 조회, 예약 해제, 기기 연결 해제, 이미 전달된 알림의 서버 확인 API는 유지한다. 활성 상태의 발송 검사는 `TRAVEL_REMINDERS_ENABLED=true`와 유효한 `TRAVEL_REMINDER_FIREBASE_PROJECT_ID`를 지정한 테스트 환경에서 별도로 수행한다.
- DB 경합·발송 모형 테스트는 실기기의 FCM/APNs 전달이나 운영 설정의 증거로 사용하지 않는다.

## CI의 여행 지도와 Firebase 설정

- Android CI와 배포 워크플로는 `GOOGLE_MAPS_ANDROID_API_KEY` GitHub Actions 시크릿을 읽는다. Android 앱 제한을 적용한 키에는 디버그와 배포 패키지명 및 각각의 서명 인증서 SHA-1을 등록한다. Play 앱 서명 사용 시 배포 인증서는 Play Console에서 확인한다.
- Xcode Cloud에는 각각 `GOOGLE_MAPS_IOS_API_KEY_DEBUG`와 `GOOGLE_MAPS_IOS_API_KEY`를 전달한다. `ci_post_clone.sh`에서 각 환경의 `GOOGLE_MAPS_IOS_API_KEY` 빌드 설정에 적용한다.
- Xcode Cloud의 `OGURI_IOS_FIREBASE_DEBUG_PLIST_BASE64`와 `OGURI_IOS_FIREBASE_RELEASE_PLIST_BASE64`에는 해당 plist의 Base64 값을 전달한다. 릴리스 plist와 릴리스 지도 키는 필수이며, 파일 구조와 번들 식별자가 일치하면 앱 리소스 경로에 기록한다.

## 앱과 배포 링크 회귀 검증

Android의 Galaxy S25 API 36과 iOS 기기에서 공통 흐름을 확인한다.

- 홈 추천과 이어보기 카드를 확인하고 하단 `여행`에서 내 여행을 연다. 여러 도시가 모두 표시되는지, 상단 추가 아이콘과 하단 배너가 탐색·스크롤을 가리지 않는지 확인한다. 비회원 안내에는 로그인과 하단 배너가 표시되는지 확인한다.
- 추천 도시로 시작하는 여행 양식에 도시와 추천 날짜가 전달되는지 확인한다. 기간 상세에서 직접 시작하면 추천 날짜가 전달되고 도시는 선택할 수 있어야 한다.
- 점 세 개 아이콘으로 여행 관리를 열고 소유자는 연필 아이콘으로 여행 정보를 편집할 수 있는지 확인한다.
- 최초 조회는 기존 스켈레톤을, 전체 화면 저장은 오구리 중앙 로딩을 표시하는지 확인한다.
- 빈 일차에서는 이전 동선의 좌표 또는 기준 도시가 지도 범위를 정하는지 확인한다. 지도를 접고 다른 날짜를 선택한 뒤 다시 연다. 장소가 있는 날에는 점선과 직선거리, 전체 일정에는 일차별 핀과 목록을 확인한다.
- 여러 도시의 장소 검색·직접 추가·메모·카테고리를 검증한다. 행을 길게 눌러 순서를 바꾸고, 밀어서 삭제한 뒤 되돌린다. Google 장소명을 다시 가져오는 동안 스켈레톤이 표시되는지 확인한다.
- 배포된 `www.oguri.kr`의 초대 링크를 설치 유무, 비회원 로그인 복귀, 새 멤버, 기존 멤버로 확인한다. 신규 참여는 모든 도시를 표시한 확인 대화상자에서 진행하며, 기존 멤버는 상세로 바로 이동한다. 공유 메신저의 초대 썸네일도 확인한다. [웹 배포 확인](../web/README.md#여행-초대-링크-배포)
- TalkBack·VoiceOver, 좁은 폭·큰 글자, 지도·장소 검색 장애에서도 날짜와 목록에 접근할 수 있는지 확인한다.

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

당시 검증 명령은 `:composeApp:assembleDebug`, `:composeApp:testDebugUnitTest`, `git diff --check`였다. 더보기·홈·기간/도시 상세는 레이어 사양과 소스를 대조했다. 실제 로그인·초대·저장 요청, iOS 실행, 큰 글자·다크 모드 전수 검증은 별도 범위다.

## GeoNames 도시 데이터 적재

Flyway V52가 적용된 PostgreSQL에 연결한 뒤 실행한다. 연결 정보는 표준 PostgreSQL 환경 변수(`PGHOST`, `PGDATABASE`, `PGUSER` 등)로 전달한다.

```sh
mkdir -p /tmp/oguri-geonames
for file in allCountries.zip alternateNamesV2.zip admin1CodesASCII.txt countryInfo.txt; do
    curl -fL --retry 2 "https://download.geonames.org/export/dump/$file" -o "/tmp/oguri-geonames/$file" || exit 1
done
python3 scripts/import_geonames.py /tmp/oguri-geonames
```

출처는 [GeoNames](https://www.geonames.org/export/)이며 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)을 따른다. 현재 거주지 데이터에 국가·지역을 붙이고, 한국어 대표 이름과 한글·영문 별칭을 골라 적재한다. 기존 도시 ID는 유지하고 중복 별칭은 건너뛴다.

저장 공간이 제한된 DB에는 같은 배포본의 `cities500.zip`도 내려받고 `--compact`를 붙인다. 인구 기준 도시 목록과 한국어 대표 이름이 있는 추가 거주지를 함께 적재한다.
