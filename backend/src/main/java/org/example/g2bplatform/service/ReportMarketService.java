package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.MarketContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportMarketService {

    private final MarketContractMapper marketContractMapper;

    public ReportMarketService(MarketContractMapper marketContractMapper) {
        this.marketContractMapper = marketContractMapper;
    }

    public String normalizeContractType(String contractType) {
        if ("construction".equals(contractType) || "service".equals(contractType)) {
            return contractType;
        }
        throw new IllegalArgumentException("contractType은 construction 또는 service 여야 합니다.");
    }

    public List<Map<String, Object>> getList(
            String contractType, boolean grouped, int start, int length,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractName, String contractMethod,
            String procurementWorkArea, String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        String normalizedType = normalizeContractType(contractType);
        if (grouped) {
            return marketContractMapper.selectGroupedList(
                    normalizedType, start, length, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractName, contractMethod, procurementWorkArea,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
        return marketContractMapper.selectFlatList(
                normalizedType, start, length, demandAgencyName, demandAgencyRegion,
                detailItemName, contractName, contractMethod, procurementWorkArea,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public int getCount(
            String contractType, boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractName, String contractMethod,
            String procurementWorkArea, String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        String normalizedType = normalizeContractType(contractType);
        if (grouped) {
            return marketContractMapper.selectGroupedCount(
                    normalizedType, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractName, contractMethod, procurementWorkArea,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
        return marketContractMapper.selectFlatCount(
                normalizedType, demandAgencyName, demandAgencyRegion,
                detailItemName, contractName, contractMethod, procurementWorkArea,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public Map<String, Object> getTotals(
            String contractType, boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractName, String contractMethod,
            String procurementWorkArea, String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        String normalizedType = normalizeContractType(contractType);
        if (grouped) {
            return marketContractMapper.selectGroupedTotals(
                    normalizedType, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractName, contractMethod, procurementWorkArea,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
        return marketContractMapper.selectFlatTotals(
                normalizedType, demandAgencyName, demandAgencyRegion,
                detailItemName, contractName, contractMethod, procurementWorkArea,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public void streamForExcel(
            String contractType, boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractName, String contractMethod,
            String procurementWorkArea, String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler) {

        String normalizedType = normalizeContractType(contractType);
        if (grouped) {
            marketContractMapper.selectGroupedListForExport(
                    normalizedType, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractName, contractMethod, procurementWorkArea,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
            return;
        }
        marketContractMapper.selectFlatListForExport(
                normalizedType, demandAgencyName, demandAgencyRegion,
                detailItemName, contractName, contractMethod, procurementWorkArea,
                publicProcurementCategoryMid, publicProcurementCategory,
                firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
    }

    /** 검색 필터 select 옵션 (입찰계약방법 / 조달업무영역) — contract_type별 distinct */
    public Map<String, Object> getFilterOptions(String contractType) {
        String normalizedType = normalizeContractType(contractType);
        return Map.of(
                "contractMethods", marketContractMapper.selectDistinctContractMethods(normalizedType),
                "workAreas", marketContractMapper.selectDistinctWorkAreas(normalizedType));
    }

    public int updateSaved(String contractType, boolean grouped, String key, String vendorBizRegNo, String saved) {
        String normalizedType = normalizeContractType(contractType);
        if (grouped) {
            return marketContractMapper.updateGroupedSaved(normalizedType, key, vendorBizRegNo, saved);
        }
        return marketContractMapper.updateFlatSaved(normalizedType, key, saved);
    }
}
