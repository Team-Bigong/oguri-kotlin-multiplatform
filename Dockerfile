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
RUN ./gradlew :server:dependencies --configuration runtimeClasspath

# 전체 소스 복사 (공통 모듈 shared가 필요하므로 전체 복사)
COPY . .

# 서버 모듈 빌드
RUN ./gradlew :server:bootJar -x test

# 2. Run Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=build /app/server/build/libs/server-1.0.0.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행 (Render의 PORT 환경 변수를 셸에서 확장해 서버 포트에 반영)
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Xmx${JAVA_MAX_HEAP:-512m} -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
