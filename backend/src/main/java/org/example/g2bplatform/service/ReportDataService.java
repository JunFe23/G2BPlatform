package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.ProcurementContractSummaryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportDataService {

    private final ProcurementContractSummaryMapper procurementContractSummaryMapper;

    public ReportDataService(ProcurementContractSummaryMapper procurementContractSummaryMapper) {
        this.procurementContractSummaryMapper = procurementContractSummaryMapper;
    }

    /**
     * 시장현황 탭 집계.
     *
     * @param from    yyyy-MM-dd (필수)
     * @param to      yyyy-MM-dd (필수)
     * @param sources 쉼표 구분 소스 목록 (procurement, shopping_mall, service, construction)
     */
    public Map<String, Object> getMarketOverview(String from, String to, String sources) {
        if (from == null || from.isBlank()) throw new IllegalArgumentException("from은 필수입니다 (yyyy-MM-dd)");
        if (to == null || to.isBlank())     throw new IllegalArgumentException("to는 필수입니다 (yyyy-MM-dd)");
        try {
            LocalDate f = LocalDate.parse(from.trim());
            LocalDate t = LocalDate.parse(to.trim());
            if (f.isAfter(t)) throw new IllegalArgumentException("from은 to보다 클 수 없습니다");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식 오류 (yyyy-MM-dd)");
        }

        final String fromNorm = from.trim();
        final String toNorm   = to.trim();

        Set<String> validSources = Set.of("procurement", "shopping_mall", "service", "construction");
        Set<String> sourceSet = new HashSet<>();
        if (sources != null && !sources.isBlank()) {
            for (String s : sources.split(",")) {
                String trimmed = s.trim().toLowerCase();
                if (validSources.contains(trimmed)) {
                    sourceSet.add(trimmed);
                }
            }
        }
        if (sourceSet.isEmpty()) {
            sourceSet = new HashSet<>(validSources);
        }

        Map<String, Object> perSource = new LinkedHashMap<>();

        if (sourceSet.contains("procurement")) {
            Map<String, Object> row = procurementContractSummaryMapper.selectMarketSourceProcurement(fromNorm, toNorm);
            List<Map<String, Object>> excellent = procurementContractSummaryMapper.selectMarketExcellentProcurement(fromNorm, toNorm);
            long excellentAmt = 0L, generalAmt = 0L;
            for (Map<String, Object> r : excellent) {
                String flag = String.valueOf(r.getOrDefault("excellentFlag", ""));
                long amt = toLong(r.get("amount"));
                if ("Y".equalsIgnoreCase(flag)) excellentAmt += amt;
                else generalAmt += amt;
            }
            if (row != null) {
                row.put("excellentAmount", excellentAmt);
                row.put("generalAmount", generalAmt);
            }
            perSource.put("procurement", row);
        }

        if (sourceSet.contains("shopping_mall")) {
            perSource.put("shopping_mall", procurementContractSummaryMapper.selectMarketSourceShoppingMall(fromNorm, toNorm));
        }

        if (sourceSet.contains("service")) {
            Map<String, Object> row = procurementContractSummaryMapper.selectMarketSourceService(fromNorm, toNorm);
            if (row != null) { row.put("excellentAmount", 0L); row.put("generalAmount", 0L); }
            perSource.put("service", row);
        }

        if (sourceSet.contains("construction")) {
            Map<String, Object> row = procurementContractSummaryMapper.selectMarketSourceConstruction(fromNorm, toNorm);
            if (row != null) { row.put("excellentAmount", 0L); row.put("generalAmount", 0L); }
            perSource.put("construction", row);
        }

        List<String> resolvedSources = new ArrayList<>(sourceSet);
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("from", fromNorm);
        condition.put("to", toNorm);
        condition.put("sources", resolvedSources);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("perSource", perSource);
        data.put("condition", condition);

        return wrap(data);
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
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
    public Map<String, Object> getRankOverview(
            String dateBasis, String from, String to,
            Integer topN, String dataSource, String rankType) {

        final String basis     = "FIRST".equalsIgnoreCase(dateBasis) ? "FIRST" : "FINAL";
        final int resolvedTopN = (topN != null && topN > 0 && topN <= 100) ? topN : 10;
        final String ds        = normalizeDataSource(dataSource);
        final String rt        = normalizeRankType(rankType);
        final String[] dates   = resolveFromTo(from, to);

        List<Map<String, Object>> rows = queryVendorRankRows(basis, dates[0], dates[1], resolvedTopN, ds, rt);

        List<Map<String, Object>> tabs = new ArrayList<>();
        for (String[] t : new String[][]{
                {"AMOUNT", "계약금액 순위"},
                {"COUNT",  "계약건수 순위"},
                {"AVG",    "평균단가 순위"}}) {
            Map<String, Object> tab = new LinkedHashMap<>();
            tab.put("key",    t[0]);
            tab.put("label",  t[1]);
            tab.put("active", t[0].equals(rt));
            tabs.add(tab);
        }

        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("dateBasis",  basis);
        condition.put("from",       dates[0]);
        condition.put("to",         dates[1]);
        condition.put("topN",       resolvedTopN);
        condition.put("dataSource", ds);
        condition.put("rankType",   rt);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tabs",        tabs);
        data.put("topItems",    rows);
        data.put("summaryRows", rows);
        data.put("condition",   condition);

        return wrap(data);
    }

    /**
     * 순위분석 엑셀 다운로드용 전체 rows를 반환합니다. (LIMIT 없음 – topN=10000)
     */
    public List<Map<String, Object>> getRankForExcel(
            String dateBasis, String from, String to, String dataSource, String rankType) {

        final String basis   = "FIRST".equalsIgnoreCase(dateBasis) ? "FIRST" : "FINAL";
        final String ds      = normalizeDataSource(dataSource);
        final String rt      = normalizeRankType(rankType);
        final String[] dates = resolveFromTo(from, to);

        return queryVendorRankRows(basis, dates[0], dates[1], 10_000, ds, rt);
    }

    /**
     * 공통: DB 조회 + 순위/점유율 계산.
     */
    private List<Map<String, Object>> queryVendorRankRows(
            String basis, String fromStr, String toStr, int topN, String ds, String rt) {

        List<Map<String, Object>> rows;
        if ("FIRST".equals(basis)) {
            rows = procurementContractSummaryMapper.selectVendorRankByFirstDate(fromStr, toStr, topN, ds, rt);
        } else {
            rows = procurementContractSummaryMapper.selectVendorRankByFinalDate(fromStr, toStr, topN, ds, rt);
        }
        if (rows == null) rows = new ArrayList<>();

        long totalSalesAmount   = 0L;
        long totalContractCount = 0L;
        for (Map<String, Object> row : rows) {
            totalSalesAmount   += toLong(row.get("salesAmount"));
            totalContractCount += toLong(row.get("contractCount"));
        }

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            row.put("rank", i + 1);
            double amountShare = totalSalesAmount > 0
                    ? Math.round(toLong(row.get("salesAmount")) * 1000.0 / totalSalesAmount) / 10.0
                    : 0.0;
            double countShare = totalContractCount > 0
                    ? Math.round(toLong(row.get("contractCount")) * 1000.0 / totalContractCount) / 10.0
                    : 0.0;
            row.put("amountShareRate", amountShare);
            row.put("countShareRate",  countShare);
        }
        return rows;
    }

    /** from/to 날짜 유효성 검사 후 [fromStr, toStr] 반환. null/blank 이면 당해년도 전체. */
    private static String[] resolveFromTo(String from, String to) {
        if (from == null || from.isBlank()) {
            int year = LocalDate.now().getYear();
            return new String[]{year + "-01-01", year + "-12-31"};
        }
        try {
            LocalDate fromDate = LocalDate.parse(from.trim());
            LocalDate toDate   = (to != null && !to.isBlank()) ? LocalDate.parse(to.trim()) : fromDate;
            if (fromDate.isAfter(toDate)) throw new IllegalArgumentException("from은 to보다 클 수 없습니다");
            return new String[]{fromDate.toString(), toDate.toString()};
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식 오류 (yyyy-MM-dd)");
        }
    }

    private static String normalizeRankType(String rankType) {
        if (rankType == null || rankType.isBlank()) return "AMOUNT";
        switch (rankType.trim().toUpperCase()) {
            case "COUNT": return "COUNT";
            case "AVG":   return "AVG";
            default:      return "AMOUNT";
        }
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
     * 엑셀용 스트리밍: 한 번의 쿼리로 행 단위 전달(fetchSize). idx_pcs_order 사용.
     */
    public void streamReportGoodsForExcel(String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractMethod, String firstContractDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly, ResultHandler<Map<String, Object>> handler) {
        procurementContractSummaryMapper.selectReportGoodsListForExport(
                demandAgencyName, demandAgencyRegion, detailItemName, contractMethod,
                firstContractDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
    }

    /**
     * 엑셀용 Keyset 페이지네이션 - LIMIT offset 대비 대용량 시에도 O(1) 유지.
     */
    public List<Map<String, Object>> getReportGoodsListKeyset(int length,
            String lastFirstContractDate, String lastBidNoticeNo, String lastVendorBizRegNo,
            String demandAgencyName, String demandAgencyRegion, String detailItemName, String contractMethod,
            String firstContractDate, Integer year, String month, String rangeStart, String rangeEnd, boolean showSavedOnly) {
        return procurementContractSummaryMapper.selectReportGoodsListKeyset(
                length, lastFirstContractDate, lastBidNoticeNo, lastVendorBizRegNo,
                demandAgencyName, demandAgencyRegion, detailItemName, contractMethod,
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
     * - dataSource: procurement(기본) / shopping_mall / all
     */
    public Map<String, Object> getDemandAgencyMarket(String dateBasis, String from, String to, Integer topN, String dataSource) {
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

        final String ds = normalizeDataSource(dataSource);

        List<Map<String, Object>> topSales;
        List<Map<String, Object>> topContractCount;
        List<Map<String, Object>> topAvgAmount;

        if ("FIRST".equals(basis)) {
            topSales = procurementContractSummaryMapper.selectDemandAgencyTopSalesByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
            topContractCount = procurementContractSummaryMapper.selectDemandAgencyTopContractCountByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
            topAvgAmount = procurementContractSummaryMapper.selectDemandAgencyTopAvgAmountByFirstDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
        } else {
            topSales = procurementContractSummaryMapper.selectDemandAgencyTopSalesByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
            topContractCount = procurementContractSummaryMapper.selectDemandAgencyTopContractCountByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
            topAvgAmount = procurementContractSummaryMapper.selectDemandAgencyTopAvgAmountByFinalDate(fromDate.toString(), toDate.toString(), resolvedTopN, ds);
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
        condition.put("dataSource", ds);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topSales", topSales);
        data.put("topContractCount", topContractCount);
        data.put("topAvgAmount", topAvgAmount);
        data.put("condition", condition);

        return wrap(data);
    }

    /**
     * 지역별 물품 조달시장 분석(대시보드).
     * - first_contract_date로 기간 필터
     * - final_contract_amount 기준 집계
     */
    public Map<String, Object> getRegionMarket(String from, String to, String dataSource) {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("from은 필수입니다 (yyyy-mm-dd)");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("to는 필수입니다 (yyyy-mm-dd)");
        }
        try {
            LocalDate fromDate = LocalDate.parse(from.trim());
            LocalDate toDate = LocalDate.parse(to.trim());
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("from은 to보다 클 수 없습니다");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("from/to 날짜 형식이 올바르지 않습니다 (yyyy-mm-dd)");
        }

        final String ds = normalizeDataSource(dataSource);
        List<Map<String, Object>> rows = procurementContractSummaryMapper.selectRegionMarketByFirstDate(from, to, ds);
        addRank(rows);

        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("from", from);
        condition.put("to", to);
        condition.put("dataSource", ds);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("regions", rows);
        data.put("condition", condition);

        return wrap(data);
    }

    /** procurement(기본) | shopping_mall | all */
    /** procurement(기본) | shopping_mall | all | service | construction */
    private static String normalizeDataSource(String dataSource) {
        if (dataSource == null || dataSource.isBlank()) {
            return "procurement";
        }
        String s = dataSource.trim().toLowerCase();
        if ("shopping_mall".equals(s) || "shoppingmall".equals(s)) {
            return "shopping_mall";
        }
        if ("all".equals(s)) {
            return "all";
        }
        if ("service".equals(s)) {
            return "service";
        }
        if ("construction".equals(s)) {
            return "construction";
        }
        return "procurement";
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
