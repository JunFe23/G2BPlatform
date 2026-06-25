package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcurementContractMapper {

    // 조회/엑셀 쿼리는 G2B-36에서 제거(시장데이터 물품 페이지 폐지). saved 갱신만 유지 — TopContractsReportView 공유.

    /**
     * saved 갱신 — flat PK: (contract_no, item_seq)
     */
    int updateFlatSaved(
            @Param("contractNo") String contractNo,
            @Param("itemSeq") Long itemSeq,
            @Param("saved") String saved
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
