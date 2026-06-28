# ADR-0005: 탑 수주 현황(TOP) 페이지 — 신 통합테이블 재소싱 + 통합 분류 트리 + 민수 데이터

- **Status**: Accepted
- **Date**: 2026-06-29
- **Owners**: G2B (Jun Fe)

## Context

- '탑인더스트리 & 탑정보통신 수주현황' 페이지(`TopContractsReportView`, `/api/report/top-companies`)는 탑인더스트리(1188117437)·탑정보통신(1188119624) 2社의 계약만 한 화면에서 본다.
- 현재 `TopCompaniesReportMapper`는 **구 4테이블**(`procurement_contract_flat`+`construction_contract_flat`+`service_contract_flat`+`shopping_mall_flat`)을 UNION한다.
  - 그런데 `construction_contract_flat`·`service_contract_flat`는 **V21에서 DROP** → 존재하지 않는 테이블 참조 → **현재 페이지 쿼리가 깨짐**(보류돼 있던 G2B-28 미처리).
- 시장데이터 3페이지가 실제 쓰는 flat 테이블: 물품=`specific_item_flat`, 공사·용역=`market_contract_flat`. (`procurement_contract_flat`은 구 물품 테이블로 `specific_item_flat`에 의해 대체됨.)
- 요구: ① 신 테이블로 재소싱(깨진 페이지 복구) ② 관급(보고서 CSV)·민수(직접입력) 동시 조회 ③ 공공조달분류명 필터 ④ 상단 최초/최종 금액 합계 ⑤ 민수 입력 기능(관리자) ⑥ 민수는 현재 이 페이지에서만 ⑦ 성능.
- 분류 체계 차이: 물품은 `item_category(분류)→detail_item(품명)` 2단(물품분류체계), 공사·용역은 `public_procurement_major→mid→name`(공공조달분류체계 대/중/소). 코드 체계가 서로 다름.

## Decision

- **데이터 소스**: TOP 페이지는 **`specific_item_flat` ∪ `market_contract_flat`**(2社 필터)로 재소싱. 합쳐서보기는 `specific_item_grouped` ∪ `market_contract_grouped`. (구 `procurement_contract_flat` 등 미사용.)
  - 공통 컬럼 alias: 분류(data_type/contract_type→물품/쇼핑몰/공사/용역), 업체명, 계약건명, 수요기관명·지역, 품명내용, 입찰계약방법, 입찰공고번호, 최초계약일자/금액, 최종계약일자/금액, 계약변경차수, saved.
  - 최초/최종 기준은 물품·공사·용역 모두 동일(ADR-0004 + market ETL): flat=원계약/최종차수, grouped=MIN(최초일)·SUM(최초금액)·MAX(최종일)·SUM(최종금액).
- **통합 분류 트리**: 물품의 `item_category→detail_item`을 `중→소`로 매핑해 공사·용역의 `mid→name`과 **하나의 `CategoryTreeSelect` 2단 패널**로 통합.
  - 중분류 = `item_category_name`(물품) ∪ `public_procurement_mid`(공사·용역), 소분류=품명내용 = `detail_item_name`(물품) ∪ `public_procurement_name`(공사·용역).
  - 계층은 2社 실제 데이터에서 동적 생성(2社 한정이라 중 ~31 / 소 ~37로 작음). 필터는 통합 품명내용 `IN`.
  - → "분류 선택에 따라 동적 위젯 swap"은 불필요(통합 트리로 단순화).
- **민수 데이터**: 신 테이블 `top_manual_contract`(분류·2社·통합컬럼·data_origin='민수'). CRUD는 **ROLE_ADMIN 전용**. 조회 UNION에 포함하되 **현재 TOP 페이지에서만** 노출(대시보드 미반영).
- **필터**: 분류(전체/물품/공사/용역/쇼핑몰), 관급/민수(전체/관급/민수), 통합 공공조달분류명 트리, 계약명, 입찰계약방법(select).
- **성능**: vendor 필터가 매우 선택적(816/수백만) → `market_contract_flat`/`grouped`에 `vendor_biz_reg_no` 인덱스 추가. specific_item은 기존 `idx_vendor_biz_reg_no`.
- **롤아웃**: 티켓 A(관급 재소싱+필터+합계+인덱스, 깨진 페이지 복구) → 티켓 B(민수 테이블+CRUD+폼+관급/민수 필터). 정리(티켓 C/G2B-29: `procurement_contract_flat`·`shopping_mall_flat`·`ProcurementContractMapper`·구 saved 제거)는 **시장현황 대시보드까지 재소싱 후** 가능(현재 `ProcurementContractSummaryMapper`가 라이브로 읽음).

## Consequences

### Positive

- 깨진 TOP 페이지 복구 + 시장데이터 페이지와 동일 데이터 기준.
- 물품/공사/용역을 하나의 분류 트리·합계로 일관 조회. 관급+민수 통합 뷰.
- 구 테이블 제거의 선행조건 충족(재소싱 후 G2B-29로 청소 가능).

### Negative / Risks

- 통합 분류 트리에 물품(물품분류)·공사용역(공공조달분류) 코드가 섞임 — 좌측 패널에 이질적 명칭 혼재(2社 소량이라 수용 가능).
- 민수 입력은 수동 데이터라 정합성은 입력자 책임(관급과 키 충돌 없음 — data_origin 구분).
- 정리(G2B-29)는 시장현황 대시보드 재소싱 의존 → 별도 선행 필요.

## Alternatives considered

- **구 테이블 유지 + DROP된 것만 복구**: V21 DROP을 되돌리는 셈, 신 파이프라인과 이중관리 → 기각.
- **분류 필터를 분류별 동적 위젯 swap**: 물품=콤보박스/공사용역=2단패널 전환 → UI 복잡. 통합 트리로 단순화하여 기각.
- **품명내용 단일 텍스트 검색**: 가장 단순하나 사용자 요구(트리형 선택) 미충족 → 보조(전체 선택 시 fallback)로만.
