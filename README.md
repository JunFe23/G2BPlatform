# G2B Platform

G2B(Government-to-Business) 정부 조달 계약 정보를 수집·통합·조회할 수 있는 풀스택 플랫폼입니다.  
백엔드는 Spring Boot, 프론트엔드는 Vue 3 + Vite로 구성되어 있으며, 계약 데이터 자동 다운로드·DB 적재·REST API 조회·Excel/Google Sheets 연동을 지원합니다.

---

## 주요 기능

- **계약 데이터 자동 다운로드**  
  외부 G2B 시스템에서 계약 정보를 주기적으로 자동 다운로드합니다.

- **데이터 통합 및 DB 적재**  
  다운로드·가공한 데이터를 MySQL에 저장하고, 저장 프로시저 등으로 통합·적재합니다.

- **계약 데이터 조회 API**  
  물품·공사·용역 계약을 조건별로 검색·필터링하는 REST API를 제공합니다.

- **보고서 데이터**  
  - **물품 / 공사 / 용역**: `first_contract_date` 기준 연도·월·기간 검색, 페이징, 엑셀 다운로드, 저장 여부 관리  
  - **대시보드**: 시장현황, 수요기관별·지역별 분석, 순위분석, 우수제품, 민수관리 등 집계 데이터

- **Excel 내보내기**  
  조회 결과를 Excel 파일로 생성해 다운로드합니다.

- **Google Sheets 연동**  
  계약 데이터를 Google Sheets로 내보내 협업·시각화에 활용할 수 있습니다.

- **인증·권한**  
  로그인/회원가입, JWT 기반 인증, 관리자(사용자 관리·원시 데이터 import) 기능을 지원합니다.

- **스케줄링**  
  Spring Scheduling으로 데이터 다운로드·적재 작업을 자동 실행합니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.3, Spring Data JPA, Spring WebFlux, Spring Security, OAuth2 Client, MyBatis, Log4j2, Jackson, Lombok, Apache POI, JWT(jjwt), Springdoc OpenAPI(Swagger) |
| **Database** | MySQL 8.x |
| **Frontend** | Vue 3, Vite 6, Vue Router, Pinia, Axios, DataTables, jQuery |
| **빌드** | Backend: Gradle 8+ / Frontend: npm |
| **배포** | Docker, Docker Compose |

---

## 프로젝트 구조

```
G2BPlatform/
├── backend/                    # Spring Boot API
│   ├── src/main/java/.../g2bplatform/
│   │   ├── config/
│   │   ├── controller/         # DataController, ReportDataController, AuthController 등
│   │   ├── mapper/             # MyBatis (ProcurementContractSummaryMapper 등)
│   │   ├── service/
│   │   ├── scheduler/
│   │   └── ...
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── mapper/*.xml
│   └── build.gradle
├── frontend/                   # Vue 3 + Vite SPA
│   ├── src/
│   │   ├── views/              # ReportGoodsView, ReportConstructionsView, ReportServicesView, ReportDashboardView 등
│   │   ├── router/
│   │   └── stores/
│   ├── package.json
│   └── vite.config.js          # /api → localhost:8080 프록시
├── docker-compose.yml          # api:8080, web:3000
└── README.md
```

---

## 시작하기

### 요구 사항

- **JDK 21** 이상  
- **Gradle 8** 이상 (백엔드)  
- **Node.js 18+** 및 npm (프론트엔드)  
- **MySQL** 8.x  
- **Google Sheets API**: Service Account 자격 증명 파일 (`top-service-account.json` 등)

### 설정

1. **레포 클론**

   ```bash
   git clone <repository_url>
   cd G2BPlatform
   ```

2. **백엔드 설정**  
   `backend/src/main/resources/` 에 `application.properties` (또는 `application.yml`)를 두고 DB·기타 설정을 넣습니다.

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/g2b?serverTimezone=UTC
   spring.datasource.username=<username>
   spring.datasource.password=<password>
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   # Google Sheets API 키 경로 (필요 시)
   google.sheets.key-path=top-service-account.json
   ```

3. **프론트엔드**  
   개발 시 API는 `vite.config.js`의 프록시로 `http://localhost:8080`으로 전달됩니다. 별도 설정 없이 백엔드만 8080에서 띄우면 됩니다.

### 로컬 실행

**백엔드**

```bash
cd backend
./gradlew bootRun
```

**프론트엔드** (별도 터미널)

```bash
cd frontend
npm install
npm run dev
```

- 프론트: http://localhost:5173 (Vite 기본)  
- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html (Springdoc 사용 시)

### Docker Compose

- 프로젝트 루트에 Service Account JSON을 두거나, `docker-compose.yml`의 volume 경로에 맞춰 `config/top-service-account.json` 등을 준비합니다.  
- `application.properties`를 컨테이너에 마운트하도록 되어 있으면, 호스트의 해당 파일을 준비합니다.

```bash
docker-compose up --build
```

- API: 8080  
- Web(프론트 빌드 서빙): 3000  

---

## 주요 API 개요

| 용도 | 메서드 | 경로 예시 |
|------|--------|-----------|
| 보고서 물품 목록/건수 | GET | `/api/report/goods` |
| 보고서 물품 엑셀 | GET | `/api/report/goods/excel` |
| 보고서 물품 저장 여부 | PATCH | `/api/report/goods/saved` |
| 보고서 대시보드(시장/수요기관/지역 등) | GET | `/api/report/market`, `/api/report/agency`, `/api/report/region`, `/api/report/demand-agency-market`, `/api/report/region-market` 등 |
| 계약 데이터(물품/공사/용역) | GET | `/api/data/...` (DataController) |

보고서 물품의 **연도·월·기간 검색**은 DB 테이블 `procurement_contract_summary`의 **`first_contract_date`** 컬럼 기준으로 동작합니다.

---

## 라이선스

(프로젝트에 맞게 추가)
