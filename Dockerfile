# 1. Build Stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY version.properties .
RUN mkdir -p composeApp server shared
COPY composeApp/build.gradle.kts composeApp/build.gradle.kts
COPY server/build.gradle.kts server/build.gradle.kts
COPY shared/build.gradle.kts shared/build.gradle.kts

# Gradle 배포판과 서버 런타임 의존성을 먼저 받아 Docker 레이어 캐시를 활용합니다.
RUN chmod +x ./gradlew
RUN ./gradlew :server:dependencies --configuration runtimeClasspath --no-configuration-cache

# 서버 빌드에 필요한 소스를 명시적으로 복사합니다.
COPY shared/src shared/src
COPY server/src server/src

# 서버 모듈 빌드
RUN ./gradlew :server:clean :server:bootJar -x test --no-configuration-cache

# 2. Run Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=build /app/server/build/libs/app.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행 (Render의 PORT 환경 변수를 셸에서 확장해 서버 포트에 반영)
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Xmx${JAVA_MAX_HEAP:-512m} -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -Dspring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:-none} -Dspring.data.jpa.repositories.bootstrap-mode=${SPRING_DATA_JPA_REPOSITORIES_BOOTSTRAP_MODE:-lazy} -jar app.jar"]
