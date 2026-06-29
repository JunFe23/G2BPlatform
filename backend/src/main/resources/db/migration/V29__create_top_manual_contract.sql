-- 탑 수주 현황 민수(직접입력) 데이터 단일 테이블 (ADR-0005 / G2B-51)
-- 관급(specific_item_flat ∪ market_contract_flat)과 달리 raw/ETL 없이
-- 이 테이블 1건이 곧 최종 1행. 조회 시 flatUnion/groupedUnion에 그대로 UNION ALL.
CREATE TABLE IF NOT EXISTS top_manual_contract (
    id                       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type                     VARCHAR(10)  NOT NULL COMMENT '분류: 물품|쇼핑몰|공사|용역',
    vendor_biz_reg_no        VARCHAR(15)  NOT NULL COMMENT '사업자번호(2社): 1188117437|1188119624',
    vendor_name              VARCHAR(200)          DEFAULT NULL COMMENT '업체명',
    contract_name            VARCHAR(500)          DEFAULT NULL COMMENT '계약건명',
    demand_agency_name       VARCHAR(200)          DEFAULT NULL COMMENT '수요기관명',
    demand_agency_region     VARCHAR(100)          DEFAULT NULL COMMENT '수요기관지역명',
    mid_category             VARCHAR(100)          DEFAULT NULL COMMENT '중분류(자유 텍스트)',
    product_classification   VARCHAR(200)          DEFAULT NULL COMMENT '품명내용/소분류(자유 텍스트)',
    contract_method          VARCHAR(50)           DEFAULT NULL COMMENT '입찰계약방법',
    bid_notice_no            VARCHAR(30)           DEFAULT NULL COMMENT '입찰공고번호',
    first_contract_date      DATE                  DEFAULT NULL COMMENT '최초계약일자',
    first_contract_amount    BIGINT                DEFAULT NULL COMMENT '최초계약금액',
    last_contract_date       DATE                  DEFAULT NULL COMMENT '최종계약일자',
    last_contract_amount     BIGINT                DEFAULT NULL COMMENT '최종계약금액',
    contract_change_seq      INT          NOT NULL DEFAULT 0 COMMENT '계약변경차수',
    data_origin              VARCHAR(10)  NOT NULL DEFAULT '민수' COMMENT '데이터구분(고정: 민수)',
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tmc_vendor (vendor_biz_reg_no),
    INDEX idx_tmc_type (type),
    INDEX idx_tmc_order (last_contract_date DESC, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='탑 수주 현황 민수(직접입력) 데이터';
