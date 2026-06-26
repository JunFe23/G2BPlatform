package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.g2bplatform.DTO.CsvUploadJobDto;
import org.example.g2bplatform.service.MarketContractEtlService;
import org.example.g2bplatform.service.ReportMarketService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class MarketContractController {

    private final MarketContractEtlService etlService;
    private final ReportMarketService reportMarketService;

    /** 시장데이터(공사/용역) ETL 실행 — SUPER_ADMIN 전용 */
    @PostMapping("/api/admin/etl/market-contract")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CsvUploadJobDto> runEtl(Authentication auth) {
        String triggeredBy = (auth == null) ? null : auth.getName();
        return ResponseEntity.ok(etlService.startEtlJob(triggeredBy));
    }

    @Operation(summary = "시장데이터 공사/용역 계약 목록", description = "market_contract_flat/grouped 통합 조회 API")
    @GetMapping("/api/report/market-contracts")
    public ResponseEntity<Map<String, Object>> getMarketContracts(
            @Parameter(description = "계약 유형: construction | service") @RequestParam String contractType,
            @Parameter(description = "합쳐서 보기 여부 (true=grouped, false=flat)") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "시작 행") @RequestParam(defaultValue = "0") int start,
            @Parameter(description = "조회 건수") @RequestParam(defaultValue = "100") int length,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "공공조달분류명") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "계약명") @RequestParam(required = false) String contractName,
            @Parameter(description = "계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "조달업무영역/대분류") @RequestParam(required = false) String procurementWorkArea,
            @Parameter(description = "공공조달분류 중분류") @RequestParam(required = false) String publicProcurementCategoryMid,
            @Parameter(description = "공공조달분류명") @RequestParam(required = false) String publicProcurementCategory,
            @Parameter(description = "최초계약일자(YYYY-MM-DD)") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월(YYYY-MM)") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작(YYYY-MM)") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료(YYYY-MM)") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) {
        try {
            String detailFilter = firstNonBlank(prdctClsfcNo, publicProcurementCategory);
            List<Map<String, Object>> list = reportMarketService.getList(
                    contractType, grouped, start, length,
                    dminsttNm, dminsttNmDetail, detailFilter, contractName, cntctCnclsMthdNm,
                    procurementWorkArea, publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            int filtered = reportMarketService.getCount(
                    contractType, grouped,
                    dminsttNm, dminsttNmDetail, detailFilter, contractName, cntctCnclsMthdNm,
                    procurementWorkArea, publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("data", list);
            body.put("recordsFiltered", filtered);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @Operation(summary = "시장데이터 검색 필터 옵션 (입찰계약방법/조달업무영역 distinct)")
    @GetMapping("/api/report/market-contracts/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions(
            @Parameter(description = "계약 유형: construction | service") @RequestParam String contractType
    ) {
        try {
            Map<String, Object> body = new HashMap<>(reportMarketService.getFilterOptions(contractType));
            body.put("success", true);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @Operation(summary = "시장데이터 공사/용역 계약 엑셀 다운로드")
    @GetMapping("/api/report/market-contracts/excel")
    public ResponseEntity<Resource> getMarketContractsExcel(
            @Parameter(description = "계약 유형: construction | service") @RequestParam String contractType,
            @Parameter(description = "합쳐서 보기 여부") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "공공조달분류명") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "계약명") @RequestParam(required = false) String contractName,
            @Parameter(description = "계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "조달업무영역/대분류") @RequestParam(required = false) String procurementWorkArea,
            @Parameter(description = "공공조달분류 중분류") @RequestParam(required = false) String publicProcurementCategoryMid,
            @Parameter(description = "공공조달분류명") @RequestParam(required = false) String publicProcurementCategory,
            @Parameter(description = "최초계약일자") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) throws IOException {
        final String normalizedType;
        try {
            normalizedType = reportMarketService.normalizeContractType(contractType);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }

        final String[] headerNames = excelHeaders(grouped);
        final String[] keys = excelKeys(grouped);
        final Set<String> amountKeys = new HashSet<>(Arrays.asList(
                "firstContractAmount", "finalContractAmount", "contractAmount", "finalContractAmountSum"));

        Path tempFile = Files.createTempFile("report_market_contracts_", ".xlsx");
        try {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("시장데이터계약");
                DataFormat dataFormat = workbook.createDataFormat();
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(dataFormat.getFormat("#,##0"));

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    headerRow.createCell(i).setCellValue(headerNames[i]);
                }

                final int[] rowNumRef = {1};
                String detailFilter = firstNonBlank(prdctClsfcNo, publicProcurementCategory);
                reportMarketService.streamForExcel(
                        normalizedType, grouped,
                        dminsttNm, dminsttNmDetail, detailFilter, contractName, cntctCnclsMthdNm,
                        procurementWorkArea, publicProcurementCategoryMid, publicProcurementCategory,
                        firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                        resultContext -> {
                            Map<String, Object> row = resultContext.getResultObject();
                            Row excelRow = sheet.createRow(rowNumRef[0]++);
                            for (int colNum = 0; colNum < keys.length; colNum++) {
                                Object value = row != null ? row.getOrDefault(keys[colNum], "") : "";
                                Cell cell = excelRow.createCell(colNum);
                                if (amountKeys.contains(keys[colNum]) && value != null && !value.toString().isEmpty()) {
                                    try {
                                        cell.setCellValue(Long.parseLong(value.toString()));
                                        cell.setCellStyle(numStyle);
                                    } catch (NumberFormatException e) {
                                        cell.setCellValue(value.toString());
                                    }
                                } else {
                                    cell.setCellValue(value != null ? String.valueOf(value) : "");
                                }
                            }
                        });
                workbook.write(out);
                workbook.dispose();
            }

            long size = Files.size(tempFile);
            String filename = "report_market_contracts_" + normalizedType + "_" + System.currentTimeMillis() + ".xlsx";
            InputStream in = Files.newInputStream(tempFile);
            InputStream deletingStream = new FilterInputStream(in) {
                @Override
                public void close() throws IOException {
                    try { super.close(); } finally { Files.deleteIfExists(tempFile); }
                }
            };
            return ResponseEntity.ok()
                    .contentLength(size)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\""
                                    + "; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                    .body(new InputStreamResource(deletingStream));
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw e;
        } catch (Exception e) {
            Files.deleteIfExists(tempFile);
            throw new IOException(e);
        }
    }

    @Operation(summary = "시장데이터 공사/용역 계약 저장 여부 업데이트")
    @PatchMapping("/api/report/market-contracts/saved")
    public ResponseEntity<Map<String, Object>> updateMarketContractSaved(@RequestBody Map<String, Object> body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(error("요청 본문이 없습니다."));
        }

        String contractType = valueAsString(body.get("contractType"));
        boolean grouped = Boolean.TRUE.equals(body.get("grouped"));
        String saved = "Y".equalsIgnoreCase(valueAsString(body.get("saved"))) ? "Y" : "N";
        String vendorBizRegNo = valueAsString(body.get("vendorBizRegNo"));
        String key = grouped
                ? valueAsString(body.get("groupKey"))
                : firstNonBlank(
                        valueAsString(body.get("contractNo")),
                        valueAsString(body.get("contractDeliveryIntegratedNo")));

        if (contractType == null) {
            return ResponseEntity.badRequest().body(error("contractType 필요"));
        }
        if (key == null) {
            return ResponseEntity.badRequest().body(error(grouped ? "groupKey 필요" : "contractNo 필요"));
        }

        try {
            int updated = reportMarketService.updateSaved(contractType, grouped, key, vendorBizRegNo, saved);
            Map<String, Object> res = new HashMap<>();
            res.put("success", updated > 0);
            res.put("updated", updated);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private String[] excelHeaders(boolean grouped) {
        if (grouped) {
            return new String[]{"그룹키", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역",
                    "대분류", "중분류", "공공조달분류", "계약방법", "최초계약일자", "최초계약금액",
                    "최종계약일자", "최종계약금액(합계)", "계약건수", "장기계약여부"};
        }
        return new String[]{"계약번호", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역",
                "대분류", "중분류", "공공조달분류", "계약방법", "초년도계약번호", "최초계약일자",
                "계약일자", "최초계약금액", "계약금액", "계약변경차수", "장기계약여부"};
    }

    private String[] excelKeys(boolean grouped) {
        if (grouped) {
            return new String[]{"groupKey", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgencyName",
                    "demandAgencyRegion", "procurementWorkArea", "publicProcurementCategoryMid",
                    "publicProcurementCategory", "contractMethod", "firstContractDate", "firstContractAmount",
                    "finalContractDate", "finalContractAmount", "contractCount", "isLongTerm"};
        }
        return new String[]{"contractNo", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgencyName",
                "demandAgencyRegion", "procurementWorkArea", "publicProcurementCategoryMid",
                "publicProcurementCategory", "contractMethod", "firstYearContractNo", "firstContractDate",
                "contractDate", "firstContractAmount", "contractAmount", "latestChangeSeq", "isLongTerm"};
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", message);
        return err;
    }

    private String valueAsString(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : (second != null && !second.isBlank() ? second : null);
    }
}
