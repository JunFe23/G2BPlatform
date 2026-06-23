-- 레거시 보고서(물품/쇼핑몰 요약/공사·용역 grouped) 테이블 정리
-- 보고서-물품은 특정품목 통합 페이지로 대체, 쇼핑몰도 통합으로 대체.
-- 공사/용역은 task_member_contract_raw 기반으로 재구축 예정(grouped는 새로 설계).
-- TOP 보고서가 읽는 *_contract_flat / shopping_mall_flat 은 보존(삭제 제외).
-- 특정품목(specific_item_*, procurement_specific_item_raw, top_excellent_category)과 무관함을 확인함.

DROP TABLE IF EXISTS procurement_raw;                 -- 물품 보고서 소스(2,400만), 코드 직접참조 없음
DROP TABLE IF EXISTS procurement_contract_grouped;    -- 물품 보고서 grouped
DROP TABLE IF EXISTS procurement_contract_summary;    -- 물품/요약 보고서
DROP TABLE IF EXISTS shopping_mall_summary;           -- 쇼핑몰 요약
DROP TABLE IF EXISTS contract_shoppingmall;           -- 쇼핑몰 다운로드용(0건)
DROP TABLE IF EXISTS procurement_ingestion_log;       -- 물품 적재 로그
DROP TABLE IF EXISTS excellent_product_cert;          -- 조달청 우수제품 인증(물품 보고서용, 특정품목 미사용)
DROP TABLE IF EXISTS construction_contract_grouped;   -- 공사 grouped(재구축 예정)
DROP TABLE IF EXISTS service_contract_grouped;        -- 용역 grouped(재구축 예정)
