# G2B Platform

G2B(Government-to-Business) 정부 조달 계약 정보를 수집·통합·조회할 수 있는 풀스택 플랫폼입니다.  
백엔드는 Spring Boot, 프론트엔드는 Vue 3 + Vite로 구성되어 있으며, 계약 데이터 자동 다운로드·DB 적재·REST API 조회·Excel/Google Sheets 연동을 지원합니다.

---

## 주요 기능

- **계약 데이터 자동 다운로드**  
  외부 G2B 시스템에서 계약 정보를 주기적으로 자동 다운로드합니다.

- **데이터 통합 및 DB 적재**  
  다운로드·가공한 데이터를 MySQL에 저장하고, 저장 프로시저(ETL) 등으로 통합·적재합니다.

- **계약 데이터 조회 API (API 데이터 화면)**  
  물품·공사·용역·탑 수주현황 등을 조건별로 검색하는 REST API와 화면을 제공합니다.  
  (API 데이터 메뉴의 **3자단가** 화면은 전용 API 연동 전이며, 개발 중 표시 상태일 수 있습니다.)

- **보고서 데이터**  
  집계·보고용 테이블(flat/grouped/summary 등)을 기준으로 목록·엑셀·저장 여부를 제공합니다.  
  - **물품**: `procurement_contract_flat` / `procurement_contract_grouped` (합쳐서/펼쳐서), `procurement_contract_summary`는 물품 보고서 목록에서 사용하지 않음  
  - **쇼핑몰(제3자단가)**: `shopping_mall_flat` — `GET /api/shopping-mall/flat`  
  - **용역**: `service_contract_flat` / `service_contract_grouped`, 공공조달분류 중·소분류 필터 등  
  - **공사**: `construction_contract_flat` / `construction_contract_grouped`  
  - **대시보드** (`ReportDashboardView`):  
    - **수요기관별 / 지역별**: 실제 API 연동 — `dataSource`로 물품+3자단가(`all`)·물품·3자단가·용역·공사 선택, 기간은 상단 필터  
    - **시장현황 / 순위분석 / 우수제품**: 일부는 목업·UI만 구성 (순위분석은 데이터·기간 기준 필터 UI 포함, API 연동 예정)  
    - **민수관리**: 수기 계약 등록 UI·테이블 골격만 두고 목업 데이터 제거, 백엔드·테이블 설계 후 연동 예정  
  - **연차계약 목록**(시장현황): 장기·연차 계약 예시 목업 — 분류는 **물품·용역·공사** 중심(3자단가는 장기 연차 목록에 포함하지 않음)

- **엑셀보내기**  
  조회 결과를 Apache POI(SXSSF 등)로 생성·다운로드합니다. 금액 컬럼은 `#,##0` 형식 등을 적용합니다.

- **Google Sheets 연동**  
  계약 데이터를 Google Sheets로 보내 협업·시각화에 활용할 수 있습니다.

- **인증·권한**  
  로그인/회원가입, JWT 기반 인증, 관리자(사용자 관리·원시 CSV import) 기능을 지원합니다.

- **스케줄링**  
  Spring Scheduling으로 데이터 다운로드·적재 작업을 자동 실행합니다.

---

## 웹 메뉴 구조 (요약)

| 상위 메뉴 | 하위 |
|-----------|------|
| **보고서 데이터** | 물품, 쇼핑몰, 용역, 공사, 대시보드, 탑인더스트리·탑정보통신 **수주현황(보고서)** |
| **API 데이터** | 물품, 용역, 공사, 3자단가, 탑인더스트리·탑정보통신 **수주현황(API)** |
| **수주대상 사업탐색** | — |
| **관리자** | 회원관리, CSV 업로드 |

사이드바 메뉴가 길 경우 세로 스크롤로 전체 항목을 볼 수 있습니다.

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
│   │   ├── controller/         # DataController, ReportDataController, ShoppingMallController, AuthController 등
│   │   ├── mapper/             # MyBatis XML
│   │   ├── service/
│   │   ├── scheduler/
│   │   └── ...
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── org/example/g2bplatform/mapper/*.xml
│   └── build.gradle
├── frontend/                   # Vue 3 + Vite SPA
│   ├── src/
│   │   ├── views/              # ReportGoodsView, ReportShoppingMallView, ReportServicesView,
│   │   │                       # ReportConstructionsView, ReportDashboardView, TopContractsView,
│   │   │                       # TopContractsReportView, LegacySidebarLayout 등
│   │   ├── router/
│   │   └── stores/
│   ├── package.json
│   └── vite.config.js          # /api → 백엔드 프록시
├── docker-compose.yml
└── README.md
```

---

## 시작하기

### 요구 사항

- **JDK 21** 이상  
- **Gradle 8** 이상 (백엔드)  
- **Node.js 18+** 및 npm (프론트엔드)  
- **MySQL** 8.x  
- **Google Sheets API**: Service Account 자격 증명 파일 (`top-service-account.json` 등, 기능 사용 시)

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
   google.sheets.key-path=top-service-account.json
   ```

3. **프론트엔드**  
   개발 시 API는 `vite.config.js`의 프록시로 백엔드(예: `http://localhost:8080`)로 전달됩니다.

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

- Service Account JSON·`application.properties` 마운트는 `docker-compose.yml`을 참고합니다.

```bash
docker compose up --build
```

코드 변경이 컨테이너에 반영되지 않으면 `docker compose build --no-cache` 후 다시 올리는 것을 고려합니다.

---

## 주요 API 개요

| 용도 | 메서드 | 경로 |
|------|--------|------|
| 보고서 물품 목록/건수 | GET | `/api/report/procurements` |
| 보고서 물품 엑셀 | GET | `/api/report/procurements/excel` |
| 보고서 물품 저장 | PATCH | `/api/report/procurements/saved` |
| 보고서 용역 | GET / GET / PATCH | `/api/report/services`, `/excel`, `/saved` |
| 보고서 공사 | GET / GET / PATCH | `/api/report/constructions`, `/excel`, `/saved` |
| 쇼핑몰 flat 목록 | GET | `/api/shopping-mall/flat` |
| 대시보드 수요기관별 집계 | GET | `/api/report/demand-agency-market` (`dateBasis`, `from`, `to`, `topN`, **`dataSource`**) |
| 대시보드 지역별 집계 | GET | `/api/report/region-market` (`from`, `to`, **`dataSource`**) |
| 기타 보고서(목업/확장용) | GET | `/api/report/market`, `/agency`, `/region`, `/rank`, `/excellent`, `/private` 등 |
| 원시·통합 조회(Data) | GET 등 | `/api/data`, `/api/data/excel`, … |

**`dataSource`** (수요기관별·지역별): `procurement` | `shopping_mall` | `all` | `service` | `construction`  
- `all`: `procurement_contract_summary`와 `shopping_mall_summary`를 UNION 후 집계 (문자셋/collation은 쿼리·스키마에서 맞춤)  
- `service` / `construction`: 각각 `service_contract_grouped`, `construction_contract_grouped` 집계  

물품 보고서 목록의 연도·월·기간 검색은 **flat/grouped** 토글에 따라 기간 컬럼이 달라지며, summary 단일 테이블만 쓰는 경로는 제거된 상태입니다.

---

## 라이선스

(프로젝트에 맞게 추가)
