-- 회원 최종 접속일(로그인 시점) 저장
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP NULL;
