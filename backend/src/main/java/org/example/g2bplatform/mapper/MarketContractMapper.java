package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

@Mapper
public interface MarketContractMapper {

    List<Map<String, Object>> selectFlatList(
            @Param("contractType") String contractType,
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
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
            @Param("contractType") String contractType,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
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
            @Param("contractType") String contractType,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
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

    int updateFlatSaved(
            @Param("contractType") String contractType,
            @Param("contractNo") String contractNo,
            @Param("saved") String saved
    );

    List<Map<String, Object>> selectGroupedList(
            @Param("contractType") String contractType,
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("publicProcurementCategoryMid") String publicProcurementCategoryMid,
            @Param("publicProcurementCategory") String publicProcurementCategory,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectGroupedCount(
            @Param("contractType") String contractType,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
            @Param("publicProcurementCategoryMid") String publicProcurementCategoryMid,
            @Param("publicProcurementCategory") String publicProcurementCategory,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    void selectGroupedListForExport(
            @Param("contractType") String contractType,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("procurementWorkArea") String procurementWorkArea,
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

    int updateGroupedSaved(
            @Param("contractType") String contractType,
            @Param("groupKey") String groupKey,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("saved") String saved
    );
}
