-- 특정품목 통합 조회(풀어서 보기) '우수제품만 보기' 필터 성능 개선
-- WHERE is_active='Y' AND is_excellent_product='Y' ORDER BY contract_date DESC, contract_no, item_seq
-- 기존 idx_flat_list_order에는 is_excellent_product가 없어 count가 87만 엔트리 전체를 필터링(약 1.47s).
-- is_excellent_product를 정렬키 앞에 둔 전용 복합 인덱스로 count/list를 커버링 처리.
CREATE INDEX idx_flat_excellent_order
    ON specific_item_flat (is_active, is_excellent_product, contract_date DESC, contract_no ASC, item_seq ASC);
