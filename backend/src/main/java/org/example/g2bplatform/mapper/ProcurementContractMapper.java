package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProcurementContractMapper {

    // ===== Flat (펼쳐서 보기) — PK: (contract_no, item_seq) =====

    List<Map<String, Object>> selectFlatList(
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectFlatCount(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    void selectFlatListForExport(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
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
     * saved 갱신 — flat PK: (contract_no, item_seq)
     */
    int updateFlatSaved(
            @Param("contractNo") String contractNo,
            @Param("itemSeq") Long itemSeq,
            @Param("saved") String saved
    );

    // ===== Grouped (합쳐서 보기) — PK: (bid_notice_no, vendor_biz_reg_no, contract_no) =====

    List<Map<String, Object>> selectGroupedList(
            @Param("start") int start,
            @Param("length") int length,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    int selectGroupedCount(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("contractMethod") String contractMethod,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly
    );

    void selectGroupedListForExport(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("itemCategoryNo") String itemCategoryNo,
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
     * saved 갱신 — grouped PK: (bid_notice_no, vendor_biz_reg_no, contract_no)
     */
    int updateGroupedSaved(
            @Param("bidNoticeNo") String bidNoticeNo,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("contractNo") String contractNo,
            @Param("saved") String saved
    );
}
