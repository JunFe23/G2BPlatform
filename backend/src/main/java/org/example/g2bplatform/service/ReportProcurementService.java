package org.example.g2bplatform.service;

import org.example.g2bplatform.mapper.ProcurementContractMapper;
import org.springframework.stereotype.Service;

@Service
public class ReportProcurementService {

    private final ProcurementContractMapper procurementContractMapper;

    public ReportProcurementService(ProcurementContractMapper procurementContractMapper) {
        this.procurementContractMapper = procurementContractMapper;
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
