FROM eclipse-temurin:17-jdk

WORKDIR /app

# 🟡 JAR와 함께 deploy.sh도 복사
COPY build/libs/deploy-0.0.1-SNAPSHOT.jar app.jar
COPY deploy.sh ./deploy.sh

# 🟡 실행 권한 부여 (Linux 기반 컨테이너이므로 필요함)
RUN chmod +x ./deploy.sh

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
