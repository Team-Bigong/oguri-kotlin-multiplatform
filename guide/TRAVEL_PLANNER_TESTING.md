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
