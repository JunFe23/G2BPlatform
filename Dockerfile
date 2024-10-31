# Java 21을 지원하는 슬림 버전 이미지 사용
FROM openjdk:21-jdk-slim

# 프로젝트의 JAR 파일을 컨테이너 내부로 복사
COPY ./build/libs/*.jar app.jar

# 컨테이너 시작 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
