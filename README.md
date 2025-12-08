# G2B Platform 📊

G2B Platform은 정부 조달(G2B, Government-to-Business) 계약 정보를 수집·통합하여 제공하는 Spring Boot 기반 애플리케이션입니다.  
외부 시스템에서 계약 데이터를 자동으로 다운로드하고, 데이터베이스에 적재한 뒤, 이를 조회·필터링할 수 있는 API를 제공합니다.  
또한 Google Sheets로의 내보내기와 Excel 리포트 생성 기능을 통해, G2B 계약 데이터를 보다 효율적으로 분석·관리할 수 있도록 돕습니다.

수작업으로 분산된 G2B 계약 정보를 모으고 정리하던 문제를, 자동화된 중앙화 플랫폼으로 해결하는 것을 목표로 합니다.

## 🚀 주요 기능 (Key Features)

- **계약 데이터 자동 다운로드 🤖**  
  - 외부 G2B 시스템으로부터 계약 정보를 주기적으로 자동 다운로드합니다.

- **데이터 통합 및 적재 ⚙️**  
  - 다운로드한 데이터를 저장 프로시저 등을 활용해 데이터베이스에 통합·적재합니다.

- **데이터 조회 및 필터링 API 제공 🔍**  
  - 다양한 조건으로 계약 데이터를 검색·필터링할 수 있는 REST API를 제공합니다.

- **Excel 내보내기 📈**  
  - 조회된 계약 데이터를 기반으로 Excel 파일을 생성하여 분석·보고용으로 활용할 수 있습니다.

- **Google Sheets 연동 ☁️**  
  - 계약 데이터를 Google Sheets로 내보내어, 협업 및 시각화에 활용할 수 있습니다.

- **전역 예외 처리 🛡️**  
  - 공통 예외 처리(Global Exception Handling) 구조를 통해 일관된 에러 응답을 제공합니다.

- **보안 설정 관리 🔑**  
  - Google Sheets API 키 등 민감한 설정 정보를 안전하게 관리합니다.

- **스케줄링 기반 자동화 ⏰**  
  - Spring Scheduling을 사용하여 데이터 다운로드 및 적재 작업을 자동으로 수행합니다.

## 🛠️ 기술 스택 (Tech Stack)

- **Backend**
  - Java ☕
  - Spring Boot 🚀
  - Spring Data JPA
  - Spring WebFlux
  - Spring OAuth2 Client
  - Spring Security
  - MyBatis
  - Log4j2
  - Jackson
  - Lombok
  - Reactor Netty

- **Database**
  - MySQL 🐬

- **외부 API 연동**
  - Google Sheets API 📝

- **빌드 도구**
  - Gradle ⚙️

- **유틸리티**
  - Apache POI (Excel 생성용) 📊

- **컨테이너**
  - Docker 🐳
  - Docker Compose

## 📦 시작하기 (Getting Started)

### 필수 구성 요소 (Prerequisites)

- JDK 21 이상
- Gradle 8 이상
- Docker 및 Docker Compose (Docker로 배포할 경우)
- MySQL 데이터베이스
- Google Sheets API에 접근 가능한 Google Cloud Service Account
- Google Cloud Service Account 자격 증명이 담긴 `top-service-account.json` 파일

### 설치 (Installation)

1. **레포지토리 클론**

    ```bash
    git clone <repository_url>
    cd g2b-platform
    ```

2. **애플리케이션 설정**

    - `backend/src/main/resources/` 경로에 `application.properties` 또는 `application.yml` 파일을 생성합니다.
    - 데이터베이스 연결 정보를 설정합니다.

    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/g2b?serverTimezone=UTC
    spring.datasource.username=<your_db_username>
    spring.datasource.password=<your_db_password>
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.hibernate.ddl-auto=update
    ```

    - Google Sheets API 키 경로를 설정합니다.

    ```properties
    google.sheets.key-path=top-service-account.json
    ```

    - 그 외 필요한 애플리케이션 속성들을 추가로 설정합니다.

3. **백엔드 애플리케이션 빌드**

    ```bash
    cd backend
    ./gradlew build
    ```

4. **프론트엔드 애플리케이션 빌드**

    ```bash
    cd frontend
    npm install
    npm run build
    ```

### 로컬 실행 (Running Locally)

1. **MySQL 데이터베이스 실행**

   - MySQL 인스턴스가 실행 중이며 접속 가능한 상태인지 확인합니다.

2. **백엔드 애플리케이션 실행**

    ```bash
    cd backend
    ./gradlew bootRun
    ```

3. **프론트엔드 애플리케이션 실행**

   - `npm run build`로 생성된 빌드 파일을 웹 서버(예: `nginx`, `http-server`)를 통해 서빙합니다.

4. **Docker Compose로 실행**

   - 프로젝트 루트 디렉터리에 `top-service-account.json` 파일을 위치시킵니다.
   - 다음 명령으로 컨테이너를 빌드 및 실행합니다.

    ```bash
    docker-compose up --build
    ```

## 💻 프로젝트 구조 (Project Structure)

```text
📂 g2b-platform
├── 📂 backend
│   ├── 📂 src
│   │   ├── 📂 main
│   │   │   ├── 📂 java
│   │   │   │   ├── 📂 org
│   │   │   │   │   ├── 📂 example
│   │   │   │   │   │   ├── 📂 g2bplatform
│   │   │   │   │   │   │   ├── 📂 config
│   │   │   │   │   │   │   ├── 📂 controller
│   │   │   │   │   │   │   ├── 📂 DTO
│   │   │   │   │   │   │   ├── 📂 exception
│   │   │   │   │   │   │   ├── 📂 mapper
│   │   │   │   │   │   │   ├── 📂 model
│   │   │   │   │   │   │   ├── 📂 repository
│   │   │   │   │   │   │   ├── 📂 scheduler
│   │   │   │   │   │   │   ├── 📂 service
│   │   │   │   │   │   │   ├── G2BPlatformApplication.java
│   │   │   ├── 📂 resources
│   │   │   │   ├── application.properties
│   │   │   │   ├── mapper/*.xml
│   ├── build.gradle
│   └── ...
├── 📂 frontend
│   ├── 📂 src
│   │   ├── ...
│   ├── package.json
│   └── ...
├── docker-compose.yml
├── top-service-account.json
└── README.md
