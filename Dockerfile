# 1. Build Stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 전체 소스 복사 (안전한 빌드를 위해 한 번에 복사)
COPY . .

# Gradle 권한 부여 및 빌드
# 의존성을 미리 받는 단계가 때로는 소스 인식을 방해할 수 있어 통합하여 빌드합니다.
RUN chmod +x ./gradlew
RUN ./gradlew :server:bootJar -x test --no-configuration-cache --no-daemon

# 2. Run Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=build /app/server/build/libs/server-1.0.0.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Xmx${JAVA_MAX_HEAP:-512m} -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
