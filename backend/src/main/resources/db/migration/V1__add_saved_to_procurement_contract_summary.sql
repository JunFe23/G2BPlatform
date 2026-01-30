-- 보고서 물품 페이지에서 "저장" 체크 상태를 DB에 반영하기 위한 컬럼
-- (Flyway 미사용 시 수동 실행: mysql -u ... -p ... < 이 파일)
ALTER TABLE procurement_contract_summary
  ADD COLUMN saved CHAR(1) DEFAULT 'N' COMMENT '저장여부 Y/N' AFTER is_long_term;

ALTER TABLE procurement_contract_summary
  ADD KEY idx_saved (saved);
