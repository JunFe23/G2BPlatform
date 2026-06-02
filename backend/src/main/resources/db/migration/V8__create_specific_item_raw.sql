-- 특정품목 조달 내역 RAW 테이블
-- 조달데이터허브에서 다운로드한 '특정품목 조달 내역' CSV를 원본 그대로 적재.
-- 물품(총액/일반단가)과 쇼핑몰(제3자단가) 모두 동일 테이블에 저장하며,
-- ETL 단계에서 contract_type 기준으로 분기하여 조회용 테이블로 변환.
CREATE TABLE IF NOT EXISTS procurement_specific_item_raw (
    id                          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- 조달/계약 분류 (원본 CSV 1~4열)
    procurement_method          VARCHAR(20)     NOT NULL DEFAULT '' COMMENT '조달방식 (자체조달/중앙조달)',
    business_type               VARCHAR(20)     NOT NULL DEFAULT '' COMMENT '업무구분 (물품)',
    contract_type               VARCHAR(30)     NOT NULL DEFAULT '' COMMENT '계약구분 (일반단가계약/제3자단가계약/총액계약)',
    delivery_contract_type      VARCHAR(10)              DEFAULT NULL COMMENT '계약납품구분 (계약/납품요구)',

    -- 계약(납품요구) 식별 — UNIQUE KEY 구성 컬럼 (원본 CSV 5~9열)
    contract_date               CHAR(8)                  DEFAULT NULL COMMENT '계약(납품요구)일자 YYYYMMDD',
    contract_no                 VARCHAR(30)     NOT NULL COMMENT '계약(납품요구)번호',
    change_seq                  VARCHAR(5)      NOT NULL DEFAULT '' COMMENT '변경차수',
    item_seq                    SMALLINT        NOT NULL DEFAULT 1   COMMENT '물품순번',
    is_final_contract           CHAR(1)                  DEFAULT NULL COMMENT '최종계약(납품요구)여부 Y/N',

    -- 수요기관 (원본 CSV 10~13열)
    demand_agency_code          VARCHAR(20)              DEFAULT NULL COMMENT '수요기관코드',
    demand_agency_name          VARCHAR(200)             DEFAULT NULL COMMENT '수요기관',
    jurisdiction_type           VARCHAR(30)              DEFAULT NULL COMMENT '소관구분',
    demand_agency_region        VARCHAR(100)             DEFAULT NULL COMMENT '수요기관소재시군구',

    -- 물품 정보 (원본 CSV 14~19열)
    item_category_no            VARCHAR(10)              DEFAULT NULL COMMENT '물품분류번호',
    item_category_name          VARCHAR(100)             DEFAULT NULL COMMENT '품명',
    detail_item_no              VARCHAR(15)              DEFAULT NULL COMMENT '세부품명번호',
    detail_item_name            VARCHAR(100)             DEFAULT NULL COMMENT '세부품명',
    item_identifier_no          VARCHAR(15)              DEFAULT NULL COMMENT '물품식별번호',
    item_name                   VARCHAR(300)             DEFAULT NULL COMMENT '품목',

    -- 업체 정보 (원본 CSV 20~23열)
    vendor_name                 VARCHAR(200)             DEFAULT NULL COMMENT '업체명',
    vendor_biz_reg_no           VARCHAR(15)              DEFAULT NULL COMMENT '사업자등록번호',
    vendor_enterprise_type      VARCHAR(30)              DEFAULT NULL COMMENT '계약시점 기업구분',
    vendor_region               VARCHAR(100)             DEFAULT NULL COMMENT '계약시점 업체소재시도',

    -- 계약명 (원본 CSV 24열)
    contract_name               VARCHAR(500)             DEFAULT NULL COMMENT '계약(납품요구)명',

    -- 제품 특성 플래그 Y/N (원본 CSV 25~29열)
    is_excellent_product        CHAR(1)                  DEFAULT NULL COMMENT '우수제품여부 Y/N',
    is_direct_purchase          CHAR(1)                  DEFAULT NULL COMMENT '직접구매대상여부 Y/N',
    is_mas                      CHAR(1)                  DEFAULT NULL COMMENT 'MAS여부 Y/N',
    is_two_stage_competition    CHAR(1)                  DEFAULT NULL COMMENT '이단계경쟁제안서제출여부 Y/N',
    is_sme_competitive          CHAR(1)                  DEFAULT NULL COMMENT '중기간경쟁물품여부 Y/N',

    -- 최초계약일 (원본 CSV 30열)
    first_contract_date         CHAR(8)                  DEFAULT NULL COMMENT '최초계약(납품요구)일자 YYYYMMDD',

    -- 원계약 번호 — 제3자단가계약인 경우 단가계약 원본 번호 (원본 CSV 31~32열)
    base_contract_no            VARCHAR(30)              DEFAULT NULL COMMENT '계약번호',
    base_contract_change_seq    VARCHAR(5)               DEFAULT NULL COMMENT '계약변경차수',

    -- 계약 방법 (원본 CSV 33~36열)
    contract_method             VARCHAR(20)              DEFAULT NULL COMMENT '계약방법',
    bid_method                  VARCHAR(50)              DEFAULT NULL COMMENT '낙찰방법',
    applicable_law              VARCHAR(20)              DEFAULT NULL COMMENT '적용계약법',
    clause_content              VARCHAR(200)             DEFAULT NULL COMMENT '조항호내용',

    -- 장기계약 정보 (원본 CSV 37~40열)
    new_long_term_type          VARCHAR(50)              DEFAULT NULL COMMENT '신규장기구분',
    is_long_term_first_year     VARCHAR(10)              DEFAULT NULL COMMENT '장기초년도계약여부',
    first_year_contract_no      VARCHAR(30)              DEFAULT NULL COMMENT '초년도계약번호',
    long_term_sequence          VARCHAR(5)               DEFAULT NULL COMMENT '장기계속차수',

    -- 납품 정보 (원본 CSV 41~44열)
    delivery_location           VARCHAR(300)             DEFAULT NULL COMMENT '납품장소명',
    delivery_deadline           VARCHAR(9)               DEFAULT NULL COMMENT '납품기한 (YYYYMMDD 또는 빈값)',
    delivery_condition          VARCHAR(50)              DEFAULT NULL COMMENT '인도조건',
    unit                        VARCHAR(30)              DEFAULT NULL COMMENT '단위',

    -- 금액/수량 원본 문자열 보존 (원본 CSV 45~49열, 쉼표 포함)
    unit_price_raw              VARCHAR(30)              DEFAULT NULL COMMENT '계약납품단가 (원본)',
    quantity_raw                VARCHAR(20)              DEFAULT NULL COMMENT '계약납품수량 (원본)',
    quantity_change_raw         VARCHAR(20)              DEFAULT NULL COMMENT '계약납품증감수량 (원본)',
    supply_amount_raw           VARCHAR(30)              DEFAULT NULL COMMENT '공급금액 (원본)',
    supply_amount_change_raw    VARCHAR(30)              DEFAULT NULL COMMENT '공급증감금액 (원본)',

    -- 시스템 메타
    source_file                 VARCHAR(200)             DEFAULT NULL COMMENT '업로드 원본 파일명',
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_specific_item      (contract_no, change_seq, item_seq),
    INDEX      idx_contract_date     (contract_date),
    INDEX      idx_first_contract_date (first_contract_date),
    INDEX      idx_contract_type     (contract_type),
    INDEX      idx_item_category_no  (item_category_no),
    INDEX      idx_vendor_biz_reg_no (vendor_biz_reg_no),
    INDEX      idx_is_final_contract (is_final_contract)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='특정품목 조달 내역 RAW 테이블 (조달데이터허브)';
