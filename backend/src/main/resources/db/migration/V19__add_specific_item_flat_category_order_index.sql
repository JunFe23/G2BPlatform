-- 자사 우수제품(물품분류번호 IN) 필터 조회 성능 개선
-- 자사 우수제품 count(WHERE is_active='Y' AND item_category_no IN (...))가
-- 기존 인덱스로 41만행을 스캔하던 것을 커버링 인덱스로 처리(약 1.58s → 0.11s).
-- list는 idx_flat_list_order(계약일자 정렬)를 사용하며, 리터럴 IN 전달로 filesort를 제거한다.
CREATE INDEX idx_flat_category_order
    ON specific_item_flat (is_active, item_category_no, contract_date DESC, contract_no ASC, item_seq ASC);
