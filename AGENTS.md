# G2BPlatform — Codex 컨텍스트

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 경로 | `/Users/junfe/Desktop/G2B/G2BPlatform` |
| Backend | Java 21, Spring Boot 3.3, WebFlux, JPA, MyBatis, Flyway, Security(JWT) |
| Frontend | Vue 3, Vite 6, Axios |
| DB | 로컬: MySQL 8 (`root@127.0.0.1`, 스키마 `g2b`) / 서버: RDS MySQL 8.4 (`admin@g2b-db-prod.c7so4iqo4f0a.ap-northeast-2.rds.amazonaws.com`) |
| 배포 | Docker Compose (`g2b-api-1`:8080 + `g2b-web-1`:3000), 서버 nginx `deploy/g2b.conf` |
| 서버 | `ssh -i ~/Desktop/G2B/ssh/g2b_prod.pem ubuntu@3.37.169.101` (경로: `~/g2b`) |
| 도메인 | https://g2btop.duckdns.org |

---

## Git 규칙

- Jira 프로젝트: **G2B**
- **모든 작업은 작업 단위로 Jira 티켓을 먼저 생성**(담당자 Jun Fe)하고 진행 중으로 전환한 뒤 착수. 묶이는 작업은 에픽으로 그룹화
- 브랜치: `feature/{JIRA_KEY}-{slug}` (master에서 분기)
- 커밋: `{JIRA_KEY} {type}: 한 줄 요약`
- **작업 종료 시 `AI_HANDOFF.md`에 티켓 섹션 추가/갱신** — Claude/Codex 어느 쪽이 이어받아도 끊김 없게 자족적으로 작성 (변경 파일·검증·남은 일·배포 상태)
- **커밋·푸시·PR·서버 배포는 명시적 요청 시에만 실행**
- `application.properties`, service key 등 비밀값 커밋 절대 금지
  - `backend/.gitignore`에 `application.properties` 등록되어 있음
  - 서버 `application.properties`는 서버에만 존재, git 이력에서 완전 제거 완료 (2026-06-09)

---

## 로드맵 현황

### 특정품목 CSV 파이프라인 (Phase 1~8 — ✅ 전부 완료)

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | G2B-8 로컬 특정품목 CSV E2E 검증 | ✅ 완료 |
| Phase 2 | 서버 배포 + Flyway V8~V10 검증 | ✅ 완료 |
| Phase 3 | 서버 실제 CSV 대량 업로드·검증 (문서화) | ✅ 완료 |
| Phase 4 | 업무별 구성원별 CSV 분석·설계 문서 | ✅ 완료 |
| Phase 5 | Flyway V11 raw 테이블 | ✅ 완료 |
| Phase 6 | 백엔드 task-member-contract 업로드 API | ✅ 완료 |
| Phase 7 | 프론트 두 번째 탭 연동 | ✅ 완료 |
| Phase 8 | 서버 반영·운영 적재 | ✅ 완료 |

### 시장데이터 공사/용역 통합 (에픽 G2B-25 — ✅ 운영 반영 완료)

| 티켓 | 내용 | 상태 |
|------|------|------|
| G2B-15 | 공사·용역 CSV 전체 raw 적재 (`task_member_contract_raw` 11.7M행) | ✅ 완료 |
| G2B-24/16 | `market_contract_flat/grouped` + `market_target_category`(V21) ETL·적재 | ✅ 완료 |
| G2B-26 | 통합 조회 API/매퍼 (`/api/report/market-contracts`) | ✅ 완료 |
| G2B-27 | 공사/용역 보고서 프론트 통합 API 전환 | ✅ 완료 |
| G2B-32 | 특정품목 품목별 최초/최종 + 납품·착수완수일 (V22) | ✅ 완료 |
| G2B-33 | 특정품목 보기 최종계약금액 컬럼·순서 정정 (+엑셀) | ✅ 완료 |
| G2B-34 | 특정품목 풀어서 합계 조회 성능 개선 (V23) | ✅ 완료 |
| G2B-28 | TOP/시장현황 보고서의 공사·용역 쿼리 통합 테이블 전환 | ⏳ 미착수 |
| G2B-29 | 공사/용역 레거시 매퍼·서비스·엔드포인트 제거 | ⏳ 미착수 |
| G2B-30 | 시장데이터 ETL 성능 최적화 및 운영 RDS 스펙 결정 | ⏳ 미착수 |

> 상세 진행·운영 반영 기록은 `AI_HANDOFF.md` 참조 (작업 종료 시 갱신 필수).

---

## 현재 브랜치

- `master` — G2B-34까지 머지 완료 (최신 커밋 `e2e8ae2`)

---

## 주요 결정사항 (ADR 요약)

### DB 연결
- 로컬 직접 기동(Gradle) 시: `application-local.properties`의 `spring.datasource.url=jdbc:mysql://127.0.0.1:3306/g2b...` 사용
- 로컬 Docker Compose 기동 시: `host.docker.internal:3306` → 호스트 MySQL 연결
- 서버: RDS `g2b-db-prod` (admin 계정, 비밀번호는 서버 `~/g2b/backend/src/main/resources/application.properties`에만 존재)

### Flyway
- 서버 RDS에 Flyway 최초 도입 시 `flyway_schema_history` 수동 baseline 삽입으로 해결 (V7 baseline)
- `baselineOnMigrate=true` 코드 추가 방식은 채택하지 않음 (빈 DB 배포 시 위험)

### 특정품목 CSV 업로드 (구현 완료)
- API: `POST /api/admin/upload/specific-item`
- 권한: `ROLE_SUPER_ADMIN`
- 처리: UTF-16→UTF-8 변환, JDBC batch 2000건, `INSERT IGNORE`
- 로컬 적재 결과: 2017~2026년 **1,023,477행** (Job 14건 전부 SUCCESS)
- 중복 방지: 재업로드 시 전량 skipped 확인

### 보안 조치 (2026-06-09)
- `application.properties`가 과거 커밋에 포함된 사실 발견 (RDS 비밀번호 노출 — 값은 문서에 기재하지 않음)
- RDS 비밀번호 교체 완료
- `git filter-repo`로 전체 이력에서 파일 제거 후 force push 완료
- GitHub Support 캐시 제거 요청은 선택사항 (비밀번호 교체로 실질 위험 없음)

---

## 핵심 참고 파일

| 영역 | 경로 |
|------|------|
| ADR (특정품목 Job) | `docs/adr/ADR-0001-specific-item-raw-ingestion-job.md` |
| CSV·ETL 계획 | `docs/plan_csv_upload_etl_migration.md` |
| 로컬 E2E 런북 | `docs/runbook/local-specific-item-upload.md` |
| 샘플 CSV | `docs/sample_csv/` |
| raw 테이블 DDL | `backend/src/main/resources/db/migration/V8~V10` |
| 업로드 API | `backend/.../controller/CsvUploadController.java` |
| Job 서비스 | `backend/.../service/SpecificItemCsvJobService.java` |
| 업로드 UI | `frontend/src/views/RawDataImportView.vue` |
| 비동기 풀 | `backend/.../config/AsyncConfig.java` (pool=2) |
| 로컬 스케줄 off | `backend/src/main/resources/application-local.properties` |
| **handoff (이어받기용)** | `AI_HANDOFF.md` (작업 종료 시 티켓 섹션 추가/갱신) |
| 시장데이터 테이블 DDL | `backend/.../db/migration/V21~V23` (market_contract_*, market_target_category) |
| 시장데이터 ETL | `backend/.../service/MarketContractEtlService.java` |
| 시장데이터 API/매퍼 | `backend/.../controller/MarketContractController.java`, `mapper/MarketContractMapper.{java,xml}`, `service/ReportMarketService.java` |
| 시장데이터 화면 | `frontend/src/views/ReportConstructionsView.vue`, `ReportServicesView.vue`, `ReportSpecificItemView.vue` |

---

## 미구현 (향후 작업)

- TOP/시장현황 보고서 공사·용역 쿼리 통합 테이블 전환 (G2B-28) — 구 `construction_contract_*`/`service_contract_*` 참조 매퍼가 V21 DROP으로 깨질 수 있음, 영향범위 우선 점검
- 공사/용역 레거시 매퍼·서비스·엔드포인트 제거 (G2B-29) — 구 화면(`/api/data` 계열) 은퇴 후
- 시장데이터 ETL 성능 최적화 / 운영 RDS 스펙 결정 (G2B-30) — 현재 RDS `t4g.large` 임시 상향 상태(micro 복귀 미정)
- Job 취소 API — 미구현 (api restart 시 Job 중단)
- `SpecificItemCsvService.java` — 레거시, 사용 안 함
