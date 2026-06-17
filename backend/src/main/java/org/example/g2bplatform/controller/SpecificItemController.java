package org.example.g2bplatform.controller;

import lombok.RequiredArgsConstructor;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.example.g2bplatform.mapper.SpecificItemMapper;
import org.example.g2bplatform.service.SpecificItemEtlService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SpecificItemController {

    private final SpecificItemEtlService etlService;
    private final SpecificItemMapper specificItemMapper;
    private final org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory;

    /** ETL 실행 — SUPER_ADMIN 전용, 비동기 Job 반환 */
    @PostMapping("/api/admin/etl/specific-item")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CsvUploadJobDto> runEtl(Authentication auth) {
        String triggeredBy = (auth == null) ? null : auth.getName();
        CsvUploadJobDto job = etlService.startEtlJob(triggeredBy);
        return ResponseEntity.ok(job);
    }

    /** 통합 조회 */
    @GetMapping("/api/specific-item")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String demandAgencyName,
            @RequestParam(required = false) String demandAgencyRegion,
            @RequestParam(required = false) String vendorBizRegNo,
            @RequestParam(required = false) String itemCategoryNo,
            @RequestParam(required = false) String detailItemNo,
            @RequestParam(required = false) String isMas,
            @RequestParam(required = false) String isExcellentProduct,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly,
            @RequestParam(required = false, defaultValue = "false") boolean grouped,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int length
    ) {
        Map<String, Object> params = buildParams(
                dataType, demandAgencyName, demandAgencyRegion, vendorBizRegNo,
                itemCategoryNo, detailItemNo, isMas, isExcellentProduct,
                year, month, rangeStart, rangeEnd, showSavedOnly, grouped, start, length);

        List<Map<String, Object>> data = grouped
                ? specificItemMapper.selectGroupedList(params)
                : specificItemMapper.selectFlatList(params);
        int total = grouped
                ? specificItemMapper.selectGroupedCount(params)
                : specificItemMapper.selectFlatCount(params);

        Map<String, Object> res = new HashMap<>();
        res.put("data", data);
        res.put("recordsFiltered", total);
        if (grouped) {
            // 묶어서 보기 상단 합계 (최초/최종 계약금액)
            res.put("totals", specificItemMapper.selectGroupedTotals(params));
        }
        return ResponseEntity.ok(res);
    }

    /** 저장 여부 토글 — grouped 여부에 따라 대상 테이블 분기 */
    @PatchMapping("/api/specific-item/saved")
    public ResponseEntity<Void> toggleSaved(@RequestBody Map<String, Object> body) {
        if (Boolean.TRUE.equals(body.get("grouped"))) {
            specificItemMapper.updateGroupedSaved(body);
        } else {
            specificItemMapper.updateSaved(body);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 엑셀 다운로드 — 대용량 대응(동기).
     * DB 결과를 MyBatis Cursor로 스트리밍하고 SXSSFWorkbook(윈도우 플러시)으로 생성해
     * 전체 List/Workbook 메모리 적재를 피한다. 요청 스레드에서 동기로 생성하므로
     * (StreamingResponseBody 비동기 재디스패치 시 Spring Security AccessDenied 회피) 보안 필터가 정상 동작한다.
     */
    @GetMapping("/api/specific-item/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String demandAgencyName,
            @RequestParam(required = false) String demandAgencyRegion,
            @RequestParam(required = false) String vendorBizRegNo,
            @RequestParam(required = false) String itemCategoryNo,
            @RequestParam(required = false) String detailItemNo,
            @RequestParam(required = false) String isMas,
            @RequestParam(required = false) String isExcellentProduct,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly,
            @RequestParam(required = false, defaultValue = "false") boolean grouped
    ) throws java.io.IOException {
        Map<String, Object> params = buildParams(
                dataType, demandAgencyName, demandAgencyRegion, vendorBizRegNo,
                itemCategoryNo, detailItemNo, isMas, isExcellentProduct,
                year, month, rangeStart, rangeEnd, showSavedOnly, grouped, 0, Integer.MAX_VALUE);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        writeExcel(out, params, grouped);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"specific_item.xlsx\"")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(out.toByteArray());
    }

    private Map<String, Object> buildParams(
            String dataType, String demandAgencyName, String demandAgencyRegion,
            String vendorBizRegNo, String itemCategoryNo, String detailItemNo,
            String isMas, String isExcellentProduct,
            Integer year, String month, String rangeStart, String rangeEnd,
            boolean showSavedOnly, boolean grouped, int start, int length) {

        Map<String, Object> p = new HashMap<>();
        if (ne(dataType))          p.put("dataType", dataType);
        if (ne(demandAgencyName))  p.put("demandAgencyName", demandAgencyName);
        if (ne(demandAgencyRegion))p.put("demandAgencyRegion", demandAgencyRegion);
        if (ne(vendorBizRegNo))    p.put("vendorBizRegNo", vendorBizRegNo);
        if (ne(itemCategoryNo))    p.put("itemCategoryNo", itemCategoryNo);
        if (ne(detailItemNo))      p.put("detailItemNo", detailItemNo);
        if (ne(isMas))             p.put("isMas", isMas);
        if (ne(isExcellentProduct))p.put("isExcellentProduct", isExcellentProduct);
        if (year != null)          p.put("year", year);
        if (ne(month))             p.put("month", month);
        if (ne(rangeStart))        p.put("rangeStart", rangeStart);
        if (ne(rangeEnd))          p.put("rangeEnd", rangeEnd);
        p.put("showSavedOnly", showSavedOnly);
        p.put("start", start);
        p.put("length", length);
        return p;
    }

    private boolean ne(String v) { return v != null && !v.isBlank(); }

    private void writeExcel(java.io.OutputStream out, Map<String, Object> params, boolean grouped) throws java.io.IOException {
        // SXSSF: 메모리에 최근 100행만 유지하고 나머지는 임시파일로 플러시
        org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
        wb.setCompressTempFiles(true);
        try {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("특정품목");
            String[] headers = grouped
                    ? new String[]{"구분","업체명","사업자번호","수요기관명","수요기관지역","계약명","계약방법","계약유형","MAS","물품분류번호","물품분류명","최초계약일자","최종계약일자","최초계약금액","최종계약금액(합계)","계약건수","장기여부","저장"}
                    : new String[]{"구분","업체명","사업자번호","수요기관명","수요기관지역","계약명","계약방법","계약유형","물품분류번호","물품분류명","세부품명번호","세부품명","물품식별명","단위","단가","수량","공급금액","MAS","우수제품","직접구매","중기간경쟁","최초계약일자","계약일자","장기여부","저장"};

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            // 스트리밍 람다는 컨트롤러 트랜잭션 밖(비동기)에서 실행되므로 전용 SqlSession을 직접 연다.
            try (org.apache.ibatis.session.SqlSession session = sqlSessionFactory.openSession();
                 org.apache.ibatis.cursor.Cursor<Map<String, Object>> cursor =
                         grouped ? session.getMapper(SpecificItemMapper.class).selectGroupedListExport(params)
                                 : session.getMapper(SpecificItemMapper.class).selectFlatListExport(params)) {
                for (Map<String, Object> r : cursor) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                    if (grouped) writeGroupedRow(row, r);
                    else writeFlatRow(row, r);
                }
            }
            wb.write(out);
        } catch (Exception e) {
            throw new java.io.IOException("엑셀 생성 실패", e);
        } finally {
            wb.dispose(); // 임시파일 정리
            wb.close();
        }
    }

    private void writeGroupedRow(org.apache.poi.ss.usermodel.Row row, Map<String, Object> r) {
        setCell(row, 0, r.get("dataTypeLabel"));
        setCell(row, 1, r.get("vendorName"));
        setCell(row, 2, r.get("vendorBizRegNo"));
        setCell(row, 3, r.get("demandAgencyName"));
        setCell(row, 4, r.get("demandAgencyRegion"));
        setCell(row, 5, r.get("contractName"));
        setCell(row, 6, r.get("contractMethod"));
        setCell(row, 7, r.get("contractType"));
        setCell(row, 8, r.get("isMas"));
        setCell(row, 9, r.get("itemCategoryNo"));
        setCell(row, 10, r.get("itemCategoryName"));
        setCell(row, 11, r.get("firstContractDate"));
        setCell(row, 12, r.get("lastContractDate"));
        setCell(row, 13, r.get("initialContractAmount"));
        setCell(row, 14, r.get("totalSupplyAmount"));
        setCell(row, 15, r.get("contractCount"));
        setCell(row, 16, r.get("isLongTerm"));
        setCell(row, 17, r.get("saved"));
    }

    private void writeFlatRow(org.apache.poi.ss.usermodel.Row row, Map<String, Object> r) {
        setCell(row, 0, r.get("dataTypeLabel"));
        setCell(row, 1, r.get("vendorName"));
        setCell(row, 2, r.get("vendorBizRegNo"));
        setCell(row, 3, r.get("demandAgencyName"));
        setCell(row, 4, r.get("demandAgencyRegion"));
        setCell(row, 5, r.get("contractName"));
        setCell(row, 6, r.get("contractMethod"));
        setCell(row, 7, r.get("contractType"));
        setCell(row, 8, r.get("itemCategoryNo"));
        setCell(row, 9, r.get("itemCategoryName"));
        setCell(row, 10, r.get("detailItemNo"));
        setCell(row, 11, r.get("detailItemName"));
        setCell(row, 12, r.get("itemIdentifierName"));
        setCell(row, 13, r.get("unit"));
        setCell(row, 14, r.get("unitPrice"));
        setCell(row, 15, r.get("quantity"));
        setCell(row, 16, r.get("supplyAmount"));
        setCell(row, 17, r.get("isMas"));
        setCell(row, 18, r.get("isExcellentProduct"));
        setCell(row, 19, r.get("isDirectPurchase"));
        setCell(row, 20, r.get("isSmeCompetitive"));
        setCell(row, 21, r.get("firstContractDate"));
        setCell(row, 22, r.get("contractDate"));
        setCell(row, 23, r.get("isLongTerm"));
        setCell(row, 24, r.get("saved"));
    }

    private void setCell(org.apache.poi.ss.usermodel.Row row, int col, Object val) {
        // null/빈값은 셀 자체를 만들지 않아 대용량 export 시 셀 생성 비용 절감
        if (val == null) return;
        if (val instanceof Number) {
            row.createCell(col).setCellValue(((Number) val).doubleValue());
        } else {
            String s = val.toString();
            if (!s.isEmpty()) row.createCell(col).setCellValue(s);
        }
    }
}
