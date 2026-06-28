-- 탑 수주현황(2社 한정) 조회 성능 (G2B-49 / ADR-0005)
-- specific_item_flat은 idx_vendor_biz_reg_no 보유. market_contract_flat/grouped엔 vendor 인덱스 없어
-- vendor_biz_reg_no IN (2社) 필터가 풀스캔 → 인덱스 추가(매우 선택적, 816/수백만).
CREATE INDEX idx_mcf_vendor ON market_contract_flat (vendor_biz_reg_no);
CREATE INDEX idx_mcg_vendor ON market_contract_grouped (vendor_biz_reg_no);
