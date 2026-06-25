# ADR-0004: 특정품목 품목별 최초/최종 계약일자·금액 및 납품·착수/완수일 표기

- 상태: 채택(Accepted)
- 작성일: 2026-06-24
- 관련 티켓: G2B-32 (에픽 G2B-25)
- 대체/보완: **ADR-0003 §4(날짜·금액 산출 기준)를 본 ADR이 대체**한다.
- 관련 코드:
  - `backend/.../service/SpecificItemEtlService.java` (flat 적재 + `rebuildGrouped`)
  - `backend/.../service/MarketContractEtlService.java`
  - `backend/.../mapper/SpecificItemMapper.xml`, `MarketContractMapper.xml`
  - `backend/src/main/resources/db/migration/V22`
  - `frontend/src/views/ReportSpecificItemView.vue` 등

## 배경

특정품목 조달내역(물품·쇼핑몰 통합) 화면에서 장기/단기계약의 **최초·최종 계약일자·금액**을 표기해야 한다. ADR-0003은 grouped 집계 기준을 정했으나, 다품목·변경계약 실데이터를 분석한 결과 다음이 드러났다.

- 한 계약(contract_no)에 **품목이 여러 개**인 경우가 흔하다(특히 쇼핑몰=제3자단가 17,798건, 일반물품 716건). 공사/용역은 품목 개념 자체가 없다.
- 변경계약 시 **품목이 통합/분리**되거나(변경계약의 5.1%, 2,895건) **신규 품목이 00이 아닌 차수에 처음 등장**(전체의 0.67%, 2,878계약)한다.
- 따라서 "계약 단위 단일 최초금액"이나 "00차수 = 최초" 가정은 부정확하다. **품목(item_seq) 단위 추적**이 정확하다.

클라이언트 요구: 단건은 품목별로 보고, 장기계약 합쳐서 볼 때는 맨 앞 한 품목으로 합쳐도 됨. 단건 엑셀도 품목별.

## 결정

### 1) flat = 품목(item_seq) 단위 유지, 품목별 최초/최종
- 품목 추적키 = **`item_seq`(라인 번호)**. 차수 간 고정이며, `detail_item_no`는 같은 라인 안에서 변동할 수 있어 키로 부적합.
- 각 품목(최종차수 `is_final_contract='Y'` 행)에 대해:
  - **최초계약일자/금액** = 그 `item_seq`가 **처음 등장한 차수**(`MIN(change_seq)`)의 `contract_date` / `supply_amount`
    - → 신규 컬럼 `first_item_contract_date`, `first_item_contract_amount`
  - **최종계약일자/금액** = 최종차수의 `contract_date` / `supply_amount` (기존 컬럼 사용)
- **최종차수에 없는 품목은 flat에 적재하지 않는다**(= 최종계약 기준 품목만 관리).

### 2) grouped = 장기그룹 합산 + 대표품목 (ADR-0003 §4 대체)
- 묶음 키는 ADR-0003 §2 그대로(`data_type` + `COALESCE(first_year_contract_no, contract_no)` + `vendor_biz_reg_no`).
- 산출:
  - 최초계약일자 = `MIN(first_item_contract_date)`
  - **최초계약금액(합) = `SUM(first_item_contract_amount)`** ← ADR-0003의 "가장 이른 contract_date 건 금액 합" 정의를 대체
  - 최종계약일자 = `MAX(contract_date)`
  - 최종계약금액(합) = `SUM(supply_amount)`
  - 대표품목 = `MIN(item_seq)` 1개(맨 앞)
- 단기·단일계약은 1행 그대로.

### 3) 납품/착수·완수일 표기
- 특정품목: `specific_item_flat.delivery_deadline`(납품기한). raw 채움률 99.99%.
- 공사/용역: `market_contract_flat.start_date`(착수/착공), `end_date`(완수/준공). raw 보유.

### 4) 공사/용역은 품목 고려 불필요
- `task_member_contract_raw`엔 `item_seq`가 없고 계약당 1건 → 계약 단위 그대로. 다품목 로직은 특정품목에만 적용.

## 대안 및 기각 사유
- **flat을 계약 단위(대표품목)로 적재 + raw 엑셀 다운로드 버튼**: 단건 화면을 품목별로 보려는 요구와 충돌하고, raw 다운로드 버튼 등 복잡도가 큼. 기각.
- **품목 식별을 `detail_item_no`로**: 같은 `item_seq` 라인 안에서 세부품명이 바뀌는 케이스(예: 분전반→분전함) 때문에 매칭이 깨짐. `item_seq` 채택.
- **최초 = 항상 '00'차수**: 신규 품목이 '00'이 아닌 차수에 처음 나오는 0.67% 케이스에서 오답. 품목별 `MIN(change_seq)` 채택.

## 영향
- V22 마이그레이션: `specific_item_flat`에 `first_item_contract_date`, `first_item_contract_amount`, `delivery_deadline` 추가 / `market_contract_flat`에 `start_date`, `end_date` 추가.
- `SpecificItemEtlService` flat 적재에 품목별 첫등장 lookup 추가, `rebuildGrouped` 산출식 변경.
- `MarketContractEtlService`에 착수/완수일 매핑 추가.
- 매퍼/API/프론트에 신규 컬럼 노출.
- ETL 재실행 필요(로컬 검증 → 운영 적재; 운영은 RDS 임시 상향 후 벌크 적재 — [[reference-prod-rds-bulk-load]] 참고).

## 데이터 근거 (로컬, 2026-06-24)
- `item_seq` 차수 간 고정 확인(샘플 `20171008DE1_1`).
- 신규 품목이 비-00 차수에 첫 등장: 2,878계약 / 432,560 (0.67%).
- 변경계약 중 품목 통합·분리(00↔최종 품목수 변동): 2,895 / 56,310 (5.1%).
- 납품기한 채움률: 1,008,717 / 1,008,812 (99.99%).
