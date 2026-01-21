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

    @Operation(summary = "시장현황 데이터", description = "대시보드 시장현황 탭 데이터")
    @GetMapping("/market")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        return ResponseEntity.ok(reportDataService.getMarketOverview());
    }

    @Operation(summary = "수요기관별 데이터", description = "대시보드 수요기관별 탭 데이터")
    @GetMapping("/agency")
    public ResponseEntity<Map<String, Object>> getAgencyOverview() {
        return ResponseEntity.ok(reportDataService.getAgencyOverview());
    }

    @Operation(summary = "지역별 데이터", description = "대시보드 지역별 탭 데이터")
    @GetMapping("/region")
    public ResponseEntity<Map<String, Object>> getRegionOverview() {
        return ResponseEntity.ok(reportDataService.getRegionOverview());
    }

    @Operation(summary = "순위분석 데이터", description = "대시보드 순위분석 탭 데이터")
    @GetMapping("/rank")
    public ResponseEntity<Map<String, Object>> getRankOverview() {
        return ResponseEntity.ok(reportDataService.getRankOverview());
    }

    @Operation(summary = "우수제품 데이터", description = "대시보드 우수제품 탭 데이터")
    @GetMapping("/excellent")
    public ResponseEntity<Map<String, Object>> getExcellentOverview() {
        return ResponseEntity.ok(reportDataService.getExcellentOverview());
    }

    @Operation(summary = "민수관리 데이터", description = "대시보드 민수관리 탭 데이터")
    @GetMapping("/private")
    public ResponseEntity<Map<String, Object>> getPrivateOverview() {
        return ResponseEntity.ok(reportDataService.getPrivateOverview());
    }
}
