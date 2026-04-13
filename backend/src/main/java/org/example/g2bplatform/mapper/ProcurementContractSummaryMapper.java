package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

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

    /** 엑셀용 스트리밍: 동일 조건, LIMIT 없음, fetchSize로 행 단위 전달. idx_pcs_order 사용 권장. */
    void selectReportGoodsListForExport(
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("detailItemName") String detailItemName,
            @Param("contractMethod") String contractMethod,
            @Param("firstContractDate") String firstContractDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler
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
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    List<Map<String, Object>> selectDemandAgencyTopSalesByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    List<Map<String, Object>> selectDemandAgencyTopContractCountByFinalDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    List<Map<String, Object>> selectDemandAgencyTopContractCountByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    List<Map<String, Object>> selectDemandAgencyTopAvgAmountByFinalDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    List<Map<String, Object>> selectDemandAgencyTopAvgAmountByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("topN") int topN,
            @Param("dataSource") String dataSource
    );

    /** 지역별 물품 조달시장 분석: first_contract_date 기간 필터, final_contract_amount 집계 */
    List<Map<String, Object>> selectRegionMarketByFirstDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("dataSource") String dataSource
    );

    // ===== 시장현황 탭: 소스별 집계 =====

    /** 시장현황: 물품 집계 (procurement_contract_summary) */
    Map<String, Object> selectMarketSourceProcurement(
            @Param("from") String from,
            @Param("to") String to
    );

    /** 시장현황: 물품 우수제품/일반제품 집계 (procurement_contract_flat) */
    List<Map<String, Object>> selectMarketExcellentProcurement(
            @Param("from") String from,
            @Param("to") String to
    );

    /** 시장현황: 3자단가 집계 (shopping_mall_summary) */
    Map<String, Object> selectMarketSourceShoppingMall(
            @Param("from") String from,
            @Param("to") String to
    );

    /** 시장현황: 용역 집계 (service_contract_grouped) */
    Map<String, Object> selectMarketSourceService(
            @Param("from") String from,
            @Param("to") String to
    );

    /** 시장현황: 공사 집계 (construction_contract_grouped) */
    Map<String, Object> selectMarketSourceConstruction(
            @Param("from") String from,
            @Param("to") String to
    );
}
