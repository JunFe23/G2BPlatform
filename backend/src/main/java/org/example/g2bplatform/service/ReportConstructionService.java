package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.ConstructionContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportConstructionService {

    private final ConstructionContractMapper constructionContractMapper;

    public ReportConstructionService(ConstructionContractMapper constructionContractMapper) {
        this.constructionContractMapper = constructionContractMapper;
    }

    /**
     * 공사 계약 목록 조회.
     * grouped=true → construction_contract_grouped, 기간 필터: initial_contract_date
     * grouped=false → construction_contract_flat,   기간 필터: contract_date
     */
    public List<Map<String, Object>> getList(
            boolean grouped, int start, int length,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        if (grouped) {
            return constructionContractMapper.selectGroupedList(
                    start, length, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return constructionContractMapper.selectFlatList(
                    start, length, demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    /**
     * 공사 계약 건수 조회.
     */
    public int getCount(
            boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        if (grouped) {
            return constructionContractMapper.selectGroupedCount(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return constructionContractMapper.selectFlatCount(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    /**
     * 엑셀 스트리밍 (페이징 없이 fetchSize 단위로 행 전달).
     */
    public void streamForExcel(
            boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler) {

        if (grouped) {
            constructionContractMapper.selectGroupedListForExport(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
        } else {
            constructionContractMapper.selectFlatListForExport(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
        }
    }

    /**
     * saved 체크박스 갱신.
     * grouped=true → group_key 기준 UPDATE (construction_contract_grouped)
     * grouped=false → contract_no 기준 UPDATE (construction_contract_flat)
     */
    public int updateSaved(boolean grouped, String key, String saved) {
        if (grouped) {
            return constructionContractMapper.updateGroupedSaved(key, saved);
        } else {
            return constructionContractMapper.updateFlatSaved(key, saved);
        }
    }
}
