package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProcurementContractSummaryMapper {

    List<Map<String, Object>> selectReportGoodsList(
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("firstContractDate") String firstContractDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectReportGoodsCount(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("firstContractDate") String firstContractDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int updateSaved(
            @Param("bidNoticeNo") String bidNoticeNo,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("saved") String saved
    );
}
