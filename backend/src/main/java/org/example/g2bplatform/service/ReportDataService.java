package org.example.g2bplatform.service;

import org.example.g2bplatform.mapper.ProcurementContractSummaryMapper;
import org.springframework.stereotype.Service;

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
     * 공통 응답 포맷으로 래핑합니다.
     */
    private Map<String, Object> wrap(Map<String, Object> data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}
