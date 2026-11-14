# 여행 플래너 자동 검증

## 단위 테스트와 앱 빌드

```sh
./gradlew :server:test :composeApp:testDebugUnitTest :composeApp:assembleDebug :composeApp:compileKotlinIosSimulatorArm64
npm --prefix web ci
npm --prefix web test
npm --prefix web run build
```

Android 빌드에는 로컬 Firebase 설정이 필요하다. iOS Kotlin 컴파일은 Swift 호스트 앱 전체 빌드나 실제 기기 전달 검증을 대신하지 않는다.

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
