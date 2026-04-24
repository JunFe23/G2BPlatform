-- 우수제품 인증 정보 테이블
-- 탑인더스트리/탑정보통신이 보유한 우수제품과 경쟁사 현황을 관리
CREATE TABLE IF NOT EXISTS excellent_product_cert (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_code        VARCHAR(50)  NOT NULL                 COMMENT '제품 고유 코드 (동일 제품 식별 키)',
    product_name        VARCHAR(200) NOT NULL                 COMMENT '품명',
    item_category_name  VARCHAR(200) DEFAULT NULL             COMMENT '물품분류명',
    vendor_name         VARCHAR(200) NOT NULL                 COMMENT '업체명',
    vendor_biz_reg_no   VARCHAR(20)  NOT NULL                 COMMENT '사업자등록번호',
    vendor_region       VARCHAR(100) DEFAULT NULL             COMMENT '업체 소재 지역 (시/도)',
    acquisition_date    DATE         DEFAULT NULL             COMMENT '취득일자',
    expiry_date         DATE         DEFAULT NULL             COMMENT '유효기간 만료일',
    is_active           CHAR(1)      NOT NULL DEFAULT 'Y'     COMMENT '활성 여부 (Y/N)',
    created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_epc_product_code      (product_code),
    INDEX idx_epc_vendor_biz_reg_no (vendor_biz_reg_no),
    INDEX idx_epc_expiry_date       (expiry_date),
    INDEX idx_epc_is_active         (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='우수제품 인증 정보 (탑그룹 + 경쟁사)';
