-- G2B-58: 탑 수주 현황 민수 데이터도 관급 데이터처럼 저장 필터/토글 대상에 포함.
ALTER TABLE top_manual_contract
    ADD COLUMN saved CHAR(1) NOT NULL DEFAULT 'N' COMMENT '저장 여부(Y/N)' AFTER data_origin,
    ADD INDEX idx_tmc_saved (saved);
