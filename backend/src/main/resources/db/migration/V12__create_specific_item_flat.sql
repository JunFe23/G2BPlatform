-- 특정품목 조달내역 통합 flat 테이블
-- procurement_specific_item_raw에서 ETL하여 물품(일반계약)과 쇼핑몰(제3자단가) 데이터를 단일 테이블로 관리
-- data_type: shopping_mall(제3자단가+납품) | general(총액·일반단가+계약)
CREATE TABLE IF NOT EXISTS specific_item_flat (
    id                      BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- 데이터 구분
    data_type               VARCHAR(20)     NOT NULL COMMENT 'shopping_mall | general',

    -- 계약 기본
    contract_no             VARCHAR(30)     NOT NULL COMMENT '계약번호',
    change_seq              VARCHAR(5)      NOT NULL DEFAULT '' COMMENT '계약변경차수',
    item_seq                SMALLINT        NOT NULL DEFAULT 1 COMMENT '품목순번',
    is_final_contract       CHAR(1)                  DEFAULT NULL COMMENT '최종계약여부 Y/N',
    is_long_term            CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '장기계약여부 Y/N',

    -- 기관
    demand_agency_code      VARCHAR(20)              DEFAULT NULL,
    demand_agency_name      VARCHAR(200)             DEFAULT NULL COMMENT '수요기관명',
    demand_agency_region    VARCHAR(100)             DEFAULT NULL COMMENT '수요기관지역',

    -- 업체
    vendor_name             VARCHAR(200)             DEFAULT NULL COMMENT '업체명',
    vendor_biz_reg_no       VARCHAR(15)              DEFAULT NULL COMMENT '사업자등록번호',
    vendor_enterprise_type  VARCHAR(30)              DEFAULT NULL COMMENT '기업구분',
    vendor_region           VARCHAR(100)             DEFAULT NULL COMMENT '업체소재지역',

    -- 계약 정보
    contract_name           VARCHAR(500)             DEFAULT NULL COMMENT '계약명',
    contract_method         VARCHAR(20)              DEFAULT NULL COMMENT '계약방법',
    bid_notice_no           VARCHAR(50)              DEFAULT NULL COMMENT '입찰공고번호',
    contract_type           VARCHAR(30)              DEFAULT NULL COMMENT '계약유형(총액/제3자단가 등)',
    delivery_contract_type  VARCHAR(10)              DEFAULT NULL COMMENT '납품유형(계약/납품)',

    -- 품목
    item_category_no        VARCHAR(10)              DEFAULT NULL COMMENT '물품분류번호',
    item_category_name      VARCHAR(100)             DEFAULT NULL COMMENT '물품분류명',
    detail_item_no          VARCHAR(15)              DEFAULT NULL COMMENT '세부품명번호',
    detail_item_name        VARCHAR(100)             DEFAULT NULL COMMENT '세부품명',
    item_identifier_no      VARCHAR(15)              DEFAULT NULL COMMENT '물품식별번호',
    item_identifier_name    VARCHAR(300)             DEFAULT NULL COMMENT '물품식별명',
    unit                    VARCHAR(30)              DEFAULT NULL COMMENT '단위',

    -- 금액 (원본 문자열 → 정수 변환, 변환 불가 시 NULL)
    unit_price              BIGINT                   DEFAULT NULL COMMENT '단가',
    quantity                BIGINT                   DEFAULT NULL COMMENT '수량',
    quantity_change         BIGINT                   DEFAULT NULL COMMENT '수량변경',
    supply_amount           BIGINT                   DEFAULT NULL COMMENT '공급금액',
    supply_amount_change    BIGINT                   DEFAULT NULL COMMENT '공급금액변경',

    -- 플래그
    is_mas                  CHAR(1)                  DEFAULT NULL COMMENT 'MAS 여부 Y/N',
    is_excellent_product    CHAR(1)                  DEFAULT NULL COMMENT '우수제품 Y/N',
    is_direct_purchase      CHAR(1)                  DEFAULT NULL COMMENT '직접구매 Y/N',
    is_sme_competitive      CHAR(1)                  DEFAULT NULL COMMENT '중기간경쟁 Y/N',

    -- 날짜
    contract_date           DATE                     DEFAULT NULL COMMENT '계약일자',
    first_contract_date     DATE                     DEFAULT NULL COMMENT '최초계약일자',

    -- 부가
    jurisdiction_type       VARCHAR(30)              DEFAULT NULL,
    delivery_location       VARCHAR(300)             DEFAULT NULL COMMENT '납품장소',
    source_file             VARCHAR(200)             DEFAULT NULL,

    -- 저장 여부 (사용자가 관심 표시)
    saved                   CHAR(1)         NOT NULL DEFAULT 'N',

    -- ETL 메타
    etl_loaded_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active               CHAR(1)         NOT NULL DEFAULT 'Y',

    UNIQUE KEY uq_specific_item_flat (contract_no, change_seq, item_seq),
    INDEX idx_data_type          (data_type),
    INDEX idx_contract_date      (contract_date),
    INDEX idx_first_contract_date (first_contract_date),
    INDEX idx_vendor_biz_reg_no  (vendor_biz_reg_no),
    INDEX idx_demand_agency_name (demand_agency_name(50)),
    INDEX idx_item_category_no   (item_category_no),
    INDEX idx_detail_item_no     (detail_item_no),
    INDEX idx_is_final_contract  (is_final_contract),
    INDEX idx_saved              (saved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='특정품목 조달내역 통합 flat 테이블 (물품+쇼핑몰, RAW ETL 결과)';
