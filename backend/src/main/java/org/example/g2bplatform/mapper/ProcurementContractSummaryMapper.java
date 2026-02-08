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

    /** 엑셀용 Keyset 페이지네이션 - LIMIT offset 대비 대용량 시 O(1) 유지 */
    List<Map<String, Object>> selectReportGoodsListKeyset(
            @Param("length") int length,
            @Param("lastFirstContractDate") String lastFirstContractDate,
            @Param("lastBidNoticeNo") String lastBidNoticeNo,
            @Param("lastVendorBizRegNo") String lastVendorBizRegNo,
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

    // ===== 수요기관별 물품 조달시장 분석(대시보드) =====
    List<Map<String, Object>> selectDemandAgencyTopSalesByFinalDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    List<Map<String, Object>> selectDemandAgencyTopSalesByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    List<Map<String, Object>> selectDemandAgencyTopContractCountByFinalDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    List<Map<String, Object>> selectDemandAgencyTopContractCountByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    List<Map<String, Object>> selectDemandAgencyTopAvgAmountByFinalDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    List<Map<String, Object>> selectDemandAgencyTopAvgAmountByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN
    );

    /** 지역별 물품 조달시장 분석: first_contract_date 기간 필터, final_contract_amount 집계 */
    List<Map<String, Object>> selectRegionMarketByFirstDate(
            @Param("from") String from,
            @Param("to") String to
    );
}
