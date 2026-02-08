-- 보고서 물품 엑셀 Keyset 페이지네이션 튜닝
-- ORDER BY first_contract_date DESC, bid_notice_no, vendor_biz_reg_no + (date,bid,vendor) < (...) 조건에 사용
-- Flyway 미사용 시 수동 실행: mysql -u ... -p ... < 이 파일
ALTER TABLE procurement_contract_summary
  ADD KEY idx_first_date_bid_vendor (first_contract_date, bid_notice_no, vendor_biz_reg_no);
