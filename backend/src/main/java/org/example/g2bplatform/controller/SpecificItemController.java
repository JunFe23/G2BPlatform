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
        return ResponseEntity.ok(res);
    }

    /** 저장 여부 토글 */
    @PatchMapping("/api/specific-item/saved")
    public ResponseEntity<Void> toggleSaved(@RequestBody Map<String, Object> body) {
        specificItemMapper.updateSaved(body);
        return ResponseEntity.ok().build();
    }

    /** 엑셀 다운로드 */
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
    ) {
        Map<String, Object> params = buildParams(
                dataType, demandAgencyName, demandAgencyRegion, vendorBizRegNo,
                itemCategoryNo, detailItemNo, isMas, isExcellentProduct,
                year, month, rangeStart, rangeEnd, showSavedOnly, grouped, 0, Integer.MAX_VALUE);

        List<Map<String, Object>> rows = grouped
                ? specificItemMapper.selectGroupedList(params)
                : specificItemMapper.selectFlatList(params);

        byte[] xlsx = buildExcel(rows, grouped);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"specific_item.xlsx\"")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(xlsx);
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

    private byte[] buildExcel(List<Map<String, Object>> rows, boolean grouped) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("특정품목");
            String[] headers = grouped
                    ? new String[]{"구분","업체명","사업자번호","수요기관명","수요기관지역","계약유형","물품분류번호","세부품명","최초계약일자","최종계약일자","최종계약금액(합계)","계약건수","장기여부","저장"}
                    : new String[]{"구분","업체명","사업자번호","수요기관명","수요기관지역","계약방법","계약유형","물품분류번호","세부품명번호","세부품명","물품식별명","단위","단가","수량","공급금액","MAS","우수제품","직접구매","중기간경쟁","최초계약일자","계약일자","장기여부","저장"};

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            for (Map<String, Object> r : rows) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                if (grouped) {
                    setCell(row, 0, r.get("dataTypeLabel"));
                    setCell(row, 1, r.get("vendorName"));
                    setCell(row, 2, r.get("vendorBizRegNo"));
                    setCell(row, 3, r.get("demandAgencyName"));
                    setCell(row, 4, r.get("demandAgencyRegion"));
                    setCell(row, 5, r.get("contractType"));
                    setCell(row, 6, r.get("itemCategoryNo"));
                    setCell(row, 7, r.get("detailItemName"));
                    setCell(row, 8, r.get("firstContractDate"));
                    setCell(row, 9, r.get("lastContractDate"));
                    setCell(row, 10, r.get("totalSupplyAmount"));
                    setCell(row, 11, r.get("contractCount"));
                    setCell(row, 12, r.get("isLongTerm"));
                    setCell(row, 13, r.get("saved"));
                } else {
                    setCell(row, 0, r.get("dataTypeLabel"));
                    setCell(row, 1, r.get("vendorName"));
                    setCell(row, 2, r.get("vendorBizRegNo"));
                    setCell(row, 3, r.get("demandAgencyName"));
                    setCell(row, 4, r.get("demandAgencyRegion"));
                    setCell(row, 5, r.get("contractMethod"));
                    setCell(row, 6, r.get("contractType"));
                    setCell(row, 7, r.get("itemCategoryNo"));
                    setCell(row, 8, r.get("detailItemNo"));
                    setCell(row, 9, r.get("detailItemName"));
                    setCell(row, 10, r.get("itemIdentifierName"));
                    setCell(row, 11, r.get("unit"));
                    setCell(row, 12, r.get("unitPrice"));
                    setCell(row, 13, r.get("quantity"));
                    setCell(row, 14, r.get("supplyAmount"));
                    setCell(row, 15, r.get("isMas"));
                    setCell(row, 16, r.get("isExcellentProduct"));
                    setCell(row, 17, r.get("isDirectPurchase"));
                    setCell(row, 18, r.get("isSmeCompetitive"));
                    setCell(row, 19, r.get("firstContractDate"));
                    setCell(row, 20, r.get("contractDate"));
                    setCell(row, 21, r.get("isLongTerm"));
                    setCell(row, 22, r.get("saved"));
                }
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("엑셀 생성 실패", e);
        }
    }

    private void setCell(org.apache.poi.ss.usermodel.Row row, int col, Object val) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        if (val == null) { cell.setCellValue(""); return; }
        if (val instanceof Number) cell.setCellValue(((Number) val).doubleValue());
        else cell.setCellValue(val.toString());
    }
}
