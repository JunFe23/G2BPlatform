-- 물품분류 옵션 조회(SpecificItemMapper.selectItemCategories) 성능 개선.
-- 기존: SELECT DISTINCT item_category_no, item_category_name FROM specific_item_flat WHERE is_active='Y' ...
--   item_category_name이 어떤 인덱스에도 없어 885만행을 풀스캔해 dedup(distinct 약 27개) → 매 입력마다 느림.
-- 개선: (is_active, item_category_no, item_category_name) 커버링 인덱스로
--   DISTINCT를 loose index scan(index-only)으로 처리(EXPLAIN: Using index for group-by).
CREATE INDEX idx_flat_category_options
    ON specific_item_flat (is_active, item_category_no, item_category_name);
