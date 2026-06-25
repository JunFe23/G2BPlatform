-- G2B-32: 특정품목 품목별 최초계약일자/금액 + 납품기한, 공사/용역 착수·완수일
-- ADR-0004 참고

-- 1) specific_item_flat: 품목(item_seq)별 첫 등장 차수의 계약일자/금액 + 납품기한
ALTER TABLE specific_item_flat
  ADD COLUMN first_item_contract_date   DATE   DEFAULT NULL COMMENT '품목 첫 등장 차수의 계약일자(최초계약일자)',
  ADD COLUMN first_item_contract_amount BIGINT DEFAULT NULL COMMENT '품목 첫 등장 차수의 공급금액(최초계약금액)',
  ADD COLUMN delivery_deadline          DATE   DEFAULT NULL COMMENT '납품기한';

-- 2) market_contract_flat(공사/용역): 착수/완수(착공/준공) 일자
ALTER TABLE market_contract_flat
  ADD COLUMN start_date DATE DEFAULT NULL COMMENT '착수일자(기술용역)/착공일자(공사)',
  ADD COLUMN end_date   DATE DEFAULT NULL COMMENT '완수일자(기술용역)/준공일자(공사)';
