# 서버 특정품목 CSV 업로드 E2E 검증 런북

**티켓:** G2B-10  
**검증일:** 2026-06-09  
**검증자:** super_admin  
**결과:** 전체 PASS

---

## 1. 사전 조건

| 항목 | 확인 방법 | 기댓값 |
|------|-----------|--------|
| 서버 컨테이너 기동 | `docker ps` | `g2b-api-1`, `g2b-web-1` UP |
| RDS Flyway 상태 | `SELECT version, success FROM flyway_schema_history;` | V7~V10 전부 success=1 |
| 호스트 nginx | `sudo nginx -T \| grep client_max_body` | 150M |
| Docker web nginx | `docker exec g2b-web-1 nginx -T \| grep client_max_body` | 150m |
| Spring multipart | `application.properties` | max-file-size=150MB |

---

## 2. 접속 및 업로드

1. `https://g2btop.duckdns.org` 접속
2. SUPER_ADMIN 로그인
3. `/raw-data-import` → 특정품목 탭 선택
4. CSV 파일 업로드 (연도별 순차 업로드)

---

## 3. 트러블슈팅 이력 (2026-06-09)

### 413 Request Entity Too Large

**원인:** 대용량 CSV 업로드 시 두 레이어에서 파일 크기 제한에 걸림

| 레이어 | 문제 | 조치 |
|--------|------|------|
| 호스트 nginx | `client_max_body_size` 미설정 (기본 1MB) | `deploy/g2b.conf`에 `client_max_body_size 150M` 추가 |
| Spring Boot | 서버 `application.properties`에 multipart 설정 누락 (기본 1MB) | `spring.servlet.multipart.max-file-size=150MB` 추가 |
| Docker web nginx | 이미 `client_max_body_size 150m` 설정됨 | 조치 불필요 |

**진단 방법:** 브라우저 개발자 도구 → Network → 실패한 요청 → 응답 헤더 확인
- `Vary: Origin`, `Cache-Control: no-cache, no-store` 포함 → Spring에서 반환된 413
- 단순 nginx 에러페이지 → nginx에서 차단된 413

---

## 4. 검증 쿼리

```sql
-- 연도별 적재 건수
SELECT source_file, COUNT(*) AS cnt
FROM procurement_specific_item_raw
GROUP BY source_file
ORDER BY source_file;

-- 전체 건수
SELECT COUNT(*) AS total FROM procurement_specific_item_raw;

-- Job 결과
SELECT status, COUNT(*) AS cnt FROM csv_upload_job GROUP BY status;
```

---

## 5. 검증 결과 (2026-06-09)

### 연도별 적재 결과

| 연도 | 건수 |
|------|------|
| 2017 | 77,544 |
| 2018 | 83,920 |
| 2019 | 95,841 |
| 2020 | 106,678 |
| 2021 | 121,620 |
| 2022 | 121,576 |
| 2023 | 139,782 |
| 2024 | 141,026 |
| 2025 | 118,898 |
| 2026 | 16,592 |
| **합계** | **1,023,477** |

- 로컬 적재 결과(1,023,477)와 **완전 일치** ✅
- Job 10건 전부 SUCCESS ✅
- 로컬과 서버 데이터 정합성 확인 완료 ✅
