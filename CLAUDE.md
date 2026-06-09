# G2BPlatform — Claude Code 컨텍스트

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
- 브랜치: `feature/{JIRA_KEY}-{slug}` (master에서 분기)
- 커밋: `{JIRA_KEY} {type}: 한 줄 요약`
- **커밋·푸시·PR·서버 배포는 명시적 요청 시에만 실행**
- `application.properties`, service key 등 비밀값 커밋 절대 금지
  - `backend/.gitignore`에 `application.properties` 등록되어 있음
  - 서버 `application.properties`는 서버에만 존재, git 이력에서 완전 제거 완료 (2026-06-09)

---

## 로드맵 현황

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | G2B-8 로컬 특정품목 CSV E2E 검증 | ✅ 완료 |
| Phase 2 | 서버 배포 + Flyway V8~V10 검증 | ✅ 완료 |
| Phase 3 | 서버 실제 CSV 대량 업로드·검증 (문서화) | ✅ 완료 |
| Phase 4 | 업무별 구성원별 CSV 분석·설계 문서 | 🔄 진행 중 |
| Phase 5 | Flyway V11 raw 테이블 | ⏳ 대기 |
| Phase 6 | 백엔드 task-member-contract 업로드 API | ⏳ 대기 |
| Phase 7 | 프론트 두 번째 탭 연동 | ⏳ 대기 |
| Phase 8 | 서버 반영·운영 적재 | ⏳ 대기 |

> **Phase 3 끝나기 전에 Phase 4 시작 금지**

---

## 현재 브랜치

- `feature/G2B-8-local-specific-item-e2e` — Phase 1~2 작업 브랜치 (push 완료)

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
- `application.properties`가 과거 커밋에 포함된 사실 발견 (RDS 비밀번호 `g2btop123` 노출)
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

---

## 미구현 (향후 작업)

- raw → flat/grouped ETL (`CALL sp_etl_*`) — 별도 티켓
- Job 취소 API — 미구현 (api restart 시 Job 중단)
- `SpecificItemCsvService.java` — 레거시, 사용 안 함
- RawDataImport 두 번째 탭 **업무별 구성원별 계약내역** — UI 스텁만 존재 (Phase 4~7)
