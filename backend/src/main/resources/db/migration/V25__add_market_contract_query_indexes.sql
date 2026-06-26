-- 시장데이터(공사/용역) 조회 성능 튜닝 (G2B-43)
-- 문제: 목록 정렬 컬럼이 인덱스와 불일치하여 filesort, totals는 contract_type 파티션 풀스캔.
--   flat  목록: ORDER BY contract_date DESC, contract_no  (기존 idx_mcf_order는 first_contract_date 기준)
--   grouped 목록: ORDER BY first_contract_date DESC, group_key, vendor_biz_reg_no (기존 idx_mcg_last는 last_contract_date 기준)

-- 1) flat 목록 정렬·기간(contract_date) 정합 인덱스 → filesort 제거
CREATE INDEX idx_mcf_cdate
    ON market_contract_flat (contract_type, contract_date DESC, contract_no);

-- 2) grouped 목록 정렬·기간(first_contract_date) 정합 인덱스 → filesort 제거
CREATE INDEX idx_mcg_first
    ON market_contract_grouped (contract_type, first_contract_date DESC, group_key, vendor_biz_reg_no);

-- 3) flat 상단 합계 커버링(필터 없는 첫 로드 index-only)
CREATE INDEX idx_mcf_totals
    ON market_contract_flat (contract_type, is_active, first_contract_amount, total_contract_amount);

-- 4) grouped 상단 합계 커버링
CREATE INDEX idx_mcg_totals
    ON market_contract_grouped (contract_type, initial_contract_amount, total_contract_amount_sum);
