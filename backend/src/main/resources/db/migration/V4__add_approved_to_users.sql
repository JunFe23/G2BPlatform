-- 회원 승인 플로우: 가입 시 미승인(approved=false), 관리자 승인 후 로그인 가능
-- 기존 회원은 approved=true 유지 (DEFAULT 1)
-- Flyway 미사용 시 수동 실행: mysql -u ... -p ... < 이 파일
ALTER TABLE users
  ADD COLUMN approved TINYINT(1) NOT NULL DEFAULT 1 COMMENT '승인여부 1=승인 0=대기';
