-- 특정품목 통합 grouped 테이블
-- specific_item_flat에서 ETL 시점에 미리 집계해 두는 물리 테이블.
-- 단기/단일계약은 1행 그대로, 장기계약은 초년도계약번호(first_year_contract_no) 기준으로 연차를 1행으로 병합.
-- 묶음 키(group_key) = COALESCE(first_year_contract_no, contract_no)
CREATE TABLE IF NOT EXISTS specific_item_grouped (
    id                      BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- 묶음 키
    data_type               VARCHAR(20)     NOT NULL COMMENT 'shopping_mall | general',
    group_key               VARCHAR(30)     NOT NULL COMMENT 'COALESCE(초년도계약번호, 계약번호)',
    vendor_biz_reg_no       VARCHAR(15)              DEFAULT NULL COMMENT '사업자등록번호',

    -- 대표/집계 표시값
    first_year_contract_no  VARCHAR(30)              DEFAULT NULL COMMENT '초년도계약번호 (장기계약만)',
    contract_no             VARCHAR(30)              DEFAULT NULL COMMENT '대표 계약번호 MIN',
    vendor_name             VARCHAR(200)             DEFAULT NULL COMMENT '업체명',
    demand_agency_name      VARCHAR(200)             DEFAULT NULL COMMENT '수요기관명',
    demand_agency_region    VARCHAR(100)             DEFAULT NULL COMMENT '수요기관지역',
    contract_method         VARCHAR(20)              DEFAULT NULL COMMENT '계약방법',
    contract_type           VARCHAR(30)              DEFAULT NULL COMMENT '계약유형',
    item_category_no        VARCHAR(10)              DEFAULT NULL COMMENT '물품분류번호',
    detail_item_name        VARCHAR(100)             DEFAULT NULL COMMENT '세부품명',
    is_long_term            CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '장기계약여부 Y/N',

    -- 집계값
    first_contract_date     DATE                     DEFAULT NULL COMMENT '최초계약일자 MIN',
    last_contract_date      DATE                     DEFAULT NULL COMMENT '최종계약일자 MAX',
    total_supply_amount     BIGINT          NOT NULL DEFAULT 0 COMMENT '공급금액 합산',
    contract_count          INT             NOT NULL DEFAULT 1 COMMENT '묶인 계약번호 수',

    -- 저장 여부 (묶음 단위 독립 관리)
    saved                   CHAR(1)         NOT NULL DEFAULT 'N',

    etl_loaded_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_specific_item_grouped (data_type, group_key, vendor_biz_reg_no),
    INDEX idx_grp_data_type          (data_type),
    INDEX idx_grp_last_contract_date (last_contract_date),
    INDEX idx_grp_demand_agency_name (demand_agency_name(50)),
    INDEX idx_grp_vendor_biz_reg_no  (vendor_biz_reg_no),
    INDEX idx_grp_item_category_no   (item_category_no),
    INDEX idx_grp_saved              (saved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='특정품목 통합 grouped 테이블 (장기계약 초년도번호 묶음, ETL 집계 결과)';
