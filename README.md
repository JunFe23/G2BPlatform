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
  - **물품**: `procurement_contract_flat` / `procurement_contract_grouped` (합쳐서/펼쳐서 보기)
  - **쇼핑몰(제3자단가)**: `shopping_mall_flat` — `GET /api/report/shopping-mall`, 엑셀 `GET /api/report/shopping-mall/excel`
  - **용역**: `service_contract_flat` / `service_contract_grouped`, 공공조달분류 중·소분류 필터
  - **공사**: `construction_contract_flat` / `construction_contract_grouped`
  - **탑인더스트리·탑정보통신 수주현황**: `GET /api/report/top-companies`, 엑셀 다운로드 지원

- **보고서 대시보드** (`ReportDashboardView`)  
  상단 공통 기간 필터(연도 선택 / 기간 직접 지정)를 기준으로 아래 탭을 제공합니다.

  | 탭 | 구현 상태 | 주요 API |
  |----|-----------|----------|
  | **시장현황** | ✅ 실제 API 연동 | `GET /api/report/market` (소스별 다중 선택) |
  | **수요기관별** | ✅ 실제 API 연동 | `GET /api/report/demand-agency-market` |
  | **지역별** | ✅ 실제 API 연동 | `GET /api/report/region-market` |
  | **순위분석** | ✅ 실제 API 연동 + 엑셀 다운로드 | `GET /api/report/rank`, `GET /api/report/rank/excel` |
  | **우수제품** | 🔲 UI 골격 (목업) | — |
  | **민수관리** | 🔲 UI 골격 (목업) | — |

- **엑셀 다운로드**  
  조회 결과를 Apache POI(SXSSFWorkbook 스트리밍)로 생성·다운로드합니다. 금액 컬럼은 `#,##0` 형식을 적용합니다.

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
| **보고서 데이터** | 물품, 쇼핑몰, 용역, 공사, 탑인더스트리·탑정보통신 수주현황, 수주대상 사업탐색, **대시보드** |
| **API 데이터** | 물품, 용역, 공사, 3자단가, 탑인더스트리·탑정보통신 수주현황, 수주대상 사업탐색 |
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
│   │   ├── controller/         # ReportDataController, DataController, AuthController 등
│   │   ├── mapper/             # MyBatis Mapper 인터페이스
│   │   ├── service/            # ReportDataService, ShoppingMallService 등
│   │   ├── scheduler/
│   │   └── ...
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── db/migration/       # Flyway 마이그레이션 SQL
│   │   └── org/example/g2bplatform/mapper/*.xml   # MyBatis SQL XML
│   └── build.gradle
├── frontend/                   # Vue 3 + Vite SPA
│   ├── src/
│   │   ├── views/              # ReportGoodsView, ReportShoppingMallView,
│   │   │                       # ReportServicesView, ReportConstructionsView,
│   │   │                       # ReportDashboardView, TopContractsView,
│   │   │                       # TopContractsReportView 등
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
- **MySQL 8.x**  
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
- Swagger UI: http://localhost:8080/swagger-ui.html

### Docker Compose

```bash
docker compose up --build
```

코드 변경이 컨테이너에 반영되지 않으면 `docker compose build --no-cache` 후 다시 올리는 것을 고려합니다.

---

## 주요 API 개요

| 용도 | 메서드 | 경로 |
|------|--------|------|
| 보고서 물품 목록 / 엑셀 / 저장 | GET / GET / PATCH | `/api/report/procurements`, `/excel`, `/saved` |
| 보고서 용역 목록 / 엑셀 / 저장 | GET / GET / PATCH | `/api/report/services`, `/excel`, `/saved` |
| 보고서 공사 목록 / 엑셀 / 저장 | GET / GET / PATCH | `/api/report/constructions`, `/excel`, `/saved` |
| 보고서 쇼핑몰 목록 / 엑셀 | GET | `/api/report/shopping-mall`, `/api/report/shopping-mall/excel` |
| 탑 수주현황 목록 / 엑셀 | GET | `/api/report/top-companies`, `/api/report/top-companies/excel` |
| 대시보드 시장현황 | GET | `/api/report/market` (`from`, `to`, `sources`) |
| 대시보드 수요기관별 집계 | GET | `/api/report/demand-agency-market` (`dateBasis`, `from`, `to`, `topN`, `dataSource`) |
| 대시보드 지역별 집계 | GET | `/api/report/region-market` (`from`, `to`, `dataSource`) |
| 대시보드 순위분석 | GET | `/api/report/rank` (`dateBasis`, `from`, `to`, `topN`, `dataSource`, `rankType`) |
| 대시보드 순위분석 엑셀 | GET | `/api/report/rank/excel` (`dateBasis`, `from`, `to`, `dataSource`, `rankType`) |
| 원시·통합 조회 | GET 등 | `/api/data`, `/api/data/excel`, … |

### 파라미터 상세

**`dataSource`** (수요기관별·지역별·순위분석 공통)

| 값 | 설명 |
|----|------|
| `all` | `procurement_contract_summary` + `shopping_mall_flat` UNION 집계 |
| `procurement` | `procurement_contract_summary` 단독 |
| `shopping_mall` | `shopping_mall_flat` 단독 |
| `service` | `service_contract_grouped` 단독 |
| `construction` | `construction_contract_grouped` 단독 |

**`rankType`** (순위분석)

| 값 | 정렬 기준 |
|----|-----------|
| `AMOUNT` | 총 계약금액 내림차순 (기본) |
| `COUNT` | 계약건수 내림차순 |
| `AVG` | 평균 단가 내림차순 |

**`dateBasis`** (수요기관별·순위분석)

| 값 | 적용 컬럼 |
|----|-----------|
| `FINAL` | 최종계약일 (`final_contract_date` / `final_ref_date`) |
| `FIRST` | 최초계약일 (`first_contract_date` / `first_ref_date` / `initial_contract_date`) |

---

## 순위분석 탭 구현 상세

보고서 데이터 > 대시보드 > **순위분석** 탭은 시장에서 업체별 조달 실적을 순위로 보여주는 기능입니다.

### 필터
- **데이터 구분**: 물품+3자단가 / 물품 / 3자단가 / 용역 / 공사
- **기간 기준**: 최종계약일 / 최초계약일
- **순위 기준 탭**: 계약금액 순위 / 계약건수 순위 / 평균단가 순위
- **기간**: 페이지 상단 공통 기간 필터(연도·기간 지정) 연동

### 표시 내용
- **TOP 10 리스트**: 선택된 순위 기준의 상위 10개 업체
- **종합 순위표**: 순위·업체명·총 계약금액·계약건수·평균 단가·금액 점유율
- **엑셀 다운로드**: 현재 필터 조건의 전체 순위 (`GET /api/report/rank/excel`)  
  엑셀 컬럼: 순위 / 업체명 / 사업자등록번호 / 총 계약금액 / 계약건수 / 평균 단가 / 금액 점유율 / 건수 점유율

### 참조 테이블

| 데이터 소스 | 테이블 | 금액 컬럼 | 최종계약일 | 최초계약일 |
|-------------|--------|-----------|-----------|-----------|
| 물품 | `procurement_contract_summary` | `final_contract_amount` | `final_contract_date` | `first_contract_date` |
| 3자단가 | `shopping_mall_flat` | `supply_amount` | `ref_date` | `first_ref_date` |
| 용역 | `service_contract_grouped` | `final_contract_amount_sum` | `final_contract_date` | `initial_contract_date` |
| 공사 | `construction_contract_grouped` | `final_contract_amount_sum` | `final_contract_date` | `initial_contract_date` |

---

## 라이선스

(프로젝트에 맞게 추가)
