# ADR-0002: 업무별 구성원별 계약내역 RAW 적재 전략

- **Status**: Accepted
- **Date**: 2026-06-11
- **Owners**: JunFe23

## Context

- 조달데이터허브에서 '업무별 구성원별 계약내역'을 기술용역/공사 두 종류로 CSV 다운로드 가능
- 기존에 `service_contract_raw`, `construction_contract_raw` 두 테이블이 존재했으나, CSV 구조 변경으로 재설계 필요
- 두 CSV의 컬럼 구조를 분석한 결과 45개 중 42개 공통, 3개만 명칭 차이(착수/완수일자 ↔ 착공/준공일자)
- 향후 flat/grouped/summary 테이블과 대시보드 구현이 목표

## Decision

**기술용역과 공사를 단일 테이블 `task_member_contract_raw`로 통합 적재한다.**

- **결정 내용**: `contract_type` 컬럼(engineering | construction)으로 업종 구분. 날짜 컬럼은 `start_date`, `end_date`, `total_end_date`로 통합
- **적용 범위**: V11 Flyway 마이그레이션, 업로드 API `POST /api/admin/upload/task-member-contract`, 프론트 업로드 탭 단일화
- **롤아웃**: 기존 `service_contract_raw`, `construction_contract_raw` DROP → `task_member_contract_raw` 신규 생성 → CSV 재적재

## Consequences

### Positive

- 테이블 하나로 관리 → 조회/ETL 쿼리 단순화
- 업로드 UI 탭 하나로 통합 → UX 단순화
- 향후 flat/grouped 테이블 설계 시 UNION 없이 단일 소스 테이블 사용

### Negative / Risks

- `contract_type` 필터를 빠뜨리면 기술용역+공사가 혼합 집계될 수 있음 → 조회 쿼리 작성 시 주의 필요
- 기존 `service_contract_flat`, `service_contract_grouped`, `construction_contract_flat`, `construction_contract_grouped`는 구 raw 테이블 기반 → ETL 재설계 필요 (Phase 이후 별도 티켓)

### Operational notes

- 업로드 시 파일명에 '기술용역' 포함 여부로 `contract_type` 자동 판별 (fallback: 드롭다운 선택)
- 중복 방지: `UNIQUE KEY (contract_no, change_seq, contract_type)`

## Alternatives considered

- **대안 A — 테이블 2개 유지** (`service_contract_raw`, `construction_contract_raw`): 컬럼 차이가 3개뿐이라 중복 관리 비용이 더 큼. 기각
- **대안 B — contract_type 없이 파일명으로만 구분**: 조회 시 매번 source_file 패턴 매칭 필요, 인덱스 활용 어려움. 기각

## Links

- Issue: G2B-11
- Migration: `backend/src/main/resources/db/migration/V11__create_task_member_contract_raw.sql`
- Sample CSV: `docs/sample_csv/task_member_contract/`
