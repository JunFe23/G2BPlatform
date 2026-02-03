# 빌드 단계: 소스에서 JAR 생성 (docker compose up --build 시 로컬 JAR 없어도 동작)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# 실행 단계
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/G2BPlatform-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
