-- 입찰공고번호 적재 (G2B-50) — market_contract_flat에 컬럼이 없어 공사/용역·탑 화면에서 항상 빈값이던 것 보완.
-- 소스: task_member_contract_raw.bid_notice_no (varchar30). ETL이 flat 적재 시 매핑.
ALTER TABLE market_contract_flat ADD COLUMN bid_notice_no VARCHAR(30) DEFAULT NULL COMMENT '입찰공고번호' AFTER contract_method;
