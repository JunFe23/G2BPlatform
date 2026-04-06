package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.g2bplatform.DTO.ShoppingMallFlatDto;
import org.example.g2bplatform.service.ReportConstructionService;
import org.example.g2bplatform.service.ReportDataService;
import org.example.g2bplatform.service.ReportProcurementService;
import org.example.g2bplatform.service.ReportServiceContractService;
import org.example.g2bplatform.service.ShoppingMallService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Report Data", description = "보고서 데이터 API")
@RestController
@RequestMapping("/api/report")
public class ReportDataController {

    private final ReportDataService reportDataService;
    private final ReportConstructionService reportConstructionService;
    private final ReportProcurementService reportProcurementService;
    private final ReportServiceContractService reportServiceContractService;
    private final ShoppingMallService shoppingMallService;

    public ReportDataController(ReportDataService reportDataService,
                                ReportConstructionService reportConstructionService,
                                ReportProcurementService reportProcurementService,
                                ReportServiceContractService reportServiceContractService,
                                ShoppingMallService shoppingMallService) {
        this.reportDataService = reportDataService;
        this.reportConstructionService = reportConstructionService;
        this.reportProcurementService = reportProcurementService;
        this.reportServiceContractService = reportServiceContractService;
        this.shoppingMallService = shoppingMallService;
    }

    /**
     * 시장현황 탭에서 사용하는 요약/차트/연차계약 데이터를 반환합니다.
     */
    @Operation(summary = "시장현황 데이터", description = "대시보드 시장현황 탭 데이터")
    @GetMapping("/market")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        // 시장현황(연차계약/전체 지표/차트) 탭에 필요한 집계 데이터를 반환
        return ResponseEntity.ok(reportDataService.getMarketOverview());
    }

    /**
     * 수요기관별 탭에서 사용하는 Top10/계약건수/평균단가/상세표 데이터를 반환합니다.
     */
    @Operation(summary = "수요기관별 데이터", description = "대시보드 수요기관별 탭 데이터")
    @GetMapping("/agency")
    public ResponseEntity<Map<String, Object>> getAgencyOverview() {
        // 수요기관별(Top10/계약건수/평균단가/상세표) 탭 데이터를 반환
        return ResponseEntity.ok(reportDataService.getAgencyOverview());
    }

    /**
     * 지역별 탭에서 사용하는 스택차트/비율/계약건수/상세표 데이터를 반환합니다.
     */
    @Operation(summary = "지역별 데이터", description = "대시보드 지역별 탭 데이터")
    @GetMapping("/region")
    public ResponseEntity<Map<String, Object>> getRegionOverview() {
        // 지역별(스택차트/비율/계약건수/상세표) 탭 데이터를 반환
        return ResponseEntity.ok(reportDataService.getRegionOverview());
    }

    /**
     * 지역별 물품 조달시장 분석(대시보드) - procurement_contract_summary 기반.
     * first_contract_date 기간 필터, final_contract_amount 집계.
     */
    @Operation(summary = "지역별 물품 조달시장 분석", description = "기간(from/to)에 맞는 최초계약일자로 필터, 최종계약금액 기준 지역별 집계")
    @GetMapping("/region-market")
    public ResponseEntity<Map<String, Object>> getRegionMarket(
            @Parameter(description = "기간 시작 (yyyy-mm-dd)", required = true) @RequestParam String from,
            @Parameter(description = "기간 종료 (yyyy-mm-dd)", required = true) @RequestParam String to,
            @Parameter(description = "데이터 소스: procurement | shopping_mall | all") @RequestParam(required = false, defaultValue = "procurement") String dataSource
    ) {
        try {
            return ResponseEntity.ok(reportDataService.getRegionMarket(from, to, dataSource));
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * 순위분석 탭에서 사용하는 Top 리스트/종합 순위표 데이터를 반환합니다.
     */
    @Operation(summary = "순위분석 데이터", description = "대시보드 순위분석 탭 데이터")
    @GetMapping("/rank")
    public ResponseEntity<Map<String, Object>> getRankOverview() {
        // 순위분석(Top 리스트/종합 순위표) 탭 데이터를 반환
        return ResponseEntity.ok(reportDataService.getRankOverview());
    }

    /**
     * 우수제품 탭에서 사용하는 요약 카드/보유 현황/알림/상세표 데이터를 반환합니다.
     */
    @Operation(summary = "우수제품 데이터", description = "대시보드 우수제품 탭 데이터")
    @GetMapping("/excellent")
    public ResponseEntity<Map<String, Object>> getExcellentOverview() {
        // 우수제품(요약 카드/보유 현황/알림/상세표) 탭 데이터를 반환
        return ResponseEntity.ok(reportDataService.getExcellentOverview());
    }

    /**
     * 민수관리 탭에서 사용하는 민수 계약 테이블 데이터를 반환합니다.
     */
    @Operation(summary = "민수관리 데이터", description = "대시보드 민수관리 탭 데이터")
    @GetMapping("/private")
    public ResponseEntity<Map<String, Object>> getPrivateOverview() {
        // 민수관리(민수 계약 테이블) 탭 데이터를 반환
        return ResponseEntity.ok(reportDataService.getPrivateOverview());
    }

    /**
     * 수요기관별 물품 조달시장 분석(대시보드) - 신규 집계 API.
     */
    @Operation(summary = "수요기관별 물품 조달시장 분석", description = "기간(from/to)과 기준일자(dateBasis)에 따라 수요기관별 매출/건수/평균단가 TopN을 집계합니다.")
    @GetMapping("/demand-agency-market")
    public ResponseEntity<Map<String, Object>> getDemandAgencyMarket(
            @Parameter(description = "기간 기준 (FINAL|FIRST)", required = false) @RequestParam(required = false, defaultValue = "FINAL") String dateBasis,
            @Parameter(description = "기간 시작 (yyyy-mm-dd)", required = true) @RequestParam String from,
            @Parameter(description = "기간 종료 (yyyy-mm-dd)", required = true) @RequestParam String to,
            @Parameter(description = "Top N (10 또는 20)", required = false) @RequestParam(required = false) Integer topN,
            @Parameter(description = "데이터 소스: procurement | shopping_mall | all") @RequestParam(required = false, defaultValue = "procurement") String dataSource
    ) {
        try {
            return ResponseEntity.ok(reportDataService.getDemandAgencyMarket(dateBasis, from, to, topN, dataSource));
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // ================================================================
    // 보고서 쇼핑몰 (shopping_mall_flat)
    // ================================================================

    @Operation(summary = "보고서 쇼핑몰 납품 실적 목록", description = "shopping_mall_flat 기준 (보고서 데이터 전용 경로)")
    @GetMapping("/shopping-mall")
    public ResponseEntity<Map<String, Object>> getReportShoppingMallFlat(
            @Parameter(description = "기간 시작 (ref_date >=), yyyy-MM-dd") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "기간 종료 (ref_date <=), yyyy-MM-dd") @RequestParam(required = false) String dateTo,
            @Parameter(description = "업체사업자등록번호") @RequestParam(required = false) String vendorBizRegNo,
            @Parameter(description = "수요기관명 (부분일치)") @RequestParam(required = false) String demandAgencyName,
            @Parameter(description = "수요기관지역 (부분일치)") @RequestParam(required = false) String demandAgencyRegion,
            @Parameter(description = "물품분류번호") @RequestParam(required = false) String itemCategoryNo,
            @Parameter(description = "세부품명번호") @RequestParam(required = false) String detailItemNo,
            @Parameter(description = "MAS여부 Y/N") @RequestParam(required = false) String isMas,
            @Parameter(description = "우수제품여부 Y/N") @RequestParam(required = false) String isExcellentProduct,
            @Parameter(description = "페이지 (1부터)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int size
    ) {
        Map<String, Object> data = shoppingMallService.getFlatList(
                dateFrom, dateTo, vendorBizRegNo,
                demandAgencyName, demandAgencyRegion,
                itemCategoryNo, detailItemNo,
                isMas, isExcellentProduct,
                page, size);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.putAll(data);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "보고서 쇼핑몰 납품 실적 엑셀 다운로드", description = "현재 검색 조건 전체 행 (배치 조회)")
    @GetMapping("/shopping-mall/excel")
    public ResponseEntity<Resource> getReportShoppingMallExcel(
            @Parameter(description = "기간 시작") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "기간 종료") @RequestParam(required = false) String dateTo,
            @Parameter(description = "업체사업자등록번호") @RequestParam(required = false) String vendorBizRegNo,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String demandAgencyName,
            @Parameter(description = "수요기관지역") @RequestParam(required = false) String demandAgencyRegion,
            @Parameter(description = "물품분류번호") @RequestParam(required = false) String itemCategoryNo,
            @Parameter(description = "세부품명번호") @RequestParam(required = false) String detailItemNo,
            @Parameter(description = "MAS Y/N") @RequestParam(required = false) String isMas,
            @Parameter(description = "우수제품 Y/N") @RequestParam(required = false) String isExcellentProduct
    ) throws IOException {
        final String[] headerNames = new String[]{
                "납품요구결재일", "수요기관명", "수요기관지역", "계약명(요청명)", "물품분류명", "세부품명", "물품식별명",
                "단가", "수량", "공급금액", "MAS", "우수제품", "직접구매", "업체명", "납품장소명"
        };
        Path tempFile = Files.createTempFile("report_shopping_mall_", ".xlsx");
        try {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("쇼핑몰납품실적");
                DataFormat dataFormat = workbook.createDataFormat();
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(dataFormat.getFormat("#,##0"));

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headerNames[i]);
                }

                final int[] rowNumRef = {1};
                shoppingMallService.forEachFlatBatch(
                        dateFrom, dateTo, vendorBizRegNo,
                        demandAgencyName, demandAgencyRegion,
                        itemCategoryNo, detailItemNo,
                        isMas, isExcellentProduct,
                        2000,
                        batch -> {
                            for (ShoppingMallFlatDto dto : batch) {
                                Row excelRow = sheet.createRow(rowNumRef[0]++);
                                int c = 0;
                                setDateCell(excelRow.createCell(c++), dto.getRefDate());
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getDemandAgencyName()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getDemandAgencyRegion()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getContractTitle()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getItemCategoryName()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getDetailItemName()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getItemIdentifierName()));
                                setLongCell(excelRow.createCell(c++), dto.getUnitPrice(), numStyle);
                                setLongCell(excelRow.createCell(c++), dto.getQuantity(), numStyle);
                                setLongCell(excelRow.createCell(c++), dto.getSupplyAmount(), numStyle);
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getIsMas()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getIsExcellentProduct()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getIsDirectPurchaseTarget()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getVendorName()));
                                excelRow.createCell(c++).setCellValue(nullToEmpty(dto.getDeliveryPlaceName()));
                            }
                        });
                workbook.write(out);
                workbook.dispose();
            }

            long fileSize = Files.size(tempFile);
            String filename = "report_shopping_mall_" + System.currentTimeMillis() + ".xlsx";
            InputStream in = Files.newInputStream(tempFile);
            InputStream deletingStream = new FilterInputStream(in) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        Files.deleteIfExists(tempFile);
                    }
                }
            };
            return ResponseEntity.ok()
                    .contentLength(fileSize)
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

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static void setDateCell(Cell cell, LocalDate d) {
        if (d != null) {
            cell.setCellValue(d.toString());
        } else {
            cell.setCellValue("");
        }
    }

    private static void setLongCell(Cell cell, Long value, CellStyle numStyle) {
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(numStyle);
        } else {
            cell.setCellValue("");
        }
    }

    // ================================================================
    // 보고서 공사 (construction_contract_flat / construction_contract_grouped)
    // ================================================================

    /**
     * 보고서 공사 목록 조회.
     * grouped=true  → construction_contract_grouped, 기간 필터: initial_contract_date
     * grouped=false → construction_contract_flat,   기간 필터: contract_date
     */
    @Operation(summary = "보고서 공사 목록", description = "토글에 따라 grouped/flat 테이블을 조회합니다.")
    @GetMapping("/constructions")
    public ResponseEntity<Map<String, Object>> getReportConstructions(
            @Parameter(description = "합쳐서 보기 여부 (true=grouped, false=flat)") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "시작 행") @RequestParam(defaultValue = "0") int start,
            @Parameter(description = "조회 건수") @RequestParam(defaultValue = "100") int length,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "품명내용") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "입찰계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "최초계약일자(YYYY-MM-DD)") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월(YYYY-MM)") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작(YYYY-MM)") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료(YYYY-MM)") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) {
        List<Map<String, Object>> list = reportConstructionService.getList(
                grouped, start, length,
                dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        int filtered = reportConstructionService.getCount(
                grouped,
                dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", list);
        body.put("recordsFiltered", filtered);
        return ResponseEntity.ok(body);
    }

    /**
     * 보고서 공사 엑셀 다운로드 (동일 검색 조건, SXSSFWorkbook 스트리밍).
     */
    @Operation(summary = "보고서 공사 엑셀 다운로드")
    @GetMapping("/constructions/excel")
    public ResponseEntity<Resource> getReportConstructionsExcel(
            @Parameter(description = "합쳐서 보기 여부") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "품명내용") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "입찰계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "최초계약일자") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) throws IOException {
        final String[] headerNames = grouped
                ? new String[]{"그룹키", "업체명", "계약건명", "수요기관명", "수요기관지역명", "품명내용",
                        "입찰계약방법", "입찰공고번호", "최초계약일자", "최초계약금액", "최종계약일자",
                        "최종계약금액(합계)", "계약변경차수", "그룹내건수", "장기계약여부"}
                : new String[]{"계약번호", "업체명", "계약건명", "수요기관명", "수요기관지역명", "품명내용",
                        "입찰계약방법", "입찰공고번호", "최초계약일자", "최초계약금액", "계약일자",
                        "계약금액", "계약변경차수", "장기계약여부"};
        final String[] keys = grouped
                ? new String[]{"groupKey", "vendorName", "contractTitle", "demandAgencyName", "demandAgencyRegion", "detailItemName",
                        "contractMethod", "bidNoticeNo", "firstContractDate", "firstContractAmount", "finalContractDate",
                        "finalContractAmount", "contractCount", "groupContractCount", "isLongTerm"}
                : new String[]{"contractNo", "vendorName", "contractTitle", "demandAgencyName", "demandAgencyRegion", "detailItemName",
                        "contractMethod", "bidNoticeNo", "firstContractDate", "firstContractAmount", "finalContractDate",
                        "finalContractAmount", "contractCount", "isLongTerm"};

        final Set<String> constructionAmountKeys = new HashSet<>(Arrays.asList(
                "firstContractAmount", "finalContractAmount"));

        Path tempFile = Files.createTempFile("report_constructions_", ".xlsx");
        try {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("보고서공사");
                DataFormat dataFormat = workbook.createDataFormat();
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(dataFormat.getFormat("#,##0"));

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headerNames[i]);
                }
                final int[] rowNumRef = {1};
                reportConstructionService.streamForExcel(
                        grouped,
                        dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                        firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                        resultContext -> {
                            Map<String, Object> row = resultContext.getResultObject();
                            Row excelRow = sheet.createRow(rowNumRef[0]++);
                            for (int colNum = 0; colNum < keys.length; colNum++) {
                                Object value = row != null ? row.getOrDefault(keys[colNum], "") : "";
                                Cell cell = excelRow.createCell(colNum);
                                if (constructionAmountKeys.contains(keys[colNum]) && value != null && !value.toString().isEmpty()) {
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
            String filename = "report_constructions_" + System.currentTimeMillis() + ".xlsx";
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

    /**
     * 보고서 공사 saved 체크박스 갱신.
     * grouped=true  → group_key 기준
     * grouped=false → contract_no 기준
     */
    @Operation(summary = "보고서 공사 저장 여부 업데이트")
    @PatchMapping("/constructions/saved")
    public ResponseEntity<Map<String, Object>> updateReportConstructionsSaved(@RequestBody Map<String, Object> body) {
        boolean grouped = body != null && Boolean.TRUE.equals(body.get("grouped"));
        String saved = body != null && body.get("saved") != null
                ? ("Y".equalsIgnoreCase(body.get("saved").toString()) ? "Y" : "N") : "N";
        Object keyObj = body == null ? null : (grouped ? body.get("groupKey") : body.get("contractNo"));
        String key = keyObj != null ? keyObj.toString() : null;

        if (key == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", grouped ? "groupKey 필요" : "contractNo 필요");
            return ResponseEntity.badRequest().body(err);
        }
        int updated = reportConstructionService.updateSaved(grouped, key, saved);
        Map<String, Object> res = new HashMap<>();
        res.put("success", updated > 0);
        res.put("updated", updated);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // 보고서 물품 계약 (procurement_contract_flat / procurement_contract_grouped)
    // ================================================================

    /**
     * 보고서 물품 계약 목록 조회.
     * grouped=true  → procurement_contract_grouped, 기간 필터: initial_contract_date
     * grouped=false → procurement_contract_flat,    기간 필터: contract_date
     */
    @Operation(summary = "보고서 물품 계약 목록", description = "토글에 따라 grouped/flat 테이블을 조회합니다.")
    @GetMapping("/procurements")
    public ResponseEntity<Map<String, Object>> getReportProcurements(
            @Parameter(description = "합쳐서 보기 여부 (true=grouped, false=flat)") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "시작 행") @RequestParam(defaultValue = "0") int start,
            @Parameter(description = "조회 건수") @RequestParam(defaultValue = "100") int length,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "세부품명") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "물품분류번호") @RequestParam(required = false) String itemCategoryNo,
            @Parameter(description = "입찰계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "최초계약일자(YYYY-MM-DD)") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월(YYYY-MM)") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작(YYYY-MM)") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료(YYYY-MM)") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) {
        List<Map<String, Object>> list = reportProcurementService.getList(
                grouped, start, length,
                dminsttNm, dminsttNmDetail, prdctClsfcNo, itemCategoryNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        int filtered = reportProcurementService.getCount(
                grouped,
                dminsttNm, dminsttNmDetail, prdctClsfcNo, itemCategoryNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", list);
        body.put("recordsFiltered", filtered);
        return ResponseEntity.ok(body);
    }

    /**
     * 보고서 물품 계약 엑셀 다운로드 (동일 검색 조건, SXSSFWorkbook 스트리밍).
     */
    @Operation(summary = "보고서 물품 계약 엑셀 다운로드")
    @GetMapping("/procurements/excel")
    public ResponseEntity<Resource> getReportProcurementsExcel(
            @Parameter(description = "합쳐서 보기 여부") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "세부품명") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "물품분류번호") @RequestParam(required = false) String itemCategoryNo,
            @Parameter(description = "입찰계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "최초계약일자") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) throws IOException {
        final String[] headerNames = grouped
                ? new String[]{"입찰공고번호", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역명",
                        "입찰계약방법", "물품분류번호", "세부품명", "최초계약일자", "최초계약금액",
                        "최종계약일자", "최종계약금액(합계)", "계약건수", "장기계약여부"}
                : new String[]{"계약번호", "물품순번", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역명",
                        "입찰계약방법", "입찰공고번호", "물품분류번호", "물품분류명", "세부품명번호", "세부품명",
                        "물품식별번호", "물품식별명", "단위", "단가", "수량", "MAS여부", "우수제품여부", "중기간경쟁여부",
                        "최초계약일자", "계약일자", "계약금액", "계약변경차수", "장기계약여부"};
        final String[] keys = grouped
                ? new String[]{"bidNoticeNo", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgencyName", "demandAgencyRegion",
                        "contractMethod", "itemCategoryNo", "detailItemName", "initialContractDate", "initialContractAmount",
                        "finalContractDate", "finalContractAmountSum", "contractCount", "isLongTerm"}
                : new String[]{"contractNo", "itemSeq", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgencyName", "demandAgencyRegion",
                        "contractMethod", "bidNoticeNo", "itemCategoryNo", "itemCategoryName", "detailItemNo", "detailItemName",
                        "itemIdentifierNo", "itemIdentifierName", "unit", "unitPrice", "quantity", "isMas", "isExcellentProduct", "isSmeCompetitive",
                        "firstContractDate", "contractDate", "contractAmount", "latestChangeSeq", "isLongTerm"};

        final Set<String> procurementAmountKeys = new HashSet<>(Arrays.asList(
                "initialContractAmount", "finalContractAmountSum",
                "unitPrice", "quantity", "contractAmount"));

        Path tempFile = Files.createTempFile("report_procurements_", ".xlsx");
        try {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("보고서물품계약");
                DataFormat dataFormat = workbook.createDataFormat();
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(dataFormat.getFormat("#,##0"));

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headerNames[i]);
                }
                final int[] rowNumRef = {1};
                reportProcurementService.streamForExcel(
                        grouped,
                        dminsttNm, dminsttNmDetail, prdctClsfcNo, itemCategoryNo, cntctCnclsMthdNm,
                        firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                        resultContext -> {
                            Map<String, Object> row = resultContext.getResultObject();
                            Row excelRow = sheet.createRow(rowNumRef[0]++);
                            for (int colNum = 0; colNum < keys.length; colNum++) {
                                Object value = row != null ? row.getOrDefault(keys[colNum], "") : "";
                                Cell cell = excelRow.createCell(colNum);
                                if (procurementAmountKeys.contains(keys[colNum]) && value != null && !value.toString().isEmpty()) {
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
            String filename = "report_procurements_" + System.currentTimeMillis() + ".xlsx";
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

    /**
     * 보고서 물품 계약 saved 체크박스 갱신.
     * grouped=true  → (bid_notice_no, vendor_biz_reg_no, contract_no) 3-PK UPDATE
     * grouped=false → (contract_no, item_seq) 2-PK UPDATE
     */
    @Operation(summary = "보고서 물품 계약 저장 여부 업데이트")
    @PatchMapping("/procurements/saved")
    public ResponseEntity<Map<String, Object>> updateReportProcurementsSaved(@RequestBody Map<String, Object> body) {
        if (body == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "요청 본문이 없습니다.");
            return ResponseEntity.badRequest().body(err);
        }
        boolean grouped = Boolean.TRUE.equals(body.get("grouped"));
        String saved = body.get("saved") != null
                ? ("Y".equalsIgnoreCase(body.get("saved").toString()) ? "Y" : "N") : "N";

        Map<String, Object> err = new HashMap<>();
        int updated;

        if (grouped) {
            String bidNoticeNo = body.get("bidNoticeNo") != null ? body.get("bidNoticeNo").toString() : null;
            String vendorBizRegNo = body.get("vendorBizRegNo") != null ? body.get("vendorBizRegNo").toString() : null;
            String contractNo = body.get("contractNo") != null ? body.get("contractNo").toString() : null;
            if (bidNoticeNo == null || vendorBizRegNo == null || contractNo == null) {
                err.put("success", false);
                err.put("message", "bidNoticeNo, vendorBizRegNo, contractNo 필요");
                return ResponseEntity.badRequest().body(err);
            }
            updated = reportProcurementService.updateGroupedSaved(bidNoticeNo, vendorBizRegNo, contractNo, saved);
        } else {
            String contractNo = body.get("contractNo") != null ? body.get("contractNo").toString() : null;
            Object itemSeqObj = body.get("itemSeq");
            if (contractNo == null || itemSeqObj == null) {
                err.put("success", false);
                err.put("message", "contractNo, itemSeq 필요");
                return ResponseEntity.badRequest().body(err);
            }
            Long itemSeq = Long.parseLong(itemSeqObj.toString());
            updated = reportProcurementService.updateFlatSaved(contractNo, itemSeq, saved);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", updated > 0);
        res.put("updated", updated);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // 보고서 용역 계약 (service_contract_flat / service_contract_grouped)
    // ================================================================

    /**
     * 보고서 용역 계약 목록 조회.
     * grouped=true  → service_contract_grouped, 기간 필터: initial_contract_date
     * grouped=false → service_contract_flat,    기간 필터: contract_date
     */
    @Operation(summary = "보고서 용역 계약 목록", description = "토글에 따라 grouped/flat 테이블을 조회합니다.")
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getReportServices(
            @Parameter(description = "합쳐서 보기 여부 (true=grouped, false=flat)") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "시작 행") @RequestParam(defaultValue = "0") int start,
            @Parameter(description = "조회 건수") @RequestParam(defaultValue = "100") int length,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "조달업무영역") @RequestParam(required = false) String procurementWorkArea,
            @Parameter(description = "계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "공공조달분류 중분류") @RequestParam(required = false) String publicProcurementCategoryMid,
            @Parameter(description = "공공조달분류 소분류") @RequestParam(required = false) String publicProcurementCategory,
            @Parameter(description = "최초계약일자(YYYY-MM-DD)") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월(YYYY-MM)") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작(YYYY-MM)") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료(YYYY-MM)") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) {
        List<Map<String, Object>> list = reportServiceContractService.getList(
                grouped, start, length,
                dminsttNm, dminsttNmDetail, procurementWorkArea, cntctCnclsMthdNm,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        int filtered = reportServiceContractService.getCount(
                grouped,
                dminsttNm, dminsttNmDetail, procurementWorkArea, cntctCnclsMthdNm,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        Map<String, Object> respBody = new HashMap<>();
        respBody.put("success", true);
        respBody.put("data", list);
        respBody.put("recordsFiltered", filtered);
        return ResponseEntity.ok(respBody);
    }

    /**
     * 보고서 용역 계약 엑셀 다운로드 (동일 검색 조건, SXSSFWorkbook 스트리밍).
     */
    @Operation(summary = "보고서 용역 계약 엑셀 다운로드")
    @GetMapping("/services/excel")
    public ResponseEntity<Resource> getReportServicesExcel(
            @Parameter(description = "합쳐서 보기 여부") @RequestParam(defaultValue = "true") boolean grouped,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "조달업무영역") @RequestParam(required = false) String procurementWorkArea,
            @Parameter(description = "계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "공공조달분류 중분류") @RequestParam(required = false) String publicProcurementCategoryMid,
            @Parameter(description = "공공조달분류 소분류") @RequestParam(required = false) String publicProcurementCategory,
            @Parameter(description = "최초계약일자") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) String month,
            @Parameter(description = "기간 시작") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "기간 종료") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 데이터만") @RequestParam(required = false, defaultValue = "false") boolean showSavedOnly
    ) throws IOException {
        final String[] headerNames = grouped
                ? new String[]{"그룹키", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역",
                        "계약방법", "조달업무영역", "공공조달분류(중)", "공공조달분류(소)",
                        "최초계약일자", "최초계약금액", "최종계약일자", "최종계약금액(합계)", "계약건수", "장기계속여부"}
                : new String[]{"계약납품통합번호", "업체사업자번호", "업체명", "계약명", "수요기관명", "수요기관지역",
                        "계약방법", "계약유형", "조달업무영역", "입찰공고번호", "초년도계약번호",
                        "장기계속여부", "대표물품분류코드", "대표물품분류명", "세부품명코드",
                        "공공조달분류(소)", "공공조달분류(중)",
                        "최초기준일자", "기준일자", "착수일자", "완수일자",
                        "최초계약금액", "계약금액", "계약변경차수"};
        final String[] keys = grouped
                ? new String[]{"groupKey", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgency", "demandAgencyRegion",
                        "contractMethod", "procurementWorkArea", "publicProcurementCategoryMid", "publicProcurementCategory",
                        "initialContractDate", "initialContractAmount", "finalContractDate", "finalContractAmountSum", "contractCount", "isLongTerm"}
                : new String[]{"contractDeliveryIntegratedNo", "vendorBizRegNo", "vendorName", "contractTitle", "demandAgency", "demandAgencyRegion",
                        "contractMethod", "contractType", "procurementWorkArea", "bidNoticeNo", "initialYearContractNo",
                        "isLongTerm", "representativeItemCategoryCode", "representativeItemCategory", "detailItemCode",
                        "publicProcurementCategory", "publicProcurementCategoryMid",
                        "firstContractDate", "contractDate", "startDate", "completionDate",
                        "firstContractAmount", "contractAmount", "latestChangeSeq"};

        final Set<String> serviceAmountKeys = new HashSet<>(Arrays.asList(
                "initialContractAmount", "finalContractAmountSum",
                "firstContractAmount", "contractAmount"));

        Path tempFile = Files.createTempFile("report_services_", ".xlsx");
        try {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("보고서용역계약");
                DataFormat dataFormat = workbook.createDataFormat();
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(dataFormat.getFormat("#,##0"));

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headerNames[i]);
                }
                final int[] rowNumRef = {1};
                reportServiceContractService.streamForExcel(
                        grouped,
                        dminsttNm, dminsttNmDetail, procurementWorkArea, cntctCnclsMthdNm,
                        publicProcurementCategoryMid, publicProcurementCategory,
                        firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                        resultContext -> {
                            Map<String, Object> row = resultContext.getResultObject();
                            Row excelRow = sheet.createRow(rowNumRef[0]++);
                            for (int colNum = 0; colNum < keys.length; colNum++) {
                                Object value = row != null ? row.getOrDefault(keys[colNum], "") : "";
                                Cell cell = excelRow.createCell(colNum);
                                if (serviceAmountKeys.contains(keys[colNum]) && value != null && !value.toString().isEmpty()) {
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
            String filename = "report_services_" + System.currentTimeMillis() + ".xlsx";
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

    /**
     * 보고서 용역 계약 saved 체크박스 갱신.
     * grouped=true  → (group_key, vendor_biz_reg_no) 2-PK UPDATE
     * grouped=false → (contract_delivery_integrated_no, vendor_biz_reg_no) 2-PK UPDATE
     */
    @Operation(summary = "보고서 용역 계약 저장 여부 업데이트")
    @PatchMapping("/services/saved")
    public ResponseEntity<Map<String, Object>> updateReportServicesSaved(@RequestBody Map<String, Object> body) {
        if (body == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "요청 본문이 없습니다.");
            return ResponseEntity.badRequest().body(err);
        }
        boolean grouped = Boolean.TRUE.equals(body.get("grouped"));
        String saved = body.get("saved") != null
                ? ("Y".equalsIgnoreCase(body.get("saved").toString()) ? "Y" : "N") : "N";
        String vendorBizRegNo = body.get("vendorBizRegNo") != null ? body.get("vendorBizRegNo").toString() : null;

        Map<String, Object> err = new HashMap<>();
        int updated;

        if (grouped) {
            String groupKey = body.get("groupKey") != null ? body.get("groupKey").toString() : null;
            if (groupKey == null || vendorBizRegNo == null) {
                err.put("success", false);
                err.put("message", "groupKey, vendorBizRegNo 필요");
                return ResponseEntity.badRequest().body(err);
            }
            updated = reportServiceContractService.updateGroupedSaved(groupKey, vendorBizRegNo, saved);
        } else {
            String contractDeliveryIntegratedNo = body.get("contractDeliveryIntegratedNo") != null
                    ? body.get("contractDeliveryIntegratedNo").toString() : null;
            if (contractDeliveryIntegratedNo == null || vendorBizRegNo == null) {
                err.put("success", false);
                err.put("message", "contractDeliveryIntegratedNo, vendorBizRegNo 필요");
                return ResponseEntity.badRequest().body(err);
            }
            updated = reportServiceContractService.updateFlatSaved(contractDeliveryIntegratedNo, vendorBizRegNo, saved);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", updated > 0);
        res.put("updated", updated);
        return ResponseEntity.ok(res);
    }
}
