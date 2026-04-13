package org.example.g2bplatform.service;

import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.mapper.TopCompaniesReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TopCompaniesReportService {

    private final TopCompaniesReportMapper mapper;

    public TopCompaniesReportService(TopCompaniesReportMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> getList(
            String type, String dminsttNm, String dminsttNmDetail,
            String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate,
            Integer year, String month, String rangeStart, String rangeEnd,
            boolean showSavedOnly, int start, int length) {
        return mapper.selectList(type, dminsttNm, dminsttNmDetail,
                prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate,
                year, month, rangeStart, rangeEnd, showSavedOnly, start, length);
    }

    public int getCount(
            String type, String dminsttNm, String dminsttNmDetail,
            String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate,
            Integer year, String month, String rangeStart, String rangeEnd,
            boolean showSavedOnly) {
        return mapper.selectCount(type, dminsttNm, dminsttNmDetail,
                prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate,
                year, month, rangeStart, rangeEnd, showSavedOnly);
    }

    public void streamForExcel(
            String type, String dminsttNm, String dminsttNmDetail,
            String prdctClsfcNo, String cntctCnclsMthdNm, String firstCntrctDate,
            Integer year, String month, String rangeStart, String rangeEnd,
            boolean showSavedOnly, ResultHandler<Map<String, Object>> handler) {
        mapper.selectForExport(type, dminsttNm, dminsttNmDetail,
                prdctClsfcNo, cntctCnclsMthdNm, firstCntrctDate,
                year, month, rangeStart, rangeEnd, showSavedOnly, handler);
    }

    public int updateShoppingMallSaved(
            String deliveryContractNo, Long deliveryContractChangeSeq,
            Long deliveryItemSeq, String saved) {
        return mapper.updateShoppingMallSaved(
                deliveryContractNo, deliveryContractChangeSeq, deliveryItemSeq, saved);
    }
}
