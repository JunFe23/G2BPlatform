-- 업무별 구성원별 계약내역 RAW 테이블
-- 조달데이터허브 '업무별 구성원별 계약내역' CSV를 원본 그대로 적재.
-- 기술용역(engineering)과 공사(construction) 데이터를 단일 테이블에 저장하며,
-- contract_type 컬럼으로 구분. 날짜 컬럼(착수/완수)은 명칭이 다르나 의미가 동일하여 통합.
CREATE TABLE IF NOT EXISTS task_member_contract_raw (
    id                          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- 업종 구분
    contract_type               VARCHAR(20)     NOT NULL COMMENT '업종구분: service(기술용역) | construction(공사)',

    -- 조달/계약 분류 (원본 CSV 1~4열)
    procurement_method          VARCHAR(20)     NOT NULL DEFAULT '' COMMENT '조달방식',
    contract_no                 VARCHAR(30)     NOT NULL COMMENT '계약번호',
    change_seq                  VARCHAR(5)      NOT NULL DEFAULT '' COMMENT '계약변경차수',
    is_final_contract           CHAR(1)                  DEFAULT NULL COMMENT '최종계약여부 Y/N',

    -- 계약 기본 정보 (원본 CSV 5~9열)
    contract_date               CHAR(8)                  DEFAULT NULL COMMENT '계약일자 YYYYMMDD',
    first_contract_date         CHAR(8)                  DEFAULT NULL COMMENT '최초계약일자 YYYYMMDD',
    contract_name               VARCHAR(500)             DEFAULT NULL COMMENT '계약명',
    demand_agency_code          VARCHAR(20)              DEFAULT NULL COMMENT '수요기관코드',
    demand_agency_name          VARCHAR(200)             DEFAULT NULL COMMENT '수요기관',

    -- 입찰/낙찰 정보 (원본 CSV 10~15열)
    contract_method             VARCHAR(20)              DEFAULT NULL COMMENT '계약방법',
    bid_notice_no               VARCHAR(30)              DEFAULT NULL COMMENT '입찰공고번호',
    bid_notice_seq              VARCHAR(5)               DEFAULT NULL COMMENT '입찰공고차수',
    bid_method                  VARCHAR(50)              DEFAULT NULL COMMENT '낙찰방법',
    applicable_law              VARCHAR(20)              DEFAULT NULL COMMENT '적용계약법',
    clause_content              VARCHAR(200)             DEFAULT NULL COMMENT '조항호내용',

    -- 장기계약 정보 (원본 CSV 16~19열)
    new_long_term_type          VARCHAR(50)              DEFAULT NULL COMMENT '신규장기구분',
    is_long_term_first_year     CHAR(1)                  DEFAULT NULL COMMENT '장기초년도계약여부 Y/N',
    first_year_contract_no      VARCHAR(30)              DEFAULT NULL COMMENT '초년도계약번호',
    long_term_sequence          VARCHAR(5)               DEFAULT NULL COMMENT '장기계속차수',

    -- 공공조달 분류 (원본 CSV 20~22열)
    public_procurement_type     VARCHAR(30)              DEFAULT NULL COMMENT '공공조달분류',
    public_procurement_major    VARCHAR(50)              DEFAULT NULL COMMENT '공공조달대분류',
    public_procurement_minor    VARCHAR(50)              DEFAULT NULL COMMENT '공공조달중분류',

    -- 현장/일정 정보 (원본 CSV 23~26열)
    work_location               VARCHAR(100)             DEFAULT NULL COMMENT '공사현장(기술용역)/공사현장(공사)',
    start_date                  CHAR(8)                  DEFAULT NULL COMMENT '착수일자(기술용역) / 착공일자(공사) YYYYMMDD',
    end_date                    CHAR(8)                  DEFAULT NULL COMMENT '완수일자(기술용역) / 준공일자(공사) YYYYMMDD',
    total_end_date              CHAR(8)                  DEFAULT NULL COMMENT '총완수일자(기술용역) / 총준공일자(공사) YYYYMMDD',

    -- 업체 정보 (원본 CSV 27~39열)
    vendor_enterprise_type      VARCHAR(30)              DEFAULT NULL COMMENT '계약시점 기업구분',
    vendor_region               VARCHAR(100)             DEFAULT NULL COMMENT '계약시점 업체소재시군구',
    is_female_enterprise        CHAR(1)                  DEFAULT NULL COMMENT '계약시점 여성기업인증여부 Y/N',
    is_disabled_enterprise      CHAR(1)                  DEFAULT NULL COMMENT '계약시점 장애인기업인증여부 Y/N',
    is_social_enterprise        CHAR(1)                  DEFAULT NULL COMMENT '계약시점 사회적기업인증여부 Y/N',
    jurisdiction_type           VARCHAR(30)              DEFAULT NULL COMMENT '소관구분',
    demand_agency_region        VARCHAR(100)             DEFAULT NULL COMMENT '수요기관소재시군구',
    joint_contract_method       VARCHAR(30)              DEFAULT NULL COMMENT '공동도급방식',
    work_type                   VARCHAR(50)              DEFAULT NULL COMMENT '공종',
    is_representative           CHAR(1)                  DEFAULT NULL COMMENT '대표업체여부 Y/N',
    vendor_name                 VARCHAR(200)             DEFAULT NULL COMMENT '업체명',
    vendor_biz_reg_no           VARCHAR(15)              DEFAULT NULL COMMENT '사업자등록번호',
    representative_name         VARCHAR(50)              DEFAULT NULL COMMENT '대표자명',

    -- 금액 정보 (원본 CSV 40~45열, 쉼표 포함 원본 보존)
    contract_share_rate_raw     VARCHAR(20)              DEFAULT NULL COMMENT '계약지분율 (원본)',
    contract_share_change_raw   VARCHAR(30)              DEFAULT NULL COMMENT '계약지분증감금액 (원본)',
    contract_share_amount_raw   VARCHAR(30)              DEFAULT NULL COMMENT '계약지분금액 (원본)',
    current_contract_amount_raw VARCHAR(30)              DEFAULT NULL COMMENT '금차계약금액 (원본)',
    first_contract_amount_raw   VARCHAR(30)              DEFAULT NULL COMMENT '최초계약금액 (원본)',
    total_contract_amount_raw   VARCHAR(30)              DEFAULT NULL COMMENT '총계약금액 (원본)',

    -- 시스템 메타
    source_file                 VARCHAR(200)             DEFAULT NULL COMMENT '업로드 원본 파일명',
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_task_member_contract   (contract_no, change_seq, contract_type),
    INDEX      idx_contract_date         (contract_date),
    INDEX      idx_first_contract_date   (first_contract_date),
    INDEX      idx_contract_type         (contract_type),
    INDEX      idx_vendor_biz_reg_no     (vendor_biz_reg_no),
    INDEX      idx_is_final_contract     (is_final_contract),
    INDEX      idx_demand_agency_code    (demand_agency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='업무별 구성원별 계약내역 RAW 테이블 (기술용역+공사 통합, 조달데이터허브)';
