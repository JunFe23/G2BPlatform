package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.ProcurementContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportProcurementService {

    private final ProcurementContractMapper procurementContractMapper;

    public ReportProcurementService(ProcurementContractMapper procurementContractMapper) {
        this.procurementContractMapper = procurementContractMapper;
    }

    /**
     * 물품 계약 목록 조회.
     * grouped=true  → procurement_contract_grouped, 기간 필터: initial_contract_date
     * grouped=false → procurement_contract_flat,    기간 필터: contract_date
     */
    public List<Map<String, Object>> getList(
            boolean grouped, int start, int length,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String itemCategoryNo, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        if (grouped) {
            return procurementContractMapper.selectGroupedList(
                    start, length, demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return procurementContractMapper.selectFlatList(
                    start, length, demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    /**
     * 물품 계약 건수 조회.
     */
    public int getCount(
            boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String itemCategoryNo, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly) {

        if (grouped) {
            return procurementContractMapper.selectGroupedCount(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return procurementContractMapper.selectFlatCount(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    /**
     * 엑셀 스트리밍 (페이징 없이 fetchSize 단위로 행 전달).
     */
    public void streamForExcel(
            boolean grouped,
            String demandAgencyName, String demandAgencyRegion,
            String detailItemName, String itemCategoryNo, String contractMethod,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler) {

        if (grouped) {
            procurementContractMapper.selectGroupedListForExport(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
        } else {
            procurementContractMapper.selectFlatListForExport(
                    demandAgencyName, demandAgencyRegion,
                    detailItemName, itemCategoryNo, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly, handler);
        }
    }

    /**
     * saved 체크박스 갱신.
     * grouped=true  → (bid_notice_no, vendor_biz_reg_no, contract_no) 3-PK UPDATE
     * grouped=false → (contract_no, item_seq) 2-PK UPDATE
     */
    public int updateGroupedSaved(String bidNoticeNo, String vendorBizRegNo, String contractNo, String saved) {
        return procurementContractMapper.updateGroupedSaved(bidNoticeNo, vendorBizRegNo, contractNo, saved);
    }

    public int updateFlatSaved(String contractNo, Long itemSeq, String saved) {
        return procurementContractMapper.updateFlatSaved(contractNo, itemSeq, saved);
    }
}
