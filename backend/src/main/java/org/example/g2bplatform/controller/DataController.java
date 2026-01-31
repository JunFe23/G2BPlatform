package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.example.g2bplatform.service.DataService;
import org.example.g2bplatform.service.ExcelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Data", description = "데이터 관련 API")
@RestController
@RequestMapping("/api")
public class DataController {

    private final DataService dataService;

    private final ExcelService excelService;

    public DataController(DataService dataService, ExcelService excelService) {
        this.dataService = dataService;
        this.excelService = excelService;
    }

    @Operation(summary = "데이터 조회", description = "카테고리 및 검색 조건에 따라 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/data")
    public Map<String, Object> getData(
            @Parameter(description = "DataTables의 그리기 카운터", required = true) @RequestParam int draw,
            @Parameter(description = "데이터 시작 위치", required = true) @RequestParam int start,
            @Parameter(description = "가져올 데이터 수", required = true) @RequestParam int length,
            @Parameter(description = "검색 값") @RequestParam Map<String, String> search,
            @Parameter(description = "데이터 카테고리 (goods, services, constructions, shoppingmall, onlyTop, servicesSelected)", required = true) @RequestParam String category,
            @Parameter(description = "수요기관명") @RequestParam(required = false) String dminsttNm,
            @Parameter(description = "수요기관 지역명") @RequestParam(required = false) String dminsttNmDetail,
            @Parameter(description = "품명 내용") @RequestParam(required = false) String prdctClsfcNo,
            @Parameter(description = "입찰 계약방법") @RequestParam(required = false) String cntctCnclsMthdNm,
            @Parameter(description = "최초 계약일자") @RequestParam(required = false) String firstCntrctDate,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "월") @RequestParam(required = false) String month,
            @Parameter(description = "날짜 범위 시작") @RequestParam(required = false) String rangeStart,
            @Parameter(description = "날짜 범위 끝") @RequestParam(required = false) String rangeEnd,
            @Parameter(description = "저장된 항목만 표시") @RequestParam(required = false) int showSavedOnly,
            @Parameter(description = "onlyTop 카테고리일 경우 타입") @RequestParam(required = false) String type
    ) {
        String searchValue = search.get("search[value]"); // DataTables가 전송하는 검색어

        List<Map<String, String>> data = new ArrayList<>();
        int totalRecords = 0;
        int filteredRecords = 0;

        // 예시 데이터 - 실제로는 데이터베이스에서 가져와야 함
        if ("goods".equals(category)) {
            data = dataService.getThingsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getThingsTotalCount(category);
            filteredRecords = dataService.getThingsFilteredCount(dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else if ("services".equals(category)) {
            data = dataService.getServicesData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getServicesTotalCount(category);
            filteredRecords = dataService.getServicesFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else if ("constructions".equals(category)) {
            data = dataService.getConstructionsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getConstructionsTotalCount(category);
            filteredRecords = dataService.getConstructionsFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else if ("shoppingmall".equals(category)) {
            data = dataService.getConstructionsData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getConstructionsTotalCount(category);
            filteredRecords = dataService.getConstructionsFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else if ("onlyTop".equals(category)) {
            data = dataService.getTopsData(type, start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getTopsTotalCount(category);
            filteredRecords = dataService.getTopsFilteredCount( type, start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else if ("servicesSelected".equals(category)) {
            data = dataService.getServicesSelectedData(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
            totalRecords = dataService.getServicesSelectedTotalCount(category);
            filteredRecords = dataService.getServicesSelectedFilteredCount(start, length, dminsttNm, dminsttNmDetail, prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", totalRecords);
        response.put("recordsFiltered", filteredRecords);
        response.put("data", data);

        return response;
    }

    @Operation(summary = "엑셀 다운로드", description = "조회된 데이터를 엑셀 파일로 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/data/excel")
    public void downloadExcel(@RequestBody Map<String, Object> requestData, HttpServletResponse response) throws IOException {
        // 숫자 값 변환
        Integer year = null;
        if (requestData.get("year") != null && !requestData.get("year").toString().isEmpty()) {
            try {
                year = Integer.parseInt(requestData.get("year").toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid year format: " + requestData.get("year"));
            }
        }

        String category = requestData.get("category").toString();
        List<Map<String, String>> data = new ArrayList<>();

        // category 별 데이터 조회
        if ("goods".equals(category)) {
            data = dataService.getThingsData(
                    0, Integer.MAX_VALUE, // 전체 데이터를 가져옴
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    (int) requestData.get("showSavedOnly")
            );
        } else if ("services".equals(category)) {
            data = dataService.getServicesData(
                    0, Integer.MAX_VALUE, // 전체 데이터를 가져옴
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    (int) requestData.get("showSavedOnly")
            );
        } else if ("constructions".equals(category)) {
            data = dataService.getConstructionsData(
                    0, Integer.MAX_VALUE, // 전체 데이터를 가져옴
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    (int) requestData.get("showSavedOnly")
            );
        } else if ("shoppingmall".equals(category)) {
            data = dataService.getConstructionsData(
                    0, Integer.MAX_VALUE,
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    requestData.get("showSavedOnly") != null ? (int) requestData.get("showSavedOnly") : 0
            );
        } else if ("onlyTop".equals(category)) {
            data = dataService.getTopsData(
                    (String) requestData.get("type"),
                    0, Integer.MAX_VALUE, // 전체 데이터를 가져옴
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    (int) requestData.get("showSavedOnly")
            );
        } else if ("servicesSelected".equals(category)) {
            data = dataService.getServicesSelectedData(
                    0, Integer.MAX_VALUE, // 전체 데이터를 가져옴
                    (String) requestData.get("dminsttNm"),
                    (String) requestData.get("dminsttNmDetail"),
                    (String) requestData.get("prdctClsfcNo"),
                    (String) requestData.get("cntctCnclsMthdNm"),
                    (String) requestData.get("firstCntrctDate"),
                    year,
                    (String) requestData.get("month"),
                    (String) requestData.get("rangeStart"),
                    (String) requestData.get("rangeEnd"),
                    (int) requestData.get("showSavedOnly")
            );
        }

        // 엑셀 파일 생성
        ByteArrayOutputStream excelFile = excelService.createExcelFile(data);

        // URL 인코딩된 파일 이름 생성
        String fileName = URLEncoder.encode("조회결과.xlsx", StandardCharsets.UTF_8.toString());

        // 응답 설정 및 파일 전송
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.getOutputStream().write(excelFile.toByteArray());
        response.getOutputStream().flush();
    }

    @Operation(summary = "체크박스 상태 업데이트", description = "항목의 체크박스 상태를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/update-checked")
    public ResponseEntity<String> updateChecked(@RequestBody Map<String, Object> request) {
        try {
            String category = (String) request.get("category");
            int id = request.get("id") instanceof Number ? ((Number) request.get("id")).intValue() : Integer.parseInt(String.valueOf(request.get("id")));
            boolean checked = Boolean.TRUE.equals(request.get("checked")) || "true".equalsIgnoreCase(String.valueOf(request.get("checked")));

            if ("goods".equals(category)) {
                dataService.updateCheckedThings(id, checked);
            } else if ("services".equals(category)) {
                dataService.updateCheckedServices(id, checked);
            } else if ("constructions".equals(category) || "shoppingmall".equals(category)) {
                dataService.updateCheckedConstructions(id, checked);
            } else if ("onlyTop".equals(category)) {
                dataService.updateCheckedTops(id, checked);
            } else {
                return ResponseEntity.badRequest().body("지원하지 않는 카테고리입니다: " + category);
            }
            return ResponseEntity.ok("체크박스 상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("체크박스 상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "물품 데이터 수동 처리", description = "일일 물품 계약 데이터를 수동으로 통합 가공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "생성 실패")
    })
    @PostMapping("/manual-process/dailyThings")
    public ResponseEntity<String> manualProcessGoods() {
        try {
            dataService.callProcedure("g2b.update_daily_contracts_things");
            return ResponseEntity.ok("✅ 물품 데이터 생성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 물품 데이터 생성 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "용역 데이터 수동 처리", description = "일일 용역 계약 데이터를 수동으로 통합 가공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "생성 실패")
    })
    @PostMapping("/manual-process/dailyServices")
    public ResponseEntity<String> manualProcessServices() {
        try {
            dataService.callProcedure("g2b.update_daily_contracts_services");
            return ResponseEntity.ok("✅ 용역 데이터 생성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 용역 데이터 생성 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "공사 데이터 수동 처리", description = "일일 공사 계약 데이터를 수동으로 통합 가공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "생성 실패")
    })
    @PostMapping("/manual-process/dailyConstructions")
    public ResponseEntity<String> manualProcessConstructions() {
        try {
            dataService.callProcedure("g2b.update_daily_contracts_constructions");
            return ResponseEntity.ok("✅ 공사 데이터 생성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 공사 데이터 생성 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "탑 데이터 수동 처리", description = "일일 탑 데이터를 수동으로 통합 가공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 완료"),
            @ApiResponse(responseCode = "500", description = "생성 실패")
    })
    @PostMapping("/manual-process/dailyTopDatas")
    public ResponseEntity<String> manualProcessTopDatas() {
        try {
            dataService.callProcedure("g2b.update_daily_contracts_topDatas");
            return ResponseEntity.ok("✅ 탑 데이터 생성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 탑 데이터 생성 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "모달 서비스 데이터 조회", description = "모달에 표시할 서비스 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/modal-service-data")
    public List<Map<String, Object>> getModalServiceData(
            @Parameter(description = "데이터 카테고리", required = true) @RequestParam String category,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "데이터 시작 위치", required = true) @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "가져올 데이터 수", required = true) @RequestParam(defaultValue = "20") int limit
    ) {
        return dataService.getModalServiceData(category, keyword, offset, limit);
    }

    @Operation(summary = "'is_selected' 상태 업데이트", description = "지정된 테이블의 항목에 대해 'is_selected' 상태를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/update-is-selected")
    public void updateIsSelected(@RequestBody Map<String, Object> payload) {
        String tableName = (String) payload.get("tableName");
        List<String> untyCntrctNos = (List<String>) payload.get("untyCntrctNos");

        dataService.updateIsSelected(tableName, untyCntrctNos);
    }

    @Operation(summary = "선택 해제", description = "지정된 테이블의 항목을 선택 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선택 해제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/unselect")
    public ResponseEntity<?> unselect(@RequestBody Map<String, Object> payload) {
        try {
            String tableName = (String) payload.get("tableName");
            String untyCntrctNo = (String) payload.get("untyCntrctNo");

            if (tableName == null || untyCntrctNo == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "tableName과 untyCntrctNo는 필수입니다."
                ));
            }

            // 항상 selectType=2 (수주대상)
            int deletedCount = dataService.unselect(tableName, untyCntrctNo, 2);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
