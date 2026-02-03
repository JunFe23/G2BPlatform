# 멀티스테이지 빌드: Gradle로 빌드 후 실행
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/G2BPlatform-0.0.1-SNAPSHOT.jar app.jar
COPY newrelic newrelic
ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-javaagent:newrelic/newrelic.jar", "-jar", "app.jar"]
