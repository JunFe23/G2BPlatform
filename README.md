# Narajangteo Spring Project

## 프로젝트 개요
이 프로젝트는 나라장터 시스템을 위한 스프링 애플리케이션입니다. 이 애플리케이션은 Gradle 빌드 시스템을 사용하고 있으며, JPA를 활용한 데이터베이스 연동 기능을 포함하고 있습니다.

## 사용된 기술
- **Java 21**
- **Spring Boot**
    - Spring Web
    - Spring Data JPA
    - Thymeleaf
- **Gradle**
- **Database**
  - mysql
- **Thymeleaf**: 서버 사이드 템플릿 엔진
- **IntelliJ IDEA**: 개발 환경
- **Git & GitHub**: 소스 코드 관리

## 설정 및 실행 방법
1. **프로젝트 클론**:
    ```sh
    git clone https://github.com/yourusername/narajangteo-spring-project.git
    cd narajangteo-spring-project
    ```

2. **애플리케이션 실행**:
    ```sh
    ./gradlew bootRun
    ```

3. **브라우저에서 확인**:
    - 브라우저를 열고 `http://localhost:8080`에 접속합니다.

## 주요 기능
- **대용량 데이터 다운로드 기능**
  - 계약현황에 대한 물품조회
  - 계약현황에 대한 물품세부조회
  - 계약현황에 대한 물품변역이력조회
  - 나라장터검색조건에 의한 계약현황 물품조회
  - 계약현황에 대한 공사조회
  - 계약현황에 대한 용역조회
  - 나라장터검색조건에 의한 계약현황 물품조회