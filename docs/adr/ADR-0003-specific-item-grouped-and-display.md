# ADR-0003: 특정품목 통합 grouped 집계 및 통합 조회 화면 표시 기준

- 상태: 채택(Accepted)
- 작성일: 2026-06-17
- 관련 티켓: G2B-16, G2B-20
- 관련 코드:
  - `backend/.../service/SpecificItemEtlService.java` (`rebuildGrouped`)
  - `backend/.../mapper/SpecificItemMapper.xml`
  - `backend/src/main/resources/db/migration/V14`, `V17`
  - `frontend/src/views/ReportSpecificItemView.vue`

## 배경

특정품목 조달내역(물품·쇼핑몰)을 단일 화면에서 조회하는 통합 페이지를 운영한다.
조회 성능과 "장기계약 묶어서 보기", 저장(saved) 독립 관리를 위해
`specific_item_flat`(상세) → `specific_item_grouped`(집계) 2단계 구조를 사용한다.
본 ADR은 grouped 집계 기준과 화면 표시 항목의 결정 사항을 기록한다.

## 결정

### 1) 물품 / 쇼핑몰 구분
- `contract_type='제3자단가계약' AND delivery_contract_type='납품'` → `shopping_mall`
- 그 외 전부 → `general`
- ETL(`toParams`)에서 `data_type`으로 산출한다.

### 2) 장기계약 묶음(grouped) 기준
- 묶음 키 = `data_type` + `COALESCE(first_year_contract_no, contract_no)` + `vendor_biz_reg_no`
- 초년도계약번호(`first_year_contract_no`)가 있으면 그 값으로, 없으면 자기 `contract_no`로 묶인다.
  - 장기계속계약: 같은 초년도계약번호의 연차 계약들이 1행으로 병합
  - 단기·단일계약: 개별 1행
- 장기 판정 플래그(`is_long_term`)는 **묶음 기준이 아니며**, `new_long_term_type` ∈ {신규(장기), 장기, 계속비} 일 때 'Y'로 화면 배지 표시용으로만 쓴다.

### 3) flat 적재 범위
- `business_type='물품' AND is_final_contract='Y'` 인 행만 flat에 적재(증분, `INSERT IGNORE`).
- 즉 계약별 **최종 변경분 1행(+품목별 item_seq)** 만 보관한다.

### 4) 날짜·금액 기준은 contract_date로 통일
변경계약이 있는 경우 원계약일(`first_contract_date`)은 의미가 약하므로,
grouped의 날짜·금액 산출은 모두 `contract_date`(최종 변경 반영 계약일) 기준으로 한다.

- 최초계약일자 = `MIN(contract_date)` (그룹 내 가장 이른 계약일)
- 최종계약일자 = `MAX(contract_date)`
- 최초계약금액 = `SUM(supply_amount WHERE contract_date = MIN(contract_date))`
  (그룹에서 가장 이른 계약일 건들의 공급금액 합 = 장기계약의 첫 연차 금액)
- 최종계약금액 합계 = `SUM(supply_amount)` (그룹 전체 합)

이는 기존 `procurement_contract_grouped`를 채우는 `sp_etl_procurement_contracts`의
`initial_contract_amount`/`final_contract_amount_sum` 산출 방식과 동일하다.

### 5) 화면 표시 항목 (G2B-20)
- 계약명(`contract_name`) 컬럼 추가 (flat·grouped)
- MAS/3자단가 구분: `is_mas`(다수공급자계약 여부) 컬럼 노출
- 표시 품명: 세부품명 대신 **물품분류명**(`item_category_name`, 100% 적재)으로 변경
- 묶어서 보기 상단에 최초/최종 계약금액 합계 표기(`selectGroupedTotals`)
- 입찰공고번호는 원천(특정품목 조달내역 CSV)에 존재하지 않아 미표시
  (현 `bid_notice_no` 컬럼은 실제로 `base_contract_no`(계약번호)를 담고 있어 추후 정리 대상)

## 대안 및 기각 사유
- **DB View로 grouped 처리**: 실시간 GROUP BY라 대용량(45만 그룹)에서 조회가 155초까지 느려지고,
  묶음 단위 saved 저장 컬럼을 둘 수 없어 기각. 물리 테이블 + ETL 집계 채택.
- **최초계약금액을 변경 0차 금액으로 산출**: 특정품목 CSV에 최초계약금액 컬럼이 없고,
  flat은 최종행만 보관하며 일부 계약은 raw에도 초기 차수가 누락되어 복원 불가. 기각.
  → 그룹 내 "가장 이른 contract_date 건 금액"을 최초계약금액으로 정의.

## 영향
- V17 마이그레이션으로 grouped에 `contract_name`, `is_mas`, `item_category_name`, `initial_contract_amount` 추가.
- ETL `rebuildGrouped`는 매 실행 시 grouped를 TRUNCATE 후 재집계하며 saved는 보존한다.
- CSV 업로드 → raw → flat → grouped까지 자동 파이프라인으로 처리된다.
