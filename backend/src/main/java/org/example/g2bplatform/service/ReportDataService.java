package org.example.g2bplatform.service;

import org.example.g2bplatform.mapper.ProcurementContractSummaryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportDataService {

    private final ProcurementContractSummaryMapper procurementContractSummaryMapper;

    public ReportDataService(ProcurementContractSummaryMapper procurementContractSummaryMapper) {
        this.procurementContractSummaryMapper = procurementContractSummaryMapper;
    }

    /**
     * 시장현황 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getMarketOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contractCards", List.of());
        data.put("summaryStats", List.of());
        data.put("revenueBars", List.of());
        data.put("countBars", List.of());
        data.put("detailItems", List.of());
        return wrap(data);
    }

    /**
     * 수요기관별 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getAgencyOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topSales", List.of());
        data.put("countBars", List.of());
        data.put("avgBars", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    /**
     * 지역별 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getRegionOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stackedBars", List.of());
        data.put("pieLabels", List.of());
        data.put("countBars", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    /**
     * 순위분석 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getRankOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tabs", List.of());
        data.put("topItems", List.of());
        data.put("summaryRows", List.of());
        return wrap(data);
    }

    /**
     * 우수제품 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getExcellentOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summaryCards", List.of());
        data.put("byRegion", List.of());
        data.put("byCompany", List.of());
        data.put("alerts", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    /**
     * 민수관리 탭용 데이터를 구성해 반환합니다.
     */
    public Map<String, Object> getPrivateOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rows", List.of());
        return wrap(data);
    }

    /**
     * 보고서 물품 목록 조회 (procurement_contract_summary 기반, 검색/페이징).
     */
    public List<Map<String, Object>> getReportGoodsList(int start, int length,
            String demandAgencyName, String demandAgencyRegion, String detailItemName, String contractMethod,
            String firstContractDate, Integer year, String month, String rangeStart, String rangeEnd, boolean showSavedOnly) {
        return procurementContractSummaryMapper.selectReportGoodsList(
                start, length, demandAgencyName, demandAgencyRegion, detailItemName, contractMethod,
                firstContractDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    /**
     * 보고서 물품 검색 결과 건수.
     */
    public int getReportGoodsCount(String demandAgencyName, String demandAgencyRegion, String detailItemName,
            String contractMethod, String firstContractDate, Integer year, String month, String rangeStart, String rangeEnd, boolean showSavedOnly) {
        return procurementContractSummaryMapper.selectReportGoodsCount(
                demandAgencyName, demandAgencyRegion, detailItemName, contractMethod,
                firstContractDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    /**
     * 보고서 물품 엑셀용 전체 목록 (페이징 없음).
     */
    public List<Map<String, Object>> getReportGoodsForExcel(String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractMethod, String firstContractDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {
        return procurementContractSummaryMapper.selectReportGoodsList(
                0, Integer.MAX_VALUE, demandAgencyName, demandAgencyRegion, detailItemName, contractMethod,
                firstContractDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    /**
     * 보고서 물품 저장 여부 업데이트.
     */
    public int updateReportGoodsSaved(String bidNoticeNo, String vendorBizRegNo, String saved) {
        return procurementContractSummaryMapper.updateSaved(bidNoticeNo, vendorBizRegNo, saved);
    }

    /**
     * 수요기관별 물품 조달시장 분석(대시보드) 집계.
     *
     * - dateBasis: FINAL(기본) / FIRST
     * - from/to: yyyy-mm-dd (필수)
     * - topN: 10(기본) / 20
     */
    public Map<String, Object> getDemandAgencyMarket(String dateBasis, String from, String to, Integer topN) {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("from은 필수입니다 (yyyy-mm-dd)");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("to는 필수입니다 (yyyy-mm-dd)");
        }

        // 날짜 파싱/검증 (ISO_LOCAL_DATE)
        final LocalDate fromDate;
        final LocalDate toDate;
        try {
            fromDate = LocalDate.parse(from.trim());
            toDate = LocalDate.parse(to.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("from/to 날짜 형식이 올바르지 않습니다 (yyyy-mm-dd)");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("from은 to보다 클 수 없습니다");
        }

        // dateBasis 보정 (FINAL/FIRST만 허용, 그 외 FINAL)
        final String basis = "FIRST".equalsIgnoreCase(dateBasis) ? "FIRST" : "FINAL";

        // topN 보정 (10/20만 허용)
        final int resolvedTopN = (topN != null && (topN == 10 || topN == 20)) ? topN : 10;

        List<Map<String, Object>> topSales;
        List<Map<String, Object>> topContractCount;
        List<Map<String, Object>> topAvgAmount;

        if ("FIRST".equals(basis)) {
            topSales = procurementContractSummaryMapper.selectDemandAgencyTopSalesByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN);
            topContractCount = procurementContractSummaryMapper.selectDemandAgencyTopContractCountByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN);
            topAvgAmount = procurementContractSummaryMapper.selectDemandAgencyTopAvgAmountByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN);
        } else {
            topSales = procurementContractSummaryMapper.selectDemandAgencyTopSalesByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN);
            topContractCount = procurementContractSummaryMapper.selectDemandAgencyTopContractCountByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN);
            topAvgAmount = procurementContractSummaryMapper.selectDemandAgencyTopAvgAmountByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN);
        }

        // rank(1..N) 부여
        addRank(topSales);
        addRank(topContractCount);
        addRank(topAvgAmount);

        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("dateBasis", basis);
        condition.put("from", fromDate.toString());
        condition.put("to", toDate.toString());
        condition.put("topN", resolvedTopN);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topSales", topSales);
        data.put("topContractCount", topContractCount);
        data.put("topAvgAmount", topAvgAmount);
        data.put("condition", condition);

        return wrap(data);
    }

    private void addRank(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            if (row != null) {
                row.put("rank", i + 1);
            }
        }
    }

    /**
     * 공통 응답 포맷으로 래핑합니다.
     */
    private Map<String, Object> wrap(Map<String, Object> data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}
