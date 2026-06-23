-- 시장데이터(공사/용역) 통합 테이블
-- 소스: task_member_contract_raw (공사+용역, contract_type 구분)
-- 대상 분류(탑인더스트리/탑정보통신)만 flat/grouped에 적재. 조회 시 contract_type으로 공사/용역 구분.

-- 1) 대상 분류 설정 테이블 (추후 API 코드 컬럼 예약)
CREATE TABLE IF NOT EXISTS market_target_category (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contract_type       VARCHAR(20)  NOT NULL COMMENT 'construction | service',
    category_major      VARCHAR(50)           DEFAULT NULL COMMENT '공공조달대분류 명칭',
    category_mid        VARCHAR(50)           DEFAULT NULL COMMENT '공공조달중분류 명칭',
    category_name       VARCHAR(100) NOT NULL COMMENT '공공조달분류 명칭 (매칭 키)',
    category_major_code VARCHAR(20)           DEFAULT NULL COMMENT '추후 API 대분류 코드',
    category_mid_code   VARCHAR(20)           DEFAULT NULL COMMENT '추후 API 중분류 코드',
    category_name_code  VARCHAR(20)           DEFAULT NULL COMMENT '추후 API 분류 코드',
    is_active           CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_market_target_category (contract_type, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='시장데이터 대상 공공조달분류 (공사/용역)';

INSERT INTO market_target_category (contract_type, category_major, category_mid, category_name) VALUES
    ('service','기술용역','설계','토목설계용역'),
    ('service','기술용역','설계','건축설계용역'),
    ('service','기술용역','설계','상하수도설계용역'),
    ('service','기술용역','설계','전기설계용역'),
    ('service','기술용역','설계','교통설계용역'),
    ('service','기술용역','설계','정보통신설계용역'),
    ('service','기술용역','감리','건축감리용역'),
    ('service','기술용역','감리','토목감리용역'),
    ('service','기술용역','감리','전기감리용역'),
    ('service','기술용역','감리','정보통신감리용역'),
    ('service','기술용역','CM','건축CM용역'),
    ('service','기술용역','CM','토목CM용역'),
    ('service','기술용역','기타','기타기술용역'),
    ('service','ICT 서비스','운영 및 유지관리','정보시스템유지관리서비스'),
    ('service','ICT 서비스','SW 및 시스템 개발','패키지소프트웨어개발및도입서비스'),
    ('construction','시설공사','개별법령','전기공사'),
    ('construction','시설공사','개별법령','정보통신공사'),
    ('construction','시설공사','개별법령','기타시설공사'),
    ('construction','시설공사','시설물유지관리공사','상하수도설비공사'),
    ('construction','시설공사','시설물유지관리공사','시설물유지관리공사'),
    ('construction','시설공사','시설물유지관리공사','기계설비공사');

-- 2) flat (계약별 최종 1행, 공사+용역 통합)
CREATE TABLE IF NOT EXISTS market_contract_flat (
    id                       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contract_type            VARCHAR(20)  NOT NULL COMMENT 'construction | service',
    contract_no              VARCHAR(30)  NOT NULL,
    change_seq               VARCHAR(5)   NOT NULL DEFAULT '' COMMENT '최종(최신) 변경차수',
    is_final_contract        CHAR(1)               DEFAULT NULL,
    first_year_contract_no   VARCHAR(30)           DEFAULT NULL COMMENT '초년도계약번호',
    is_long_term             CHAR(1)      NOT NULL DEFAULT 'N',
    vendor_name              VARCHAR(200)          DEFAULT NULL,
    vendor_biz_reg_no        VARCHAR(15)           DEFAULT NULL,
    contract_name            VARCHAR(500)          DEFAULT NULL,
    demand_agency_name       VARCHAR(200)          DEFAULT NULL,
    demand_agency_region     VARCHAR(100)          DEFAULT NULL,
    public_procurement_major VARCHAR(50)           DEFAULT NULL COMMENT '공공조달대분류(조달업무영역 대체)',
    public_procurement_mid   VARCHAR(50)           DEFAULT NULL COMMENT '공공조달중분류',
    public_procurement_name  VARCHAR(100)          DEFAULT NULL COMMENT '공공조달분류 명칭',
    contract_method          VARCHAR(20)           DEFAULT NULL COMMENT '계약방법',
    first_contract_date      DATE                  DEFAULT NULL COMMENT '최초계약일자',
    contract_date            DATE                  DEFAULT NULL COMMENT '계약일자(최종)',
    first_contract_amount    BIGINT                DEFAULT NULL COMMENT '최초계약금액',
    total_contract_amount    BIGINT                DEFAULT NULL COMMENT '총(최종)계약금액',
    current_contract_amount  BIGINT                DEFAULT NULL COMMENT '금차계약금액',
    saved                    CHAR(1)      NOT NULL DEFAULT 'N',
    is_active                CHAR(1)      NOT NULL DEFAULT 'Y',
    etl_loaded_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_market_flat (contract_type, contract_no),
    INDEX idx_mcf_type (contract_type),
    INDEX idx_mcf_order (contract_type, first_contract_date DESC, contract_no),
    INDEX idx_mcf_agency (demand_agency_name(50)),
    INDEX idx_mcf_ppname (public_procurement_name),
    INDEX idx_mcf_saved (saved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='시장데이터 공사/용역 flat (대상분류 필터, 계약별 최종 1행)';

-- 3) grouped (초년도계약번호 묶음)
CREATE TABLE IF NOT EXISTS market_contract_grouped (
    id                        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contract_type             VARCHAR(20)  NOT NULL,
    group_key                 VARCHAR(30)  NOT NULL COMMENT 'COALESCE(초년도계약번호, 계약번호)',
    contract_no               VARCHAR(30)           DEFAULT NULL,
    first_year_contract_no    VARCHAR(30)           DEFAULT NULL,
    vendor_name               VARCHAR(200)          DEFAULT NULL,
    vendor_biz_reg_no         VARCHAR(15)           DEFAULT NULL,
    contract_name             VARCHAR(500)          DEFAULT NULL,
    demand_agency_name        VARCHAR(200)          DEFAULT NULL,
    demand_agency_region      VARCHAR(100)          DEFAULT NULL,
    public_procurement_major  VARCHAR(50)           DEFAULT NULL,
    public_procurement_mid    VARCHAR(50)           DEFAULT NULL,
    public_procurement_name   VARCHAR(100)          DEFAULT NULL,
    contract_method           VARCHAR(20)           DEFAULT NULL,
    is_long_term              CHAR(1)      NOT NULL DEFAULT 'N',
    first_contract_date       DATE                  DEFAULT NULL COMMENT '최초계약일자(MIN)',
    last_contract_date        DATE                  DEFAULT NULL COMMENT '최종계약일자(MAX)',
    initial_contract_amount   BIGINT       NOT NULL DEFAULT 0 COMMENT '최초계약금액(가장 이른 first_contract_date 연차)',
    total_contract_amount_sum BIGINT       NOT NULL DEFAULT 0 COMMENT '최종계약금액 합계(SUM total)',
    contract_count            INT          NOT NULL DEFAULT 1,
    saved                     CHAR(1)      NOT NULL DEFAULT 'N',
    etl_loaded_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_market_grouped (contract_type, group_key, vendor_biz_reg_no),
    INDEX idx_mcg_type (contract_type),
    INDEX idx_mcg_last (contract_type, last_contract_date DESC),
    INDEX idx_mcg_agency (demand_agency_name(50)),
    INDEX idx_mcg_ppname (public_procurement_name),
    INDEX idx_mcg_saved (saved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='시장데이터 공사/용역 grouped (초년도계약번호 묶음)';

-- 4) 기존 공사/용역 보고서 테이블 제거 (TOP 보존 안 함, 새 구조로 전환)
DROP TABLE IF EXISTS construction_contract_flat;
DROP TABLE IF EXISTS construction_contract_grouped;
DROP TABLE IF EXISTS service_contract_flat;
DROP TABLE IF EXISTS service_contract_grouped;
