# 로컬 특정품목 CSV 업로드 E2E 검증 런북

**티켓:** G2B-8  
**검증일:** 2026-06-09  
**검증자:** super_admin  
**결과:** 전체 PASS

---

## 1. 사전 조건

| 항목 | 확인 방법 | 기댓값 |
|------|-----------|--------|
| 로컬 MySQL | `mysql -u root g2b -e "SELECT 1;"` | 정상 응답 |
| Flyway V8~V10 | `SELECT version, success FROM flyway_schema_history;` | 전부 success=1 |
| 테이블 존재 | `SHOW TABLES LIKE 'procurement_specific_item_raw';` 등 | 3개 테이블 확인 |
| Docker Compose | `docker ps` | `g2bplatform-api-1` (8080), `g2bplatform-web-1` (3000) UP |

### Flyway 마이그레이션 확인 쿼리

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

**실행 결과 (2026-06-09):**

| version | description | success |
|---------|-------------|---------|
| 7 | baseline_before_specific_item_raw | 1 |
| 8 | create specific item raw | 1 |
| 9 | create csv upload history | 1 |
| 10 | create csv upload job | 1 |

---

## 2. 앱 기동

로컬 Docker Compose 환경은 `host.docker.internal:3306`(호스트 MySQL)에 연결된다.

```bash
# 프로젝트 루트에서
docker compose up -d
```

- 백엔드: `http://localhost:8080`
- 프론트: `http://localhost:3000`

> **주의:** `docker-compose.yml`은 `application.properties`를 마운트한다.  
> `spring.datasource.url`이 `host.docker.internal:3306`이면 로컬 MySQL 연결.  
> `rds.amazonaws.com`이면 프로덕션 DB — 반드시 확인 후 기동.

---

## 3. SUPER_ADMIN 로그인

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"super_admin","password":"<비밀번호>"}'
```

응답에서 `accessToken` 추출 후 이후 요청에 사용.

---

## 4. CSV 업로드

**대상 파일:** `docs/sample_csv/2026_특정품목 조달 내역.csv`  
**파일 크기:** 약 15 MB  
**인코딩:** UTF-16 (서비스 내부에서 UTF-8 변환 처리)

```bash
TOKEN="<accessToken>"

curl -s -X POST http://localhost:8080/api/admin/upload/specific-item \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@docs/sample_csv/2026_특정품목 조달 내역.csv"
```

응답 예시:
```json
{
  "jobId": "4a2f3958-7c1b-426f-960d-3a661ba8c493",
  "status": "QUEUED",
  "totalRows": null,
  "processedRows": 0
}
```

---

## 5. Job 폴링

```bash
JOB_ID="<jobId>"

curl -s "http://localhost:8080/api/admin/upload/jobs/$JOB_ID" \
  -H "Authorization: Bearer $TOKEN"
```

`status`가 `SUCCESS` 또는 `FAILED`가 될 때까지 반복 확인 (약 10초 간격).

---

## 6. 검증 쿼리

```sql
-- raw 테이블 적재 건수
SELECT COUNT(*) AS total FROM procurement_specific_item_raw;

-- 소스 파일별 건수
SELECT source_file, COUNT(*) AS cnt
FROM procurement_specific_item_raw
GROUP BY source_file;

-- Job 결과 확인
SELECT id, status, inserted_count, skipped_count, total_rows, started_at, finished_at
FROM csv_upload_job
ORDER BY started_at DESC
LIMIT 5;
```

---

## 7. 검증 결과 (2026-06-09)

### 샘플 파일 업로드 (2026, curl)

| 항목 | 값 |
|------|----|
| jobId | 4a2f3958-7c1b-426f-960d-3a661ba8c493 |
| status | SUCCESS |
| totalRows | 16,592 |
| insertedCount | 16,592 |
| skippedCount | 0 |
| 소요 시간 | 약 25초 (15:50:36 → 15:51:01) |

### 재업로드 (중복 방지 확인)

| 항목 | 값 |
|------|----|
| jobId | 403a9df4-1d2d-4e02-8dab-c055bcb7a829 |
| status | SUCCESS |
| totalRows | 16,592 |
| insertedCount | 0 |
| skippedCount | 16,592 |
| 소요 시간 | 약 25초 |

`INSERT IGNORE` 동작 정상 확인 — 중복 행 전량 skip.

### 전체 연도 적재 (2017~2026, UI 업로드)

| 연도 | 적재 건수 |
|------|----------|
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

- Job 14건 전부 SUCCESS
- `procurement_specific_item_raw` 총 1,023,477행 확인

---

## 8. 블로커 및 수정 이력

| 항목 | 내용 | 조치 |
|------|------|------|
| `application-local.properties` 오타 | 파일 끝 `ㅊ` 문자 제거 | 삭제 |
| 로컬 직접 기동 시 DB 연결 실패 | `./gradlew bootRun` 시 `host.docker.internal` 미해석 | `application-local.properties`에 `spring.datasource.url=jdbc:mysql://127.0.0.1:3306/g2b...` 추가 (Docker 외부 기동 시 사용) |
| Docker Compose 사용 시 정상 | `host.docker.internal`은 컨테이너 내부에서 호스트 MySQL로 정상 해석됨 | Docker Compose로 기동 확정 |
