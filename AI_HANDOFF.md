# AI_HANDOFF — 시장데이터(공사/용역) 통합 ETL & 보고서

> 목적: Claude Code 사용량 제한 시 **Codex 등 다른 AI가 그대로 이어서 개발**할 수 있도록 현재 상태·수정 파일·남은 이슈·다음 작업을 자족적으로 정리.
> 작성 시각: 2026-06-23 / 작성 주체: Claude Code
> Codex 업데이트: 2026-06-23 13시대 — Jira 정리 + G2B-26 통합 조회 API/매퍼 구현 진행
> Codex 실행 메모: 2026-06-23 13:17 — 사용자 테스트용 로컬 앱 기동. Docker `g2bplatform-api-1`은 중지했고, 현재 작업트리 백엔드를 `8080`, 프론트를 `5173`에 띄움.
> Codex 업데이트: 2026-06-23 13:22 — G2B-27 공사/용역 보고서 화면을 통합 API(`/api/report/market-contracts`)에 연결.
> Codex 업데이트: 2026-06-23 13:45 — G2B-31 시장데이터 메뉴명 변경 및 특정품목 통합 화면 검색필터 개편 구현/검증 완료. 로컬 백엔드 `8080`, 프론트 `5173` 재기동.
> Claude 업데이트: 2026-06-24 — **운영 RDS raw 적재 완료** + **운영 ETL(flat/grouped) 실행** (상세 §8).
> 우선 `CLAUDE.md`(프로젝트 컨텍스트)도 함께 읽을 것.

---

## 0. 한눈에 보기

- **작업 목표**: 기존에 분리돼 있던 공사(`construction_contract_*`)·용역(`service_contract_*`) 보고서 테이블/매퍼/뷰를, `task_member_contract_raw`(공사+용역 통합 raw) 기반의 **단일 통합 구조 `market_contract_*`(contract_type으로 공사/용역 구분)** 로 전환.
- **현재 위치**: raw → flat → grouped **ETL 파이프라인 구현·로컬 실행·검증 완료** ✅. 이후 Codex가 **통합 조회 API/매퍼(G2B-26) 구현 및 프론트 공사/용역 화면 연결(G2B-27) 완료**까지 진행. 추가로 `보고서 데이터` 메뉴명 → `시장데이터`, 특정품목 통합 화면 검색필터 개편 구현/검증 완료. 아직 **TOP·시장현황 전환 / 레거시 제거는 미착수**.
- **현재 브랜치**: `feature/G2B-16-specific-item-flat`
  - ⚠️ 브랜치명은 "specific-item"이지만 실제 작업 내용은 **시장데이터(공사/용역) 통합**임. 새 작업 시 적절한 Jira 티켓/브랜치 재정리 고려(작업단위 티켓 규칙).
- **커밋 상태**: 아래 신규/수정 파일들 **아직 커밋 안 함**(워킹트리에 untracked 포함). 커밋·푸시·PR·배포는 **사용자 명시 승인 후에만**.
- **Jira 정리**:
  - `G2B-25` 에픽: 시장데이터 공사/용역 통합 보고서 전환
  - `G2B-16`: 시장데이터 공사/용역 통합 ETL 테이블 및 적재 파이프라인 구축(기존 티켓 업데이트, G2B-25 하위)
  - `G2B-26`: 시장데이터 통합 조회 API 및 MyBatis 매퍼 구축(현재 Codex가 구현)
  - `G2B-27`: 공사/용역 보고서 프론트 통합 API 전환(Codex 구현)
  - `G2B-28`: TOP/시장현황 보고서의 공사·용역 쿼리 통합 테이블 전환
  - `G2B-29`: 공사/용역 레거시 매퍼·서비스·엔드포인트 제거
  - `G2B-30`: 시장데이터 ETL 성능 최적화 및 운영 반영 검증
  - `G2B-31`: 시장데이터 메뉴명 및 특정품목 필터 개편(Codex 구현/검증 완료, Jira 상태 `완료`)

---

## 1. 시스템 환경 (그대로 사용 가능)

| 항목 | 값 |
|------|----|
| repo 경로 | `/Users/junfe/Desktop/G2B/G2BPlatform` |
| Backend | Java 21, Spring Boot 3.3, WebFlux + JPA + MyBatis + Flyway + Security(JWT) |
| Frontend | Vue 3, Vite 6, Axios |
| 로컬 DB | MySQL 8 `mysql -uroot -h127.0.0.1 g2b` (스키마 `g2b`, 비번 없음) |
| 백엔드 기동 | 현재 **Docker Compose로 8080 기동 중**(`g2b-api-1`). `host.docker.internal:3306`로 호스트 MySQL 접속 |
| ETL 트리거 | `POST /api/admin/etl/market-contract` (권한 `ROLE_SUPER_ADMIN`) |

### 2026-06-23 Codex 로컬 실행 상태
- 사용자 테스트용으로 Docker API 컨테이너 `g2bplatform-api-1`을 중지하고 현재 작업트리 백엔드를 직접 실행:
  - `cd backend && env GRADLE_USER_HOME=.gradle ./gradlew bootRun --args='--spring.profiles.active=local --server.port=8080'`
- 프론트 dev 서버 실행:
  - `cd frontend && env PATH=/Users/junfe/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin:$PATH npm run dev -- --host 127.0.0.1`
  - 접속 URL: `http://127.0.0.1:5173/`
- 기존 Docker web(`g2bplatform-web-1`, `3000`)은 그대로 떠 있음. 현재 작업트리 프론트 테스트는 반드시 `5173` 사용.
- ⚠️ `G2B-27` 프론트 전환 전이라 공사/용역 화면은 아직 기존 `/api/report/constructions`, `/api/report/services`를 호출한다. 신규 통합 API는 백엔드에 구현되어 있지만 화면 연결은 다음 티켓.

### 2026-06-23 Codex 분석: G2B-27 프론트 연결안
- 대상 화면:
  - `frontend/src/views/ReportServicesView.vue` — 보고서 데이터 - 용역
  - `frontend/src/views/ReportConstructionsView.vue` — 보고서 데이터 - 공사
- 현재 문제:
  - 두 화면은 아직 기존 API(`/api/report/services`, `/api/report/constructions`)를 호출한다.
  - V21 적용 후 기존 `service_contract_*`, `construction_contract_*` 테이블은 제거되므로 기존 API는 데이터 조회 실패 가능성이 높다.
- 연결 원칙:
  - 합쳐서 보기 토글 ON → `market_contract_grouped`
  - 풀어서 보기 토글 OFF → `market_contract_flat`
  - 백엔드 신규 API는 `grouped=true|false`로 이를 이미 분기한다.
- 최소 변경안:
  - 두 화면 모두 `API_BASE = '/api/report/market-contracts'`로 변경
  - 공사 화면 `buildParams()`에 `contractType: 'construction'` 추가
  - 용역 화면 `buildParams()`에 `contractType: 'service'` 추가
  - 엑셀 다운로드는 `buildParams(false)`를 그대로 쓰므로 자동으로 `contractType` 포함
  - saved 토글 payload에도 `contractType` 추가
  - 공사 grouped saved는 현재 groupKey만 보내는데, 새 grouped PK가 `(contract_type, group_key, vendor_biz_reg_no)`이므로 `vendorBizRegNo`도 같이 보내는 편이 안전
- 백엔드 alias 호환:
  - `MarketContractMapper.xml`이 기존 공사/용역 화면에서 쓰는 필드명(`contractTitle`, `demandAgency`, `demandAgencyName`, `initialContractDate`, `finalContractAmountSum`, `contractDeliveryIntegratedNo` 등)을 함께 반환하도록 작성됨.
  - 단, 신규 통합 테이블에는 `bid_notice_no`, 용역 `start_date/completion_date`, 기존 용역의 세부 계약유형 필드가 없음. 화면에는 빈 값/`service`로 보일 수 있음. 필요하면 후속으로 raw 컬럼을 market flat/grouped에 추가해야 함.

### 자주 쓰는 확인 명령
```bash
# 8080 기동 여부
lsof -iTCP:8080 -sTCP:LISTEN -n -P

# 테이블 건수
mysql -uroot -h127.0.0.1 g2b -N -e "SELECT 'raw',COUNT(*) FROM task_member_contract_raw \
 UNION ALL SELECT 'flat',COUNT(*) FROM market_contract_flat \
 UNION ALL SELECT 'grouped',COUNT(*) FROM market_contract_grouped \
 UNION ALL SELECT 'target_cat',COUNT(*) FROM market_target_category;"

# ETL Job 상태
mysql -uroot -h127.0.0.1 g2b -t -e "SELECT id,status,message,LEFT(error_message,200) err,inserted_count,created_at \
 FROM csv_upload_job WHERE upload_type='market_contract_etl' ORDER BY created_at DESC LIMIT 3;"

# ETL 진행 중인지 (활성 INSERT 확인)
mysql -uroot -h127.0.0.1 g2b -t -e "SELECT id,time,state,LEFT(info,80) info FROM information_schema.processlist \
 WHERE info LIKE 'INSERT INTO market_contract%';"
```

---

## 2. 신규/수정 파일 (이번 작업)

모두 **아직 untracked(커밋 전)**:

| 파일 | 내용 |
|------|------|
| `backend/src/main/resources/db/migration/V21__create_market_contract_tables.sql` | 신규 테이블 3종 생성 + **구 공사/용역 테이블 DROP**. 로컬 Flyway 적용 완료(`flyway_schema_history`에 V21 success=1) |
| `backend/src/main/java/org/example/g2bplatform/service/MarketContractEtlService.java` | raw→flat→grouped ETL. 비동기 Job(`csv_upload_job` 재사용, `upload_type='market_contract_etl'`) |
| `backend/src/main/java/org/example/g2bplatform/controller/MarketContractController.java` | `POST /api/admin/etl/market-contract` ETL 트리거 + `GET/PATCH /api/report/market-contracts*` 통합 조회/엑셀/saved API |
| `backend/src/main/java/org/example/g2bplatform/mapper/MarketContractMapper.java` | Codex 신규. market_contract flat/grouped 조회/카운트/엑셀/saved MyBatis 인터페이스 |
| `backend/src/main/resources/org/example/g2bplatform/mapper/MarketContractMapper.xml` | Codex 신규. `market_contract_flat/grouped` 대상 쿼리. 모든 쿼리에 `contract_type` 필터 적용 |
| `backend/src/main/java/org/example/g2bplatform/service/ReportMarketService.java` | Codex 신규. `contractType` 검증과 grouped/flat 분기 서비스 |
| `frontend/src/views/ReportConstructionsView.vue` | Codex 수정. 공사 보고서 화면을 `/api/report/market-contracts?contractType=construction`으로 연결 |
| `frontend/src/views/ReportServicesView.vue` | Codex 수정. 용역 보고서 화면을 `/api/report/market-contracts?contractType=service`로 연결 |

### V21이 만드는 테이블
- `market_target_category` — 대상 공공조달분류 화이트리스트(**21건** 적재됨). `(contract_type, category_name)` 유니크. flat 적재 시 JOIN 필터로 사용.
- `market_contract_flat` — 계약별 최종 1행. 유니크 `(contract_type, contract_no)`.
- `market_contract_grouped` — 초년도계약번호 묶음(연차계약 집계). 유니크 `(contract_type, group_key, vendor_biz_reg_no)`.
- DROP: `construction_contract_flat/grouped`, `service_contract_flat/grouped` (구조 전환, TOP 데이터 보존 안 함).

### flat 적재 조건 (MarketContractEtlService L70~89)
```
FROM task_member_contract_raw r
JOIN market_target_category c
  ON c.is_active='Y' AND c.contract_type=r.contract_type
 AND c.category_name = r.public_procurement_type   -- ① 대상분류 화이트리스트 매칭
WHERE r.is_final_contract='Y'                        -- ② 계약별 최종(최신 차수) 1행
INSERT IGNORE (contract_type, contract_no)           -- ③ 계약번호당 1행 보장
```
- 금액: `CAST(NULLIF(REPLACE(raw,',',''),'') AS UNSIGNED)` (콤마 제거)
- 날짜: `STR_TO_DATE(CHAR8, '%Y%m%d')`
- `is_long_term`: `new_long_term_type IN ('신규(장기)','장기','계속비')` → Y/N
- `saved='N'` 고정. ETL 재실행 시 기존 `saved='Y'` 행은 **백업→TRUNCATE→재적재→복원** 로직으로 보존(L62~97, L121~128).

### grouped 집계 (L100~119)
- `GROUP BY contract_type, COALESCE(first_year_contract_no, contract_no), vendor_biz_reg_no`
- `initial_contract_amount` = 가장 이른 `first_contract_date` 연차의 최초계약금액 합 (윈도우함수 `MIN() OVER PARTITION`으로 grp_min 산출 후 비교)
- `total_contract_amount_sum` = SUM(total), `contract_count` = COUNT(DISTINCT contract_no)

---

## 3. 현재 DB 상태 (2026-06-23 ETL SUCCESS 직후)

| 테이블 | 건수 |
|--------|------|
| `task_member_contract_raw` | 11,736,823 |
| `market_contract_flat` | **2,672,131** (construction 1,489,972 / service 1,182,159) |
| `market_contract_grouped` | **2,623,994** (construction 1,473,667 / service 1,150,327) |
| `market_target_category` | 21 |

검증 결과: 금액 NULL 0건, 분류 매칭 정상, 연차계약 묶음 정상(공사 multi-year 13,045·최대 8년 / 용역 19,967·최대 12년).

---

## 4. 남은 이슈

1. **원본 날짜 오타 3건** (무시 가능 수준, 보정 불필요)
   - `22910359_5`: first_contract_date `2202-05-25` (→2022 오타), `R25TA00750918`: `2055-07-22` (→2025 오타) 등. 정렬 `first_contract_date DESC` 시 맨 위로 튀는 것만 주의.
2. **grouped INSERT가 약 4.5분(270초) 소요** — 서브쿼리가 `SELECT *` + 윈도우함수로 267만 행을 temp로 materialize. 11:20 Job을 수동 중단(`쿼리과부하`)했던 원인일 수 있음. 운영 적재 전 **경량화 검토** 권장: (a) 서브쿼리에서 필요 컬럼만 SELECT, (b) grp_min을 별도 임시집계 테이블로 분리, (c) `is_active='Y'` 등 인덱스 점검.
3. **통합 조회 API는 구현 및 로컬 HTTP 스모크 테스트 완료**. Gradle `compileJava`, `test` 통과. 현재 소스 기준 Spring Boot를 `--spring.profiles.active=local --server.port=18080`으로 띄워 인증 JWT 포함 호출 검증.
4. **TOP·시장현황 / 레거시 제거 미착수** (§5).
5. **운영(RDS) 미반영** — 로컬에서만 V21+ETL 수행. 서버 배포는 별도 단계(사용자 승인 필요).

---

## 5. 다음 작업 지시 (우선순위 순)

### 단계 3 — 통합 조회 API + 매퍼 (G2B-26, Codex 구현 완료/검증 중)
**기존 특정품목/공사 패턴을 그대로 복제**하는 작업. 참고 파일:
- 컨트롤러: `backend/.../controller/ReportDataController.java` 의 `getReportConstructions`(L618~) / `/constructions/excel`(L653~) / `/constructions/saved`(PATCH)
- 서비스: `backend/.../service/ReportConstructionService.java` (`getList/getCount/streamForExcel/updateSaved`)
- 매퍼: `backend/.../resources/org/example/g2bplatform/mapper/ConstructionContractMapper.xml`
  - select id: `selectFlatList/selectFlatCount/selectFlatListForExport/updateFlatSaved/selectGroupedList/selectGroupedCount/selectGroupedListForExport/updateGroupedSaved`, `<sql id="flatWhere"/>`, `<sql id="groupedWhere"/>`

**Codex 구현 내용**:
1. `MarketContractMapper.java` + `MarketContractMapper.xml` 신설 — 대상 테이블 `market_contract_flat/grouped`, 모든 쿼리에 `contract_type = #{contractType}` 필터 적용.
2. `ReportMarketService` 신설 — `contractType`(`construction|service`) 검증, grouped/flat 분기, 검색조건 처리.
3. `MarketContractController`에 조회 엔드포인트 추가:
   - `GET /api/report/market-contracts?contractType=construction|service&grouped=...&start=...&length=...`
   - `GET /api/report/market-contracts/excel?...`
   - `PATCH /api/report/market-contracts/saved`
   - ⚠️ 기존 대시보드가 이미 `GET /api/report/market`을 사용 중이므로, 충돌 회피를 위해 핸드오프 원안의 `/api/report/market` 대신 `/api/report/market-contracts`로 구현.
4. 응답 포맷: `{ success, data:[...], recordsFiltered }`.
5. 기존 공사/용역 프론트 호환을 위해 공통 alias와 기존 화면 alias를 함께 반환:
   - 공통: `contractNo`, `vendorName`, `contractTitle`, `demandAgencyName`, `publicProcurementCategory`, `contractMethod`, `firstContractDate`, `finalContractDate`, `saved` 등
   - 용역 호환: `contractDeliveryIntegratedNo`, `demandAgency`, `procurementWorkArea`, `publicProcurementCategoryMid`, `initialContractDate`, `finalContractAmountSum` 등
6. 검증:
   - `env GRADLE_USER_HOME=.gradle ./gradlew compileJava` ✅
   - `env GRADLE_USER_HOME=.gradle ./gradlew test` ✅
   - 실제 HTTP API 스모크 테스트 ✅
     - 서버: `env GRADLE_USER_HOME=.gradle ./gradlew bootRun --args='--spring.profiles.active=local --server.port=18080'`
     - `GET /api/report/market-contracts?contractType=construction&grouped=true&start=0&length=2` → `success=true`, `recordsFiltered=1473667`
     - `GET /api/report/market-contracts?contractType=construction&grouped=false&start=0&length=2` → `success=true`, `recordsFiltered=1489972`
     - `GET /api/report/market-contracts?contractType=service&grouped=true&start=0&length=2` → `success=true`, `recordsFiltered=1150327`
     - `/api/**`는 인증 필요. 로컬 기본 JWT secret으로 `super_admin / ROLE_SUPER_ADMIN` 테스트 토큰을 만들어 호출함.

### 단계 4 — 프론트 전환 (G2B-27, Codex 구현 완료)
- `frontend/src/views/ReportConstructionsView.vue`
  - `API_BASE='/api/report/market-contracts'`
  - `CONTRACT_TYPE='construction'`
  - 목록/엑셀 파라미터에 `contractType` 포함
  - saved payload에 `contractType` 포함
  - grouped saved payload에 `vendorBizRegNo` 추가
- `frontend/src/views/ReportServicesView.vue`
  - `API_BASE='/api/report/market-contracts'`
  - `CONTRACT_TYPE='service'`
  - 목록/엑셀/saved payload에 `contractType` 포함
- 검증:
  - `env PATH=/Users/junfe/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin:$PATH npm run build` ✅
  - `GET /api/report/market-contracts?contractType=construction&grouped=true&start=0&length=1` ✅
  - `GET /api/report/market-contracts?contractType=service&grouped=false&start=0&length=1` ✅

### 단계 4-1 — 시장데이터 메뉴명 및 특정품목 필터 개편 (G2B-31, Codex 구현/검증 완료)
요청 계획 기준으로 아래 변경을 적용했다.

수정 파일:
- `frontend/src/views/components/LegacySidebarLayout.vue`
  - 좌측 메뉴 상위 라벨 `보고서 데이터` → `시장데이터`.
- `frontend/src/views/ReportSpecificItemView.vue`
  - 상단 검색필드에서 `사업자번호`, `세부품명번호` 입력 제거.
  - `물품분류명` 검색형 combobox, `물품분류번호` 검색형 combobox, `계약명` 텍스트 입력 추가.
  - 두 combobox는 동일 옵션 객체 `{ itemCategoryNo, itemCategoryName }`를 사용하며 선택 시 서로 자동 세팅.
  - 목록/엑셀 파라미터에 `itemCategoryName`, `contractName` 추가.
  - 화면에서는 더 이상 `vendorBizRegNo`, `detailItemNo`를 전송하지 않음.
- `backend/src/main/java/org/example/g2bplatform/controller/SpecificItemController.java`
  - `/api/specific-item`, `/api/specific-item/excel`에 `itemCategoryName`, `contractName` 파라미터 추가.
  - 신규 옵션 API `GET /api/specific-item/item-categories?q=&limit=30` 추가. `limit`은 1~100으로 제한.
  - 기존 `vendorBizRegNo`, `detailItemNo` 파라미터는 API 호환용으로 유지.
- `backend/src/main/java/org/example/g2bplatform/mapper/SpecificItemMapper.java`
  - `selectItemCategories(q, limit)` 추가.
- `backend/src/main/resources/org/example/g2bplatform/mapper/SpecificItemMapper.xml`
  - `specific_item_flat` distinct `(item_category_no, item_category_name)` 옵션 조회 추가.
  - flat/grouped 공통 where에 `item_category_name LIKE`, `contract_name LIKE` 조건 추가.

검증:
- `cd backend && env GRADLE_USER_HOME=.gradle ./gradlew test` ✅
- `cd frontend && env PATH=/Users/junfe/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin:$PATH npm run build` ✅
  - npm 로그 파일 생성 권한 경고는 있었지만 Vite 빌드는 성공.
- 로컬 8080 재기동 후 인증 토큰 포함 curl ✅
  - `/api/specific-item/item-categories?q=무정전&limit=10` → `무정전전원장치 / 39121011` 반환.
  - `/api/specific-item?itemCategoryName=무정전&start=0&length=1` → flat 조회 `recordsFiltered=24418`.
  - `/api/specific-item?itemCategoryNo=39121011&grouped=true&start=0&length=1` → grouped 조회 `recordsFiltered=18991`.
  - `/api/specific-item?contractName=컴퓨터&grouped=true&start=0&length=1` → grouped 조회 `recordsFiltered=833`.
  - `/api/specific-item/excel?contractName=슈퍼컴퓨터&grouped=true` → `200`, xlsx 응답 7,788 bytes.
- 사용자 테스트용 로컬 서버:
  - Backend: `http://127.0.0.1:8080`
  - Frontend: `http://127.0.0.1:5173`

### 단계 5 — 레거시 제거 (단계 4 동작 확인 후)
아래 구 공사/용역 전용 파일 삭제 + 관련 wiring 제거:
- `backend/.../mapper/ConstructionContractMapper.java` + `.xml`
- `backend/.../mapper/ServiceContractMapper.java` + `.xml`
- `backend/.../service/ReportConstructionService.java`, `ReportServiceContractService.java`
- `ReportDataController.java` 의 `/constructions*`, `/services*` 엔드포인트
- ⚠️ `ProcurementContractSummaryMapper.xml`, `TopCompaniesReportMapper.xml` 도 `construction_contract_/service_contract_` 를 참조함 → **V21에서 이미 DROP된 테이블이라 깨질 수 있음**. 이 두 매퍼가 어디서 쓰이는지(대시보드/순위 보고서) 확인하고 `market_contract_*` 참조로 같이 전환 필요. **단계 3~4 진행 전/중에 영향 범위 먼저 점검할 것.**

---

## 6. ETL 재실행 방법 (필요 시)

```bash
# 1) 백엔드 8080 기동 확인 (Docker Compose). 미기동 시 사용자에게 기동 요청.
# 2) SUPER_ADMIN JWT로 트리거 (UI: RawDataImportView 또는 직접 호출)
curl -X POST http://localhost:8080/api/admin/etl/market-contract \
  -H "Authorization: Bearer <SUPER_ADMIN_JWT>"
# 3) Job 상태 폴링 (§1 명령). flat은 단일 INSERT라 진행 중엔 다른 커넥션에서 0으로 보임(정상).
#    grouped 단계까지 SUCCESS 되면 완료. 전체 약 9분(flat ~4분 + grouped ~4.5분).
```
- ETL은 **멱등**(TRUNCATE 후 재적재). `saved='Y'` 행은 백업·복원되므로 재실행 안전.

---

## 7. 프로젝트 규칙 리마인더

- 커밋: `{JIRA_KEY} {type}: 한 줄 요약`. **커밋·푸시·PR·배포는 사용자 명시 승인 시에만.**
- `application.properties`·service key 등 비밀값 커밋 금지.
- 작업마다 Jira 티켓 생성·에픽 그룹화(담당자 Jun Fe).
- 자세한 ADR·런북은 `docs/` 참고.

---

## 9. G2B-32 — 특정품목 품목별 최초/최종 + 납품·착수완수일 (2026-06-24, 진행 중)

- 티켓: G2B-32 (에픽 G2B-25). 브랜치: `feature/G2B-32-item-first-final-amount` (현재 HEAD=feature/G2B-16에서 분기, master 미머지 작업 위에 스택).
- 설계 결정: **ADR-0004** (`docs/adr/ADR-0004-specific-item-per-item-first-final.md`). ADR-0003 §4를 대체.

### 구현 완료 (코드)
- **V22 마이그레이션** `V22__add_first_item_amount_and_delivery_dates.sql`: `specific_item_flat`에 `first_item_contract_date/first_item_contract_amount/delivery_deadline`, `market_contract_flat`에 `start_date/end_date`.
- **SpecificItemEtlService**: flat 적재 시 `delivery_deadline` 매핑 + `fillFirstItemAmounts()`(품목별 min(change_seq) 첫등장 contract_date/supply_amount UPDATE) + `rebuildGrouped` 산출 변경(최초일 MIN(first_item_contract_date), 최초금액 SUM(first_item_contract_amount), 최종일 MAX(contract_date), 대표품목 FIRST_VALUE(min item_seq)).
- **MarketContractEtlService**: flat에 STR_TO_DATE(start_date/end_date) 매핑.
- **SpecificItemMapper**: flat select에 firstItemContractDate/firstItemContractAmount/deliveryDeadline 추가, `selectFlatTotals` 신설(풀어서 상단 합계). grouped는 컬럼명 동일이라 무변경.
- **SpecificItemController**: 풀어서/합쳐서 모두 `totals` 반환(flat=selectFlatTotals).
- **MarketContractMapper**: flat 공통 컬럼 fragment에 startDate/endDate.
- **프론트**: `ReportSpecificItemView`(풀어서: 최초계약일자=firstItemContractDate, 최초계약금액, 최종계약일자, 납품기한 컬럼 + 상단 합계를 풀어서에도 표시), `ReportConstructionsView`(착수/완수일 컬럼 + COL_SPAN 14→16), `ReportServicesView`(완수일 필드 completionDate→endDate 정정; 착수일 startDate 기존).
- 검증: `compileJava` PASS, 프론트 `npm run build` PASS, ETL 품목별 최초/최종 로직 **로컬 SQL 샘플 검증 통과**(20171008DE1_1, 20191104D04_1 통합건).

### 로컬 E2E 검증 — 완료 ✅ (2026-06-24)
- 백엔드 bootRun 기동 → **Flyway V22 자동 적용**(now at v22, success=1).
- **특정품목 ETL 재실행 SUCCESS**: flat 품목별 최초/최종/납품기한 채움(최초금액 870,712/870,753, 납품기한 870,671), grouped 합계·대표품목 정상. 샘플(20171008DE1_1 3품목, 20191104D04 장기그룹 최초합 145,092,170/최종합 343,180,170) 검증.
- **API 스모크**: 풀어서/합쳐서 신규 필드 + totals(풀어서에도) 반환 확인.
- **공사/용역 start/end**: market_contract_flat backfill(start 2,652,953/end 2,672,129) + API startDate/endDate 반환 확인.
- ⚠️ ETL 버그 2건 발견·수정: ① flat INSERT IGNORE라 기존행 delivery_deadline 미채움 → backfill UPDATE 추가. ② supply_amount_raw 더티값('개')에 strict CAST 에러 → REGEXP 가드(숫자/8자리날짜만 변환). market start/end도 동일 가드.

### 운영 적재 — 완료 ✅ (2026-06-24)
- RDS t4g.large 임시 상향 상태에서 **검증된 SQL을 EC2 경유 직접 실행**(코드 미배포라 앱 ETL 대신 SQL): V22 ALTER + Flyway 기록(checksum 1550333026) + flat 신규컬럼 backfill + grouped 재집계 + market start/end backfill. ~18분, 정상.
- 검증: specific_item_flat 885,340(최초금액 885,297/납품기한 885,254), grouped 438,743, market start 2,652,953/end 2,672,129. Flyway V22 success=1.
- EC2·로컬 임시파일 정리 완료.

### 배포 완료 ✅ (2026-06-25)
- 커밋·PR: PR #19(G2B-32→feature/G2B-16) 머지, PR #20(feature/G2B-16→master) 머지. master 최신.
- **서버 배포 완료**: EC2 deploy 브랜치에 origin/feature/G2B-16 머지(f4173f8) → `docker compose build` + `up -d` → g2b-api-1/web-1 재기동. Flyway 23개 검증 통과(V22 재실행 없음, 수동 기록 checksum 1550333026 일치), API 신규필드/합계 반환 확인.
- Jira: G2B-26·27·30·32 완료. G2B-28(TOP/시장현황 — 구 테이블 참조 리스크)·G2B-29(레거시 제거 — 구 화면 라이브) 미완료.

### ⚠️ 남은 일
1. **RDS를 micro로 복귀** — 적재 끝났으니 사용자 콘솔에서 하향(비용). (현재 t4g.large)
2. G2B-28(TOP/시장현황 전환, 운영 오류 리스크 우선 점검), G2B-29(레거시 제거) — 에픽 잔여.

---

## 10. G2B-33 — 특정품목 보기 최종계약금액 컬럼·순서 정정 (+엑셀) (2026-06-25)

- 티켓: G2B-33 (에픽 G2B-25). 브랜치: `feature/G2B-33-final-amount-column` (master에서 분기).
- 요구: 풀어서 보기 최종계약일자 뒤 '최종계약금액' 컬럼 추가 / 합쳐서 보기 순서를 최초일·최초금액(합계)·최종일·최종금액(합계)로 정정 / 엑셀에도 반영.

### 변경 (frontend `ReportSpecificItemView.vue`)
- 풀어서: 최종계약일자(`contractDate`) 뒤 **최종계약금액(`supplyAmount`)** 컬럼 추가. colspan 28→29.
- 합쳐서: 순서 `firstContractDate → initialContractAmount → lastContractDate → totalSupplyAmount` (헤더/바디 동일), 라벨 '최초계약금액(합계)'.

### 변경 (backend `SpecificItemController.writeExcel`)
- 합쳐서 헤더/셀 순서 정정(최초일·최초금액합·최종일·최종금액합).
- 풀어서 엑셀을 **화면과 일치**시킴(G2B-32 신규컬럼 누락분 보강): 컬럼을 `최초계약일자(firstItemContractDate)·최초계약금액(firstItemContractAmount)·최종계약일자(contractDate)·최종계약금액(supplyAmount)·납품기한(deliveryDeadline)`로 확장(flat 26→29열). 기존엔 firstContractDate/contractDate만 있었음.
- export 쿼리(selectFlatListExport)는 G2B-32에서 이미 신규 컬럼 반환하므로 매퍼 변경 불요.

### 검증
- `compileJava` PASS, 프론트 `npm run build` PASS. 헤더↔바디 컬럼 수 정합성 확인.

### 남은 일
- 커밋/PR/배포는 사용자 승인 후. (배포 절차: §8/§9 — EC2 deploy 브랜치에 origin 머지 → docker compose build/up. DB 변경 없음 = Flyway 영향 없음.)

---

## 11. G2B-34 — 특정품목 풀어서 합계 조회 성능 개선 (2026-06-25)

- 티켓: G2B-34 (에픽 G2B-25). 브랜치: `feature/G2B-34-flat-totals-perf` (master 분기).
- 문제: 풀어서 보기 첫 로드가 운영에서 12~23초(cold). 진단 — 목록 쿼리는 1ms(`idx_flat_list_order` 사용)로 빠르나, **상단 합계 `selectFlatTotals`가 매 요청마다 is_active='Y' 약 83만 행을 풀스캔 SUM**(커버링 인덱스 없음). 로컬은 데이터가 버퍼풀에 캐시돼 빠름(723ms), 운영은 RDS cold(23s)→warm(3s) = 버퍼풀 캐싱 차.
- 변경:
  - **V23 마이그레이션**: `CREATE INDEX idx_flat_totals ON specific_item_flat (is_active, first_item_contract_amount, supply_amount)` → 합계 SUM이 index-only(Extra: Using index)로 전환(EXPLAIN ALL→ref 확인). cold I/O ~20배↓.
  - **SpecificItemController**: 합계(totals)를 **첫 페이지(start=0)에서만** 계산(페이지네이션 중 불변).
  - **ReportSpecificItemView.vue**: 응답에 totals 없으면(페이지네이션) 기존 값 유지(`if (data.totals != null)`).
- 검증: compile/build PASS. 로컬 V23 적용 후 — start=0 0.68s(totals 포함), start=100 0.17s(totals 없음). 인덱스 index-only 확인.
- 배포 시 prod Flyway가 V23(CREATE INDEX) 자동 적용 → 운영 첫 로딩 대폭 개선 예상.

### 검증 명령(참고)
```sql
-- flat 신규 컬럼 채움 확인
SELECT contract_no,item_seq,first_item_contract_date,first_item_contract_amount,delivery_deadline,contract_date,supply_amount
FROM specific_item_flat WHERE contract_no='20171008DE1_1';
-- grouped 합계 확인
SELECT group_key,first_contract_date,last_contract_date,initial_contract_amount,total_supply_amount,item_category_name
FROM specific_item_grouped WHERE group_key LIKE '20191104D04%';
```

---

## 8. 운영(RDS) 반영 기록 — 2026-06-24 (Claude)

운영 RDS(`g2b-db-prod...ap-northeast-2`)는 이미 **Flyway V21까지 적용**돼 있고(EC2 `~/g2b` 배포 커밋 `5d9f60a`, `g2b-api-1` 가동), `market_*`/raw 테이블이 모두 생성돼 있었음(데이터만 비어있었음).

### 8-1. raw 적재 — 완료 ✅
- `task_member_contract_raw` **11,736,823행** 적재·검증 완료(로컬과 총건수·`contract_type`(공사 5,911,082/용역 5,825,741)·`source_file` 20개 전부 일치).
- 방법: 로컬 `mysqldump --no-create-info --insert-ignore | gzip`(1.1GB) → EC2 `scp` → EC2에서 `zcat | mysql`(세션 `unique_checks=0`+`net_read_timeout=600`). EC2→RDS 동일 리전이라 수십 초.
- ⚠️ **직결 파이프(노트북→ssh→RDS 스트리밍) 금지**: RDS `net_read_timeout=30s` 때문에 중간 정체 시 연결 끊김(`ERROR 2013`). 반드시 EC2에 파일 두고 EC2에서 적재.
- 접속: `ssh -i ~/Desktop/G2B/ssh/g2b_prod.pem ubuntu@3.37.169.101`. RDS 비번은 EC2 `~/g2b/backend/src/main/resources/application.properties`에서 읽어 `MYSQL_PWD`로 주입(평문 노출 금지).

### 8-2. ETL(flat/grouped) — **DB 내 연산 불가 → 로컬 계산본 벌크 적재로 전환**
- ETL 엔드포인트 `POST /api/admin/etl/market-contract`는 운영 배포돼 있으나 SUPER_ADMIN JWT 필요(401). 처음엔 배포된 `runEtl`과 동일 SQL을 EC2→RDS로 직접 실행 시도.
- ⚠️ **치명적 발견: 운영 RDS `innodb_buffer_pool_size = 27MB`** (매우 작은 인스턴스). flat INSERT(11.7M raw JOIN 스캔)가 거의 전부 디스크 I/O로 돌아 **40분에 34.7만 행(≈145 rows/sec)** 수준 → DB 내 ETL(JOIN/윈도우함수) 비현실적. 중단함.
  - 중단(`KILL`) 후 부분삽입 34.7만 행 **롤백도 랜덤 I/O라 ≈77 rows/sec**로 매우 느림(약 70분).
- ✅ **전환 전략**: 로컬엔 이미 flat/grouped가 계산 완료돼 있으므로, **raw와 동일하게 로컬 mysqldump → EC2 → RDS 벌크 적재**(`unique_checks=0`+`foreign_key_checks=0`, extended-insert → 순차 I/O라 빠름). 무거운 연산을 RDS에서 안 함.
  - 자동화 스크립트(EC2): `~/prod_load_flat_grouped.sh` (롤백 종료 대기 → TRUNCATE → `zcat ~/market_flat_grouped.sql.gz | mysql`), 로그 `~/load_flat_grouped.log`, nohup.
  - 덤프: 로컬 `mysqldump --no-create-info --insert-ignore market_contract_flat market_contract_grouped`(352MB gz).
- **검증 기준(로컬 기대값)**: flat 2,672,131(공사 1,489,972/용역 1,182,159), grouped 2,623,994(공사 1,473,667/용역 1,150,327).
- 완료 후 EC2 임시파일(`prod_market_etl.sql`, `run_market_etl.sh`, `market_etl.log`, `prod_load_flat_grouped.sh`, `market_flat_grouped.sql.gz`, `load_flat_grouped.log`) 정리.

> **운영 반영 원칙(중요)**: 이 RDS 인스턴스에서는 `market_contract_*` ETL을 **DB 내 INSERT...SELECT로 돌리지 말 것**. 항상 로컬에서 ETL 수행 후 결과 테이블을 벌크 적재. 근본 해결은 RDS 인스턴스 상향 또는 `innodb_buffer_pool_size` 증대(G2B-30). raw 대량 적재법은 [[reference-prod-rds-bulk-load]] 참고.

### 8-3. 인스턴스 사양 / 적재 완료 (2026-06-24)
- 운영 RDS `g2b-db-prod` = (원래) **db.t4g.micro / RAM 1GB / gp2 70GiB / single-AZ / MySQL 8.4.5**. 버퍼풀 27MB는 1GB RAM의 한계.
- **micro에서 벌크 적재가 2시간+ 멈춤** — 원인: ① 버스터블 **CPU 크레딧 고갈**(수 시간 연속 부하), ② 27MB 버퍼풀 + gp2 ~210 IOPS I/O 한계. `SELECT MAX(id)`조차 행(완전 포화).
- **해결: RDS를 `db.t4g.large`로 임시 상향(즉시 적용)** → 재부팅으로 멈춘 적재 중단(벌크 로드는 배치별 auto-commit이라 커밋된 134만 행은 잔존, 트랜잭션 롤백 없음). 상향 후 **버퍼풀 27MB → 5.4GB**(파라미터는 인스턴스 비례 자동, 하드코딩 아님).
- **TRUNCATE 후 전체 재적재 → 약 6.5분 완료**(`MYSQL_EXIT=0`). ✅ **검증: 로컬과 정확히 일치**
  - flat **2,672,131** (construction 1,489,972 / service 1,182,159)
  - grouped **2,623,994** (construction 1,473,667 / service 1,150,327)
- EC2·로컬 임시파일 전부 정리 완료.
- ⚠️ **현재 RDS는 t4g.large 상태(비용↑).** 적재 끝났으니 **micro로 복귀하거나, 운영 스펙 결정 필요**(아래).

### 8-4. 운영 인프라 권고 (비용/성능)
- 인스턴스: db.t4g.micro(1GB)는 이 데이터 규모(아래 8-5)엔 부적합. **근무시간만 사용(10명) 패턴**이므로 **EventBridge 스케줄 기동(평일 주간만) + RDS t4g.large(8GB) or small + gp3** 조합이 월 ~8–10만원으로 적정(상세 채팅 논의).
- 스토리지: **gp2 → gp3 전환 권장**(크기무관 3,000 IOPS, gp2보다 저렴). 적재 끝난 지금 콘솔에서 전환 가능.
- ⚠️ **스토리지 자동조정 최대 임계값이 70GiB로 현재 할당량과 동일** → 더 못 늘어남. 가득 차면 장애. 임계값 상향 필요.

### 8-5. DB 용량 현황 (2026-06-24)
- g2b 스키마 **약 52.7GB / 70GiB (~75%)**. 여유 ~10–15GB로 빠듯.
- 용량 큰 테이블: `contract_info_detail_list_thing` 17.8GB(3,351만행), `contract_info_construction_work` 9.0GB, `contract_info_service` 8.8GB, `contract_info_list_thing` 4.5GB, `daily_contracts_*` 등 = **약 40GB**. 현재 raw `task_member_contract_raw` 9.5GB.
- ⚠️ **정정(이전 메모 오류): 이 40GB는 "드롭 가능한 죽은 레거시"가 아님.** `/api/data`·`/api/data/excel`(DataController+DataMapper) 통해 **구 화면 ServicesView/GoodsView/ConstructionsView/TargetProjectsView/ShoppingMallView가 지금도 라이브로 읽음.** 신 `market_contract` 화면과 병존 중.
- 운영 스케줄러 `app.scheduling.enabled=false` → 자동수집 off라 40GB는 **더 안 쌓이지만(안정)**, 구 화면 은퇴 전엔 **DROP 불가**.
- **디스크 대응**: 드롭으로 못 푸니 **스토리지 자동조정 임계값 상향(70→100GB)** 이 안전 조치. 레거시 제거는 구 화면 마이그레이션 완료 후(별도 티켓, G2B-29 / [[project-scheduler-cleanup-todo]]).

### 8-6. 다음 작업
- ① RDS 스펙 결정(micro 복귀 vs 스케줄+적정스펙) ② gp3 전환 + 자동조정 임계값 상향 ③ 레거시 테이블 정리 ④ 운영 보고서 화면(공사/용역) 동작 확인 ⑤ (선택) 업로드→ETL 자동 체이닝 코드 ⑥ super_admin 비밀번호 교체(EC2 `ps`에 평문 노출 이력).

---

## 12. G2B-35 — 프로젝트 컨텍스트 문서 정리 + 워크플로우 규칙 (2026-06-25)

- 티켓: G2B-35 (Task, 단독). 코드 변경 없음 = 빌드/배포/Flyway 영향 없음.
- 배경: G2B-15/G2B-24 완료 처리 중 `CLAUDE.md`·`AGENTS.md`의 로드맵/현재 브랜치/미구현 섹션이 stale 함을 확인(시장데이터 통합이 master 머지·운영 반영됐는데 문서엔 "raw→flat/grouped ETL 미구현"으로 남아 있었음).

### Jira 상태 정리 (이번 세션)
- **G2B-15 → 완료**: raw 적재(11,736,823행) 검증 완료. flat/grouped는 G2B-24로 분리.
- **G2B-24 → 완료**: V21(market_target_category 21건 + flat/grouped) + MarketContractEtlService + Controller/Mapper/ReportMarketService + 프론트 연동(커밋 `5d9f60a`). 완료 코멘트 추가됨.

### 문서 변경 (이번 티켓)
- `CLAUDE.md` / `AGENTS.md` (둘은 미러):
  - **Git 규칙**에 "작업 단위 티켓 먼저 생성(담당자 Jun Fe) → 진행 중 전환 → 착수" + "작업 종료 시 `AI_HANDOFF.md` 티켓 섹션 추가/갱신" 규칙 명문화.
  - **로드맵 현황**을 2블록으로: 특정품목 CSV(Phase 1~8 완료) + 시장데이터 통합(에픽 G2B-25, G2B-15/24/26/27/32/33/34 완료, G2B-28/29/30 미착수).
  - **현재 브랜치**: `master — G2B-34까지 머지 완료 (e2e8ae2)`.
  - **핵심 참고 파일**에 AI_HANDOFF.md + 시장데이터 DDL(V21~V23)/ETL/API·매퍼/화면 추가.
  - **미구현** 섹션에서 완료된 "raw→flat/grouped ETL" 제거, G2B-28/29/30·RDS t4g.large 상태로 갱신.
- `AI_HANDOFF.md`: 본 §12 추가.

### 워크플로우 규칙 (이후 모든 작업에 적용)
1. 작업 시작 전 G2B 프로젝트에 티켓 생성(담당자 Jun Fe) → 진행 중 전환.
2. 작업 진행하며 코멘트로 갱신, 끝나면 완료 전환.
3. **작업 종료 시 `AI_HANDOFF.md`에 해당 티켓 섹션(`## N. G2B-XX — 제목`) 추가/갱신** — 변경 파일·검증 결과·남은 일·배포 상태를 자족적으로 기재해 Codex가 바로 이어받을 수 있게 함.
4. 커밋·푸시·PR·배포는 사용자 명시 승인 후에만.

---

## 13. G2B-36 — 시장데이터 물품/쇼핑몰 메뉴·페이지 제거 + procurements API 정리 (2026-06-25)

- 티켓: G2B-36 (Task, 에픽 G2B-25). 브랜치: `feature/G2B-36-remove-market-goods-shoppingmall` (master 분기).
- 배경: "특정품목 조달내역(물품·쇼핑몰 통합)" 메뉴 신설로 시장데이터 메뉴 내 **물품(ReportGoodsView)·쇼핑몰(ReportShoppingMallView)** 페이지 폐지.
- ⚠️ **사전 분석 핵심**: 두 페이지가 쓰는 API 일부가 살아있는 다른 화면과 공유됨 → 전량 삭제 불가.
  - `PATCH /api/report/procurements/saved`, `PATCH /api/report/shopping-mall/saved` → **TopContractsReportView가 공유** → 유지.
  - `ShoppingMallService`(+매퍼) → 구 `ShoppingMallView`(`/api/shopping-mall`)와 공유 → 유지(shopping-mall 백엔드는 손대지 않음).
  - `GET /api/report/procurements`·`/excel` 만 ReportGoodsView 전용 → 제거 대상.

### 변경 — 프론트
- `views/components/LegacySidebarLayout.vue`: 시장데이터 메뉴에서 `물품`·`쇼핑몰` `<li>` 제거.
- `router/index.js`: `/report-goods`·`/report-shopping-mall` 라우트+import 제거. 기본 리다이렉트 `next({name:'report-goods'})` 2곳 → `report-specific-item`.
- `views/ReportGoodsView.vue`·`views/ReportShoppingMallView.vue` 삭제.
- `views/MainLayoutView.vue`(라우터 미등록 고아 페이지): 끊긴 `/report-goods` 버튼 1줄 제거.

### 변경 — 백엔드(procurements 전용만 외과적 제거)
- `ReportDataController.java`: `GET /procurements`·`GET /procurements/excel` 핸들러 제거(143줄). **`PATCH /procurements/saved` 유지**. shopping-mall 핸들러 전부 유지.
- `ReportProcurementService.java`: `getList/getCount/streamForExcel` 제거. `updateGroupedSaved/updateFlatSaved` 유지. 미사용 import(List/Map/ResultHandler) 정리.
- `ProcurementContractMapper.java`/`.xml`: 6개 select(flat/grouped list·count·export) + where 프래그먼트 제거. update(saved) 2개만 유지. (매퍼는 ReportProcurementService에만 주입됨을 grep 확인.)

### 검증 — 완료 ✅
- 백엔드 `./gradlew compileJava --rerun-tasks` PASS (죽은 참조 없음).
- 프론트 `npm run build` PASS (152 modules, 삭제 뷰 참조 깨짐 없음).
- diff: 9개 파일 변경 + 2개 뷰 삭제, 순 ~1,700줄 제거.

### 배포 — 완료 ✅ (2026-06-25)
- PR #24 master 머지(`7a4386d`) → EC2 deploy 브랜치(feature/G2B-8...)에 origin/master 머지(`7e14926`) → `docker compose build` + `up -d`.
- 검증: 도메인 웹 HTTP 200, 배포 JS 번들에 `report-goods`/`report-shopping-mall` 참조 0건·`report-specific-item` 유지. API 정상 기동(Flyway 변경 없음). `deploy/g2b.conf` 서버 로컬수정 보존.

---

## 14. G2B-37 — 특정품목 물품분류 다중선택 검색 (2026-06-25)

- 티켓: G2B-37 (Task, 에픽 G2B-25). 브랜치: `feature/G2B-37-multi-item-category` (master 분기).
- 요구: 특정품목 조달내역 화면의 물품분류번호/분류명 필터를 단일 선택 → **다중 선택**.
- 설계: **단일 콤보박스 + 칩** UI, `item_category_no` 기준 **정확매칭 IN**(기존 단일 LIKE 대체). 전송은 CSV(`itemCategoryNos=a,b`) → Spring `List<String>` 자동 분리.

### 변경 — 프론트 (`ReportSpecificItemView.vue`)
- 물품분류명·번호 두 입력 → **단일 콤보박스**(`categorySearch`). API(`/item-categories`)가 명·번호 둘 다 검색.
- 상태: `selectedCategories[]`(칩), `categoryDropdownOpen`. 기존 `filters.itemCategoryNo/Name`·`activeCategoryField` 제거.
- `selectCategory`=칩 추가(중복방지·선택 후 검색어 초기화·드롭다운 유지), `removeCategory`/`isCategorySelected` 추가. 선택된 옵션 ✓ 표시.
- `buildParams`: `itemCategoryNos = selectedCategories.map(no).join(',')`. 목록·엑셀(URLSearchParams)·합계·페이지네이션 자동 반영.
- 칩 CSS 추가.

### 변경 — 백엔드
- `SpecificItemController`(목록 GET + 엑셀 GET + buildParams): `itemCategoryNo`/`itemCategoryName` 단일 String → `List<String> itemCategoryNos`. 비어있지 않으면 params에 주입.
- `SpecificItemMapper.xml`: flat `commonWhere` + grouped where의 단일 LIKE 2개 제거 → 기존 `topCategories` 패턴 복제한 `item_category_no IN <foreach collection="p.itemCategoryNos">`. list/count/export/totals 공유라 자동 반영.
- `/item-categories` 옵션 API·`selectItemCategories`는 변경 없음.

### 검증
- 백엔드 `compileJava` PASS, 프론트 `npm run build` PASS (152 modules). 제거 식별자 잔존 0건.
- ⚠️ **런타임 스모크 미실시**(로그인+백엔드+DB 필요). CSV→List 바인딩·IN foreach는 검증된 `topCategories` 패턴과 동일. 배포 후 또는 로컬 기동으로 다중 칩→IN 조회·엑셀 확인 권장.

### 남은 일
- 커밋/PR/배포는 사용자 승인 후. DB 변경 없음 = Flyway 영향 없음.

> G2B-37 배포 완료(2026-06-25): PR #25 머지(6cfb102) → EC2 deploy 머지(5c01ed5) → build/up. 도메인 200, 번들에 category-chip/다중선택 코드 반영 확인.

---

## 15. G2B-38 — 물품분류 조회 성능(V24)+칩 세로표기+표 셀 hover (2026-06-25)

- 티켓: G2B-38 (Task, 에픽 G2B-25). 브랜치: `feature/G2B-38-item-category-perf-ux` (master 분기).

### ① 물품분류 옵션 조회 성능
- 원인: `SpecificItemMapper.selectItemCategories`의 `SELECT DISTINCT item_category_no, item_category_name`이 `item_category_name` 미인덱스 → specific_item_flat 885,340행 풀스캔+temp(distinct 실제 27개). 입력마다(디바운스) 반복.
- 백엔드: **V24** `CREATE INDEX idx_flat_category_options ON specific_item_flat (is_active, item_category_no, item_category_name)`. EXPLAIN 로컬 검증: `type=ALL; Using temporary` → `type=range, key=idx_flat_category_options; Using index`(index-only, temp 제거).
- 프론트: `/item-categories` **최초 1회만 로드**(`loadAllCategories`, onMounted, limit=100→전체 27개). 입력은 `categoryOptions` **computed로 클라이언트 필터링**(키입력 API 호출 제거). 기존 `fetchCategoryOptions`·디바운스 타이머 삭제.

### ② 칩 세로표기 + 다른 필터 고정 (`ReportSpecificItemView.vue` 템플릿/CSS)
- 입력+드롭다운을 `.category-input-wrap`(position:relative)로 감싸 드롭다운을 **입력 기준**으로 고정(칩이 늘어도 드롭다운 위치 불변).
- `.search-filter-row` `align-items: center → flex-start`(다른 필터 제자리·동일크기 유지).
- `.category-chips` `flex-direction: column`(입력 아래 **세로 리스트**), 칩에 `.category-chip-label` 말줄임.

### ③ 표 셀 hover 전체값
- 표에 `ref="tableRef"`. `watch(items, () => nextTick(refreshCellTitles))`로 데이터 갱신 시 `tbody td` 중 **잘린 셀(scrollWidth>clientWidth)만 `td.title` 설정** → native 툴팁. 셀별 :title 수정 없음.

### 검증
- 프론트 `npm run build` PASS (152 modules), 제거 식별자 잔존 0건.
- ① EXPLAIN 로컬 확인(위). 로컬 인덱스는 드롭(Flyway V24가 정식 적용).
- ⚠️ ②③ 시각 검증은 배포 후 도메인에서. 

### 배포 유의
- **V24 = 운영 RDS specific_item_flat(885만행)에 CREATE INDEX** → 배포 시 Flyway가 자동 실행, 수십 초~분 소요·일시 부하 가능(현재 t4g.large).

> G2B-38 배포 완료(2026-06-25): PR #26 머지(f142585) → EC2 deploy 머지(b636fd4) → build/up. V24 운영 적용(Flyway now at v24, 16s), RDS EXPLAIN index-only 확인, 도메인 200.

---

## 16. G2B-39 — 특정품목 메뉴명 '물품' + 엑셀 파일명 (2026-06-26)

- 티켓: G2B-39 (Task, 에픽 G2B-25). 브랜치: `feature/G2B-39-menu-rename-excel-filename` (master 분기). 프론트 전용.
- `LegacySidebarLayout.vue`: 시장데이터 메뉴 라벨 `특정품목 (물품·쇼핑몰 통합)` → `물품`.
- `ReportSpecificItemView.vue`: 엑셀 `a.download`를 `물품_${excelStamp()}.xlsx`로. `excelStamp()`=yyyyMMddHHmm. (백엔드 Content-Disposition은 blob a.download가 덮어써 실제 파일명 지배 → 백엔드 무변경.)
- 검증: `npm run build` PASS. 배포는 사용자 승인 후(코드만, DB/Flyway 영향 없음).

> G2B-39 배포 완료(2026-06-26): PR #27 머지(572a3b7) → EC2 deploy → build/up. 메뉴 '물품', 엑셀 '물품_yyyyMMddHHmm'. 도메인 200, 구 라벨 0건 확인.

---

## 17. G2B-40 — 특정품목 페이지 제목 '물품' (2026-06-26)

- 티켓: G2B-40 (Task, 에픽 G2B-25, G2B-39 후속). 브랜치: `feature/G2B-40-page-title-mulpum`. 프론트 1줄.
- `ReportSpecificItemView.vue` h1 `특정품목 조달내역 (물품 · 쇼핑몰 통합)` → `물품`.
- 검증: `npm run build` PASS. DB/Flyway 영향 없음.

> G2B-40 배포 완료(2026-06-26): PR #28 머지(bd2f533) → EC2 deploy → build/up. 페이지 제목 '물품'. 도메인 200, 구 제목 0건.

---

## 18. G2B-41 — 시장데이터-용역 UI 개편 (제목/필터/CSS) (2026-06-26)

- 티켓: G2B-41 (Task, 에픽 G2B-25, 용역 개편 1/3). 브랜치: `feature/G2B-41-services-ui-revamp`.
- #1 제목: `ReportServicesView` `시장데이터 - 용역`, `ReportSpecificItemView` `시장데이터 - 물품`.
- #2 필터:
  - 입찰계약방법(cntctCnclsMthdNm)·조달업무영역(procurementWorkArea) → **select**. 옵션은 신설 `GET /api/report/market-contracts/filter-options?contractType=`(distinct) 에서 최초 1회 로드(`loadFilterOptions`).
  - **계약명(contractName) LIKE 필터 추가**: 매퍼 `commonFilters`에 `contract_name LIKE`, 6개 쿼리 메서드(@Param)·`ReportMarketService`(getList/getCount/streamForExcel 시그니처+호출)·`MarketContractController`(목록+엑셀 param) 경유.
  - 공공조달분류 중/소분류 유지. 최초계약일자 텍스트 필터 유지.
- #3 CSS: `data-table` 물품 양식(padding 6/8, font .82em, td max-width 180+ellipsis+nowrap, sticky th, 스크롤바 상시) 이식 + hover 전체값 title(`tableRef`+`watch(items)`→refreshCellTitles).
- 신규 백엔드: `MarketContractMapper.selectDistinctContractMethods/selectDistinctWorkAreas` + xml, `ReportMarketService.getFilterOptions`, 컨트롤러 `/filter-options`.
- 검증: `compileJava` PASS, `npm run build` PASS. ⚠️ 런타임(필터옵션 응답·계약명 조회)은 배포 후 도메인 확인 예정.
- 남은 일: 커밋/PR/배포 승인 후. 이후 G2B-42(데이터·합계·ETL), G2B-43(튜닝).

> G2B-41 배포 완료(2026-06-26): PR #29 머지(caae05e) → EC2 deploy → build/up. 제목·필터 select·계약명·data-table CSS. 도메인 200, 번들 '시장데이터 - 용역'·filter-options 반영.

---

## 19. G2B-42 — 시장데이터-용역 데이터/합계/ETL 정정 (2026-06-26)

- 티켓: G2B-42 (Task, 에픽 G2B-25, 용역 개편 2/3). 브랜치: `feature/G2B-42-services-data-totals`.
- #4 flat: 컬럼을 최초계약일자(firstContractDate)·최종계약일자(finalContractDate)·착수(startDate)·완수(endDate)·최초계약금액(firstContractAmount)·최종계약금액(finalContractAmount)로 정리(데이터는 market_contract_flat=raw is_final행 기준, 매퍼에 이미 노출).
- #4 합계: market totals 신설 — `selectFlatTotals`(SUM first_contract_amount, SUM total_contract_amount)·`selectGroupedTotals`(SUM initial, SUM total_sum), 컨트롤러 start=0에서 `totals` 반환, 프론트 상단 `grouped-totals` 밴드(firstAmountTotal/finalAmountTotal).
- #4 grouped 라벨: 최초계약금액 → 최초계약금액(합계).
- **ETL 물품 기준 정정**(`MarketContractEtlService`): `initial_contract_amount = SUM(first_contract_amount 그룹전체)`(기존 최초연차만), `last_contract_date = MAX(contract_date)`(기존 MAX(first_contract_date)), 윈도우 서브쿼리 제거(성능↑).
- 검증: compileJava PASS, npm build PASS. **로컬 grouped 재적재 검증**: 2,623,994행, 샘플 initial=그룹 first합·last=MAX(contract_date) 일치, flat·grouped totals 동일.
- ⚠️ **운영 반영**: 코드 변경만으로는 기존 prod grouped가 구 로직 → 배포 후 **grouped 재적재 필요**(EC2에서 TRUNCATE market_contract_grouped + 신 로직 INSERT, saved 백업/복원; flat은 불변이라 재적재 불요). 현재 RDS t4g.large.
- 남은 일: 커밋/PR/배포 + prod grouped 재적재. 이후 G2B-43(튜닝, totals/정렬 인덱스 EXPLAIN).

> G2B-42 배포 완료(2026-06-26): PR #30 머지(9760d08) → EC2 deploy → build/up. ⚠️운영 grouped 재적재 중 in-DB GROUP BY가 RDS를 2회 크래시(각 2m08s 'Server shutdown')→ §8-2 원칙대로 로컬 계산본 mysqldump→EC2→벌크 import로 복구(4m44s, 2,623,994행, totals 로컬 일치). grouped saved 플래그는 초기화됨(flat 무영향).

---

## 20. G2B-43 — 시장데이터 조회·엑셀 쿼리 튜닝 (V25 인덱스) (2026-06-26)

- 티켓: G2B-43 (Task, 에픽 G2B-25, 용역 개편 3/3). 브랜치: `feature/G2B-43-market-query-tuning`. 마이그레이션 전용(코드 무변경).
- 문제(EXPLAIN): flat 목록 `ORDER BY contract_date DESC` vs idx_mcf_order(first_contract_date)→**filesort**, grouped 목록 `ORDER BY first_contract_date` vs idx_mcg_last(last_contract_date)→**filesort**, totals는 contract_type 파티션(1.3M) 풀스캔.
- **V25** 인덱스 4종:
  - `idx_mcf_cdate (contract_type, contract_date DESC, contract_no)` — flat 목록 정렬·기간 정합
  - `idx_mcg_first (contract_type, first_contract_date DESC, group_key, vendor_biz_reg_no)` — grouped 목록 정렬
  - `idx_mcf_totals (contract_type, is_active, first_contract_amount, total_contract_amount)` — flat 합계 커버링
  - `idx_mcg_totals (contract_type, initial_contract_amount, total_contract_amount_sum)` — grouped 합계 커버링
- 검증(로컬 EXPLAIN): 목록 filesort 제거(idx_mcf_cdate/idx_mcg_first 사용), totals Using index(index-only). 엑셀(Cursor export)도 동일 WHERE/ORDER라 함께 개선. 로컬 인덱스는 드롭(Flyway V25 정식 적용).
- ⚠️ 배포: V25 = 운영 RDS flat(2.67M)·grouped(2.62M)에 CREATE INDEX 4종(Flyway 자동). CREATE INDEX는 §8-2 크래시(GROUP BY 집계)와 다른 경량 DDL이며 V24(885만행 1종 16s) 선례 있음 — 수십초~1분 예상이나 Flyway 로그 모니터 권장.

> G2B-43 배포 완료(2026-06-26): PR #31 머지(0dd7098) → EC2 deploy → build/up. Flyway V25(인덱스 4종, 2m34s, 크래시 없음). 운영 EXPLAIN: 목록 filesort 제거, totals index-only. 도메인 200.

---

## 21. G2B-44 — 용역 검색필터: 조달업무영역 옵션 인덱스(V26) + 중/소분류 다중선택 (2026-06-26)

- 티켓: G2B-44 (Task, 에픽 G2B-25). 브랜치: `feature/G2B-44-services-filter-perf-multiselect`.
- #1 조달업무영역/입찰계약방법 옵션 느림: selectDistinctWorkAreas/ContractMethods가 flat contract_type 파티션(1.3M) 풀스캔+temp+filesort로 **4.2s**. **V26** 커버링 인덱스 `idx_mcf_workarea(contract_type,is_active,public_procurement_major)`·`idx_mcf_method(contract_type,is_active,contract_method)` → index-only, **0.6s**(로컬 실측).
- #2 공공조달분류 중/소분류 다중선택:
  - 백엔드 `commonFilters`: `public_procurement_mid = #{}`/`public_procurement_name = #{}` → **FIND_IN_SET(col, #{csv}) > 0** (단일/다중 CSV 모두, 공사 페이지 단일값 호환).
  - ⚠️ 컨트롤러 `detailFilter = firstNonBlank(prdctClsfcNo, publicProcurementCategory)` → **`= prdctClsfcNo`** 로 변경(소분류 다중 CSV가 detailItemName LIKE '%a,b%'로 들어가 깨지는 문제 방지; 소분류는 FIND_IN_SET 전용).
  - 프론트: 중/소분류 `<select multiple>` + buildParams CSV join, filteredSubCategories=선택 중분류들의 합집합, onMidCategoryChange가 무효 소분류 제거.
  - **categoryMap 동적화**: 하드코딩 4개 mid(ICT 2개 누락) → filter-options에 `categories`(market_target_category mid→name, service 6mid/15name) 추가해 프론트가 동적 구성. 누락 카테고리 해결.
  - 신규 백엔드: `MarketContractMapper.selectCategoryHierarchy` + xml, `ReportMarketService.getFilterOptions`에 categories 추가.
- 검증: compileJava PASS, npm build PASS, V26 EXPLAIN(index-only) + 시간 4.2s→0.6s, FIND_IN_SET('CM') 7312건, 계층 6mid 확인. 로컬 인덱스 드롭(Flyway V26 정식 적용).
- 남은 일: 커밋/PR/배포(Flyway V26 = CREATE INDEX 2종, 경량). 공사 페이지는 단일 select 유지(영향 없음).
