# 보고서 물품 저장 기능 – 테이블 구조 변경 계획

## 1. 대상 테이블

| 항목 | 내용 |
|------|------|
| **테이블명** | `procurement_contract_summary` |
| **역할** | 조달 계약 집계/대표 단위. 보고서 물품 목록·대시보드·저장 여부(saved) 저장에 사용 |

---

## 2. 현재 문제

- **저장 갱신 조건**: `WHERE bid_notice_no = ? AND vendor_biz_reg_no = ?` 만 사용
- **결과**: 동일 (공고번호, 업체 사업자번호) 조합이 여러 행 있으면(예: 계약차수별) **한 번 체크 시 여러 행이 동시에 저장/해제됨**
- **식별자 부재**: 행 단위 PK가 없어 “한 행만” 갱신할 수 없음

---

## 3. 변경 방향

- 테이블에 **행 단위 PK `id`** 를 두고, **저장/해제 API는 `id` 한 개만 받아서 1건만 UPDATE** 하도록 변경

---

## 4. 테이블 구조 변경 (DB)

### 4.1 전제

- `procurement_contract_summary` 에 **이미 `id` 컬럼이 있는지** 먼저 확인 필요  
  - 있으면: PK/유니크 여부 확인 후, 5. 코드 변경만 진행  
  - 없으면: 아래 마이그레이션 적용

### 4.2 `id` 가 없을 때 마이그레이션 순서 (예: V6)

1. **컬럼 추가 (nullable)**  
   - `id BIGINT NOT NULL AUTO_INCREMENT` 추가  
   - 아직 PK 부여 안 함 (기존 데이터 채우기 위해)

2. **기존 행에 id 채우기**  
   - `UPDATE procurement_contract_summary SET id = @rownum := @rownum + 1 ORDER BY first_contract_date, bid_notice_no, vendor_biz_reg_no;`  
   - 또는 DB 엔진에 맞는 ROW_NUMBER() / 시퀀스 방식으로 1, 2, 3, … 부여

3. **PK 설정**  
   - `id` 를 NOT NULL + PRIMARY KEY 로 변경  
   - 이미 값이 채워졌으므로 `ALTER TABLE ... ADD PRIMARY KEY (id);` (필요 시 기존 인덱스 정리 후 실행)

4. **(선택)**  
   - AUTO_INCREMENT 시작값을 “현재 MAX(id)+1” 로 설정해, 이후 INSERT 시 충돌 방지

### 4.3 MySQL 예시 (id 없을 때)

```sql
-- Step 1
ALTER TABLE procurement_contract_summary
  ADD COLUMN id BIGINT NOT NULL DEFAULT 0;

-- Step 2: 기존 행에 순번 부여 (MySQL 5.x 호환 예시)
SET @r := 0;
UPDATE procurement_contract_summary
SET id = (@r := @r + 1)
ORDER BY first_contract_date, bid_notice_no, vendor_biz_reg_no;

-- Step 3
ALTER TABLE procurement_contract_summary
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (id);
```

- Flyway 사용 시: 위 내용을 `V6__add_id_to_procurement_contract_summary.sql` 등으로 저장 후 실행

---

## 5. 코드 변경 계획 (구체 수정 없이 항목만)

### 5.1 백엔드 – Mapper (ProcurementContractSummaryMapper)

| 파일 | 변경 내용 |
|------|-----------|
| **ProcurementContractSummaryMapper.xml** | • `selectReportGoodsList` SELECT 절에 `id AS id` 추가<br>• `selectReportGoodsListForExport` 동일<br>• `selectReportGoodsListKeyset` 동일 (keyset 조건은 기존 컬럼 유지 가능)<br>• `updateSaved` 를 `WHERE id = #{id}` 형태로 변경 (bid_notice_no, vendor_biz_reg_no 제거) |
| **ProcurementContractSummaryMapper.java** | • `updateSaved(Long id, String saved)` 시그니처로 변경 (또는 int id) |

### 5.2 백엔드 – Service

| 파일 | 변경 내용 |
|------|-----------|
| **ReportDataService.java** | • `updateReportGoodsSaved(String bidNoticeNo, String vendorBizRegNo, String saved)` 제거<br>• `updateReportGoodsSaved(Long id, String saved)` 추가하고, mapper.updateSaved(id, saved) 호출 |

### 5.3 백엔드 – Controller

| 파일 | 변경 내용 |
|------|-----------|
| **ReportDataController.java** | • `updateReportGoodsSaved` body 에서 `id` (Long) 받기<br>• `bidNoticeNo`, `vendorBizRegNo` 검증 제거<br>• `id == null` 검증 추가<br>• service.updateReportGoodsSaved(id, saved) 호출 |

### 5.4 프론트엔드

| 파일 | 변경 내용 |
|------|-----------|
| **ReportGoodsView.vue** | • 목록 항목에 `id` 가 포함되므로 그대로 사용<br>• `toggleSave(item)` 시 PATCH body 를 `{ id: item.id, saved: nextSaved }` 로 전송 (bidNoticeNo, vendorBizRegNo 제거)<br>• `rowKey(item)` 은 `item.id` 또는 `id` 없을 때 fallback 으로 기존 조합 사용 가능 |

---

## 6. 작업 순서 권장

1. DB에서 `procurement_contract_summary` 에 `id` 존재 여부 확인  
2. 없으면: 마이그레이션 작성·실행 (V6 등) 후 테이블에 `id` 채워졌는지 확인  
3. Mapper XML/Java 수정 (SELECT 에 id 추가, updateSaved 를 id 기준으로 변경)  
4. Service 시그니처·내부 구현 변경  
5. Controller 요청 파라미터·검증·호출 변경  
6. 프론트 PATCH 요청을 `id` + `saved` 만 보내도록 수정  

---

## 7. 롤백 시

- 코드만 원복하면 기존처럼 `bid_notice_no` + `vendor_biz_reg_no` 로 동작 (테이블에 id 가 있어도 기존 쿼리는 그대로 사용 가능).
- 테이블에서 `id` 제거하려면: PK 해제 → 컬럼 삭제 별도 마이그레이션 필요 (데이터 백업 권장).
