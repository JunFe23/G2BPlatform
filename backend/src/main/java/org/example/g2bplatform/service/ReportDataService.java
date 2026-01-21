package org.example.g2bplatform.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportDataService {

    public Map<String, Object> getMarketOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contractCards", List.of());
        data.put("summaryStats", List.of());
        data.put("revenueBars", List.of());
        data.put("countBars", List.of());
        data.put("detailItems", List.of());
        return wrap(data);
    }

    public Map<String, Object> getAgencyOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topSales", List.of());
        data.put("countBars", List.of());
        data.put("avgBars", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    public Map<String, Object> getRegionOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stackedBars", List.of());
        data.put("pieLabels", List.of());
        data.put("countBars", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    public Map<String, Object> getRankOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tabs", List.of());
        data.put("topItems", List.of());
        data.put("summaryRows", List.of());
        return wrap(data);
    }

    public Map<String, Object> getExcellentOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summaryCards", List.of());
        data.put("byRegion", List.of());
        data.put("byCompany", List.of());
        data.put("alerts", List.of());
        data.put("detailRows", List.of());
        return wrap(data);
    }

    public Map<String, Object> getPrivateOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rows", List.of());
        return wrap(data);
    }

    private Map<String, Object> wrap(Map<String, Object> data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}
