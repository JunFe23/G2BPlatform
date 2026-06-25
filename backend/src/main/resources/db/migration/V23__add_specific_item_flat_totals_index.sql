-- G2B-34: 풀어서 보기 상단 합계(selectFlatTotals) 성능 개선
-- SELECT SUM(first_item_contract_amount), SUM(supply_amount) FROM specific_item_flat WHERE is_active='Y'
-- 위 합계가 커버링 인덱스 없이 클러스터 인덱스를 풀스캔(83만 행)하던 것을
-- (is_active, first_item_contract_amount, supply_amount) 커버링 인덱스로 index-only 처리.
CREATE INDEX idx_flat_totals
    ON specific_item_flat (is_active, first_item_contract_amount, supply_amount);
