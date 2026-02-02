-- 보고서 물품 "저장된 데이터만 보기" + 최신순 정렬 쿼리 튜닝용 복합 인덱스
-- (saved 필터 후 first_contract_date DESC 정렬 시 filesort 제거)
-- Flyway 미사용 시 수동 실행: mysql -u ... -p ... < 이 파일
ALTER TABLE procurement_contract_summary
  ADD KEY idx_saved_first_contract_date (saved, first_contract_date, bid_notice_no, vendor_biz_reg_no);
