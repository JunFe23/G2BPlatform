-- 특정품목 통합 조회(풀어서 보기) 기본 정렬/필터 성능 개선
-- selectFlatList: WHERE is_active='Y' ORDER BY contract_date DESC, contract_no, item_seq LIMIT
-- selectFlatCount: WHERE is_active='Y'
-- 위 두 쿼리가 풀 테이블 스캔 + filesort(821k행)를 수행하던 것을
-- (is_active, contract_date DESC, contract_no, item_seq) 복합 인덱스로 커버링 처리.
CREATE INDEX idx_flat_list_order
    ON specific_item_flat (is_active, contract_date DESC, contract_no ASC, item_seq ASC);
