# 1. Build Stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# 전체 소스 복사 (공통 모듈 shared가 필요하므로 전체 복사)
COPY . .

# gradlew 실행 권한 부여 및 서버 모듈 빌드
RUN chmod +x ./gradlew
RUN ./gradlew :server:bootJar -x test

# 2. Run Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=build /app/server/build/libs/server-1.0.0.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행 (Render의 PORT 환경 변수를 반영하도록 설정)
ENTRYPOINT ["java", "-Xmx512m", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
