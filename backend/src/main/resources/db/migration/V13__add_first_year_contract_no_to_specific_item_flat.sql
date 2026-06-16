ALTER TABLE specific_item_flat
    ADD COLUMN first_year_contract_no VARCHAR(30) DEFAULT NULL COMMENT '초년도계약번호 (장기계속계약 묶음 키)' AFTER is_long_term,
    ADD INDEX idx_first_year_contract_no (first_year_contract_no);
