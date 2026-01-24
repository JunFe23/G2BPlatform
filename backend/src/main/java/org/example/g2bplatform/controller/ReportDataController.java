package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.g2bplatform.service.ReportDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
