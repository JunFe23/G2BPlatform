-- 시장데이터 검색필터 select 옵션(distinct) 성능 (G2B-44)
-- 문제: selectDistinctWorkAreas/ContractMethods가 market_contract_flat의 contract_type 파티션(약 1.3M행)을
--   풀스캔+temp+filesort 하여 값 몇 개를 뽑음 → 조달업무영역 옵션 로드 약 4.2s.
-- 개선: (contract_type, is_active, 대상컬럼) 커버링 인덱스로 index-only(EXPLAIN: Using index) → 약 0.6s.
CREATE INDEX idx_mcf_workarea
    ON market_contract_flat (contract_type, is_active, public_procurement_major);

CREATE INDEX idx_mcf_method
    ON market_contract_flat (contract_type, is_active, contract_method);
