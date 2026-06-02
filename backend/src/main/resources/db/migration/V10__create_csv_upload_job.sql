-- CSV 업로드/적재 진행률 추적 테이블
-- 업로드는 동기(파일 전송)로 받고, 서버 적재는 비동기 job으로 처리.
CREATE TABLE IF NOT EXISTS csv_upload_job (
    id              CHAR(36)     NOT NULL PRIMARY KEY COMMENT 'jobId (UUID)',
    upload_type     VARCHAR(50)  NOT NULL COMMENT '업로드 유형 (specific_item 등)',

    file_name       VARCHAR(255) NOT NULL COMMENT '업로드한 파일명',
    file_size_bytes BIGINT       DEFAULT NULL COMMENT '파일 크기(bytes)',
    temp_path       VARCHAR(500) DEFAULT NULL COMMENT '서버 임시 저장 경로',

    status          VARCHAR(20)  NOT NULL DEFAULT 'QUEUED' COMMENT 'QUEUED/RUNNING/SUCCESS/FAILED/CANCELLED',
    message         VARCHAR(500) DEFAULT NULL COMMENT '진행 메시지',

    total_rows      INT          DEFAULT NULL COMMENT '총 데이터 행 수(사전 카운트)',
    processed_rows  INT          NOT NULL DEFAULT 0 COMMENT '처리한 데이터 행 수(파싱 기준)',
    inserted_count  INT          NOT NULL DEFAULT 0 COMMENT '신규 적재 행 수',
    skipped_count   INT          NOT NULL DEFAULT 0 COMMENT '중복 스킵 행 수',

    uploader        VARCHAR(100) DEFAULT NULL COMMENT '업로더 식별자',
    error_message   VARCHAR(1000) DEFAULT NULL COMMENT '실패 시 오류 메시지',

    started_at      DATETIME     DEFAULT NULL,
    finished_at     DATETIME     DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_type_status_created (upload_type, status, created_at),
    INDEX idx_uploader_created (uploader, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='CSV 적재 Job 진행률';

