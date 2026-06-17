-- 자사(탑인더스트리/탑정보통신) 우수제품 기준 물품분류번호 설정 테이블
-- 조달청 우수제품 인증(is_excellent_product)과 별개로, 지정된 물품분류번호를
-- '자사 우수제품'으로 동적 판정한다. 목록 변경 시 재적재 없이 즉시 반영된다.
CREATE TABLE IF NOT EXISTS top_excellent_category (
    item_category_no    VARCHAR(10)  NOT NULL PRIMARY KEY COMMENT '물품분류번호',
    item_category_name  VARCHAR(100)          DEFAULT NULL COMMENT '물품분류명',
    is_active           CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용여부 Y/N',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='자사 우수제품 기준 물품분류번호';

INSERT INTO top_excellent_category (item_category_no, item_category_name) VALUES
    ('39121101', '분전반'),
    ('39121103', '배전반'),
    ('39121104', '전동기제어반'),
    ('39121189', '계장제어장치'),
    ('39121801', '빌딩자동제어장치'),
    ('41112498', '프로세스제어반')
ON DUPLICATE KEY UPDATE item_category_name = VALUES(item_category_name);
