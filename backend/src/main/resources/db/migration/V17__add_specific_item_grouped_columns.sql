-- 특정품목 grouped 표시 항목 보강
-- - contract_name      : 계약명 (요구 1)
-- - is_mas             : MAS여부 (요구 2, MAS/3자단가 구분)
-- - item_category_name : 물품분류명 (요구 5, 세부품명 대신 표시)
-- - initial_contract_amount : 최초계약금액 (요구 4, 그룹 내 가장 이른 contract_date 건들의 금액 합)
ALTER TABLE specific_item_grouped
    ADD COLUMN contract_name           VARCHAR(500) DEFAULT NULL COMMENT '계약명'         AFTER demand_agency_region,
    ADD COLUMN is_mas                  CHAR(1)      DEFAULT NULL COMMENT 'MAS여부 Y/N'    AFTER contract_type,
    ADD COLUMN item_category_name      VARCHAR(100) DEFAULT NULL COMMENT '물품분류명'      AFTER item_category_no,
    ADD COLUMN initial_contract_amount BIGINT NOT NULL DEFAULT 0 COMMENT '최초계약금액(가장 이른 contract_date 건 합)' AFTER total_supply_amount;
