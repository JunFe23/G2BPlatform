package org.example.g2bplatform.controller;

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

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataService dataService;

    private final ExcelService excelService;

    public DataController(DataService dataService, ExcelService excelService) {
        this.dataService = dataService;
        this.excelService = excelService;
    }

    @GetMapping("/data")
    public Map<String, Object> getData( @RequestParam int draw,
                                              @RequestParam int start,
                                              @RequestParam int length,
                                              @RequestParam Map<String, String> search, // Map으로 검색 값을 받아옵니다
                                              @RequestParam String category,
                                        @RequestParam(required = false) String dminsttNm,         // 수요기관명
                                        @RequestParam(required = false) String dminsttNmDetail, // 수요기관 지역명
                                        @RequestParam(required = false) String prdctClsfcNo,    // 품명 내용
                                        @RequestParam(required = false) String cntctCnclsMthdNm,// 입찰 계약방법
                                        @RequestParam(required = false) String firstCntrctDate,  // 최초 계약일자
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) String month,
                                        @RequestParam(required = false) String rangeStart,
                                        @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(required = false) int showSavedOnly,
                                        @RequestParam(required = false) String type
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
        }
        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", totalRecords);
        response.put("recordsFiltered", filteredRecords);
        response.put("data", data);

        return response;
    }

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

    // 체크박스 상태 업데이트 API
    @PostMapping("/update-checked")
    public ResponseEntity<String> updateChecked(@RequestBody Map<String, Object> request) {
        try {
            // 데이터베이스에서 업데이트 처리
            if(request.get("category").equals("goods")) {
                dataService.updateCheckedThings((int) request.get("id"), (boolean) request.get("checked"));
            } else if(request.get("category").equals("services")){
                dataService.updateCheckedServices((int) request.get("id"), (boolean) request.get("checked"));
            } else if(request.get("category").equals("constructions")){
                dataService.updateCheckedConstructions((int) request.get("id"), (boolean) request.get("checked"));
            }
            return ResponseEntity.ok("체크박스 상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("체크박스 상태 업데이트 중 오류가 발생했습니다.");
        }
    }
}
