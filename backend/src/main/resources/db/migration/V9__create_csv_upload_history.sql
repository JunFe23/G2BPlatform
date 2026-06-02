-- CSV 업로드 내역 기록 테이블
-- Raw CSV 업로드의 감사(audit) 및 운영 추적용.
CREATE TABLE IF NOT EXISTS csv_upload_history (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    upload_type     VARCHAR(50)  NOT NULL COMMENT '업로드 유형 (예: specific_item)',
    file_name       VARCHAR(255) NOT NULL COMMENT '클라이언트에서 업로드한 파일명',
    file_size_bytes BIGINT       DEFAULT NULL COMMENT '업로드 파일 크기 (bytes)',

    total_rows      INT          DEFAULT NULL COMMENT '파싱된 데이터 행 수',
    inserted_count  INT          DEFAULT NULL COMMENT '신규 적재된 행 수',
    skipped_count   INT          DEFAULT NULL COMMENT '중복 등으로 스킵된 행 수',
    elapsed_ms      BIGINT       DEFAULT NULL COMMENT '처리 소요 시간(ms)',

    uploader        VARCHAR(100) DEFAULT NULL COMMENT '업로더 식별자 (JWT subject 등)',
    status          VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    error_message   VARCHAR(1000) DEFAULT NULL COMMENT '실패 시 오류 메시지',

    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_upload_type_created_at (upload_type, created_at),
    INDEX idx_uploader_created_at (uploader, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='CSV 업로드 내역 (audit log)';

