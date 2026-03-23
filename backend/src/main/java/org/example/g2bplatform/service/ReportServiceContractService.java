package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.ServiceContractMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportServiceContractService {

    private final ServiceContractMapper serviceContractMapper;

    public ReportServiceContractService(ServiceContractMapper serviceContractMapper) {
        this.serviceContractMapper = serviceContractMapper;
    }

    public List<Map<String, Object>> getList(
            boolean grouped, int start, int length,
            String demandAgency, String demandAgencyRegion,
            String detailItemName, String procurementWorkArea, String contractMethod,
            String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly
    ) {
        if (grouped) {
            return serviceContractMapper.selectGroupedList(
                    start, length, demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return serviceContractMapper.selectFlatList(
                    start, length, demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    public int getCount(
            boolean grouped,
            String demandAgency, String demandAgencyRegion,
            String detailItemName, String procurementWorkArea, String contractMethod,
            String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly
    ) {
        if (grouped) {
            return serviceContractMapper.selectGroupedCount(
                    demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        } else {
            return serviceContractMapper.selectFlatCount(
                    demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly);
        }
    }

    public void streamForExcel(
            boolean grouped,
            String demandAgency, String demandAgencyRegion,
            String detailItemName, String procurementWorkArea, String contractMethod,
            String publicProcurementCategoryMid, String publicProcurementCategory,
            String firstCntrctDate, Integer year, String month,
            String rangeStart, String rangeEnd, boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler
    ) {
        if (grouped) {
            serviceContractMapper.selectGroupedListForExport(
                    demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                    handler);
        } else {
            serviceContractMapper.selectFlatListForExport(
                    demandAgency, demandAgencyRegion,
                    detailItemName, procurementWorkArea, contractMethod,
                    publicProcurementCategoryMid, publicProcurementCategory,
                    firstCntrctDate, year, month, rangeStart, rangeEnd, showSavedOnly,
                    handler);
        }
    }

    public int updateGroupedSaved(String groupKey, String vendorBizRegNo, String saved) {
        return serviceContractMapper.updateGroupedSaved(groupKey, vendorBizRegNo, saved);
    }

    public int updateFlatSaved(String contractDeliveryIntegratedNo, String vendorBizRegNo, String saved) {
        return serviceContractMapper.updateFlatSaved(contractDeliveryIntegratedNo, vendorBizRegNo, saved);
    }
}
