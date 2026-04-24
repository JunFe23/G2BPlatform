-- ============================================================
-- V7: excellent_product_cert 초기 데이터 적재
--
-- 기존 테이블에서 가져올 수 있는 것:
--   ✅ 업체명 / 사업자번호  (vendor_name, vendor_biz_reg_no)
--   ✅ 품목식별번호         (item_identifier_no)  → product_code로 사용
--   ✅ 품명 / 분류          (item_identifier_name, item_category_name)
--   ❌ 취득일자 / 만료일    → 해당 컬럼이 없어서 NULL 입력
--   ❌ 업체 소재 지역       → vendor_region이 없어서 NULL 입력
--
-- 데이터 소스:
--   1) shopping_mall_flat       (쇼핑몰 배송 계약, is_excellent_product = 'Y')
--   2) procurement_contract_flat (물품 계약,        is_excellent_product = 'Y')
--
-- 중복 방지: UNIQUE KEY (product_code, vendor_biz_reg_no) + INSERT IGNORE
-- ============================================================

-- 1) 중복 방지용 유니크 키 추가
ALTER TABLE excellent_product_cert
    ADD UNIQUE KEY uq_epc_product_vendor (product_code, vendor_biz_reg_no);

-- 2) shopping_mall_flat 에서 우수제품 업체+품목 추출하여 적재
INSERT IGNORE INTO excellent_product_cert
    (product_code, product_name, item_category_name, vendor_name, vendor_biz_reg_no,
     vendor_region, acquisition_date, expiry_date)
SELECT
    item_identifier_no                                  AS product_code,
    COALESCE(item_identifier_name, detail_item_name)    AS product_name,
    item_category_name,
    vendor_name,
    vendor_biz_reg_no,
    NULL                                                AS vendor_region,
    NULL                                                AS acquisition_date,
    NULL                                                AS expiry_date
FROM shopping_mall_flat
WHERE is_excellent_product = 'Y'
  AND item_identifier_no IS NOT NULL
  AND item_identifier_no != ''
  AND vendor_biz_reg_no   IS NOT NULL
  AND vendor_biz_reg_no   != ''
GROUP BY item_identifier_no, vendor_biz_reg_no;

-- 3) procurement_contract_flat 에서도 추출 (쇼핑몰에 없는 물품 계약 우수제품 포함)
INSERT IGNORE INTO excellent_product_cert
    (product_code, product_name, item_category_name, vendor_name, vendor_biz_reg_no,
     vendor_region, acquisition_date, expiry_date)
SELECT
    item_identifier_no                                  AS product_code,
    COALESCE(item_identifier_name, detail_item_name)    AS product_name,
    item_category_name,
    vendor_name,
    vendor_biz_reg_no,
    NULL                                                AS vendor_region,
    NULL                                                AS acquisition_date,
    NULL                                                AS expiry_date
FROM procurement_contract_flat
WHERE is_excellent_product = 'Y'
  AND item_identifier_no IS NOT NULL
  AND item_identifier_no != ''
  AND vendor_biz_reg_no   IS NOT NULL
  AND vendor_biz_reg_no   != ''
GROUP BY item_identifier_no, vendor_biz_reg_no;

-- ============================================================
-- 적재 후 확인 쿼리 (참고용):
--
-- SELECT COUNT(*) total,
--        SUM(CASE WHEN vendor_biz_reg_no IN ('1188117437','1188119624') THEN 1 ELSE 0 END) own_count,
--        SUM(CASE WHEN vendor_biz_reg_no NOT IN ('1188117437','1188119624') THEN 1 ELSE 0 END) competitor_count
-- FROM excellent_product_cert;
--
-- -- 탑그룹 보유 품목 확인
-- SELECT product_code, product_name, vendor_name
-- FROM excellent_product_cert
-- WHERE vendor_biz_reg_no IN ('1188117437','1188119624')
-- ORDER BY product_code;
--
-- -- 동일 품목코드를 가진 경쟁사 확인
-- SELECT e.product_code, e.product_name, e.vendor_name, e.vendor_biz_reg_no
-- FROM excellent_product_cert e
-- WHERE e.vendor_biz_reg_no NOT IN ('1188117437','1188119624')
--   AND e.product_code IN (
--       SELECT product_code FROM excellent_product_cert
--       WHERE vendor_biz_reg_no IN ('1188117437','1188119624')
--   );
-- ============================================================
