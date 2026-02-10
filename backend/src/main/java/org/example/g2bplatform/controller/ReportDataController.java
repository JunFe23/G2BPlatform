package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.g2bplatform.service.ReportDataService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Report Data", description = "보고서 데이터 API")
@RestController
@RequestMapping("/api/report")
public class ReportDataController {

    private final ReportDataService reportDataService;

    public ReportDataController(ReportDataService reportDataService) {
        this.reportDataService = reportDataService;
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
            @Parameter(description = "기간 종료 (yyyy-mm-dd)", required = true) @RequestParam String to
    ) {
        try {
            return ResponseEntity.ok(reportDataService.getRegionMarket(from, to));
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
            @Parameter(description = "Top N (10 또는 20)", required = false) @RequestParam(required = false) Integer topN
    ) {
        try {
            return ResponseEntity.ok(reportDataService.getDemandAgencyMarket(dateBasis, from, to, topN));
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * 보고서 물품 목록 조회 (procurement_contract_summary 기반, 검색/페이징).
     */
    @Operation(summary = "보고서 물품 목록", description = "수요기관명/지역/품명/입찰방법/최초계약일자(연·월·기간) 검색, 저장여부 필터")
    @GetMapping("/goods")
    public ResponseEntity<Map<String, Object>> getReportGoods(
            @Parameter(description = "시작 행") @RequestParam(defaultValue = "0") int start,
            @Parameter(description = "조회 건수") @RequestParam(defaultValue = "50") int length,
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
        List<Map<String, Object>> list = reportDataService.getReportGoodsList(
                start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        int filtered = reportDataService.getReportGoodsCount(
                dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", list);
        body.put("recordsFiltered", filtered);
        return ResponseEntity.ok(body);
    }

    /**
     * 보고서 물품 엑셀 다운로드 (동일 검색 조건).
     */
    @Operation(summary = "보고서 물품 엑셀 다운로드")
    @GetMapping("/goods/excel")
    public ResponseEntity<Resource> getReportGoodsExcel(
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
        final String[] headerNames = {
                "입찰공고번호", "업체명", "업체사업자등록번호", "계약명", "수요기관명", "수요기관지역명",
                "품명내용", "입찰계약방법", "최초계약일자", "최초계약금액", "최종계약일자", "최종계약금액",
                "계약차수", "장기계약여부"
        };
        final String[] keys = {
                "bidNoticeNo", "vendorName", "vendorBizRegNo", "contractTitle", "demandAgencyName", "demandAgencyRegion",
                "detailItemName", "contractMethod", "firstContractDate", "firstContractAmount", "finalContractDate", "finalContractAmount",
                "contractCount", "isLongTerm"
        };

        Path tempFile = Files.createTempFile("report_goods_", ".xlsx");
        try {
            // 스트리밍: 한 번의 쿼리로 fetchSize 단위 전달, idx_pcs_order 사용. 2만 건대도 수십 초 내 완료 목표.
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500); OutputStream out = Files.newOutputStream(tempFile)) {
                Sheet sheet = workbook.createSheet("보고서물품");

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headerNames.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headerNames[i]);
                }

                final int[] rowNumRef = { 1 };
                reportDataService.streamReportGoodsForExcel(
                        dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm,
                        firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                        resultContext -> {
                            Map<String, Object> row = resultContext.getResultObject();
                            Row excelRow = sheet.createRow(rowNumRef[0]++);
                            for (int colNum = 0; colNum < keys.length; colNum++) {
                                Object value = row != null ? row.getOrDefault(keys[colNum], "") : "";
                                Cell cell = excelRow.createCell(colNum);
                                cell.setCellValue(value != null ? String.valueOf(value) : "");
                            }
                        }
                );

                workbook.write(out);
                workbook.dispose();
            }

            long size = Files.size(tempFile);
            String filename = "report_goods_" + System.currentTimeMillis() + ".xlsx";
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
                    .contentLength(size)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
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
     * 보고서 물품 저장 여부 업데이트 (체크 시 DB에 저장).
     */
    @Operation(summary = "보고서 물품 저장 여부 업데이트")
    @PatchMapping("/goods/saved")
    public ResponseEntity<Map<String, Object>> updateReportGoodsSaved(@RequestBody Map<String, Object> body) {
        String bidNoticeNo = body != null && body.get("bidNoticeNo") != null ? body.get("bidNoticeNo").toString() : null;
        String vendorBizRegNo = body != null && body.get("vendorBizRegNo") != null ? body.get("vendorBizRegNo").toString() : null;
        String saved = body != null && body.get("saved") != null ? "Y".equalsIgnoreCase(body.get("saved").toString()) ? "Y" : "N" : "N";
        if (bidNoticeNo == null || vendorBizRegNo == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "bidNoticeNo, vendorBizRegNo 필요");
            return ResponseEntity.badRequest().body(err);
        }
        int updated = reportDataService.updateReportGoodsSaved(bidNoticeNo, vendorBizRegNo, saved);
        Map<String, Object> res = new HashMap<>();
        res.put("success", updated > 0);
        res.put("updated", updated);
        return ResponseEntity.ok(res);
    }
}
