package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceContractMapper {

    // ===== Flat (펼쳐서 보기) — PK: (contract_delivery_integrated_no, vendor_biz_reg_no) =====

    List<Map<String, Object>> selectFlatList(
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("publicProcurementCategoryMid") String publicProcurementCategoryMid,
            @Param("publicProcurementCategory") String publicProcurementCategory,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectFlatCount(
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("publicProcurementCategoryMid") String publicProcurementCategoryMid,
            @Param("publicProcurementCategory") String publicProcurementCategory,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    void selectFlatListForExport(
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("publicProcurementCategoryMid") String publicProcurementCategoryMid,
            @Param("publicProcurementCategory") String publicProcurementCategory,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler
    );

    /**
     * saved 갱신 — flat PK: (contract_delivery_integrated_no, vendor_biz_reg_no)
     */
    int updateFlatSaved(
            @Param("contractDeliveryIntegratedNo") String contractDeliveryIntegratedNo,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("saved") String saved
    );

    // ===== Grouped (합쳐서 보기) — PK: (group_key, vendor_biz_reg_no) =====

    List<Map<String, Object>> selectGroupedList(
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectGroupedCount(
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    void selectGroupedListForExport(
            @Param("demandAgency") String demandAgency,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler
    );

    /**
     * saved 갱신 — grouped PK: (group_key, vendor_biz_reg_no)
     */
    int updateGroupedSaved(
            @Param("groupKey") String groupKey,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("saved") String saved
    );
}
