package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

@Mapper
public interface TopCompaniesReportMapper {

    List<Map<String, Object>> selectList(
            @Param("type")             String type,
            @Param("dminsttNm")        String dminsttNm,
            @Param("dminsttNmDetail")  String dminsttNmDetail,
            @Param("prdctClsfcNo")     String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate")  String firstCntrctDate,
            @Param("year")             Integer year,
            @Param("month")            String month,
            @Param("rangeStart")       String rangeStart,
            @Param("rangeEnd")         String rangeEnd,
            @Param("showSavedOnly")    boolean showSavedOnly,
            @Param("start")            int start,
            @Param("length")           int length
    );

    int selectCount(
            @Param("type")             String type,
            @Param("dminsttNm")        String dminsttNm,
            @Param("dminsttNmDetail")  String dminsttNmDetail,
            @Param("prdctClsfcNo")     String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate")  String firstCntrctDate,
            @Param("year")             Integer year,
            @Param("month")            String month,
            @Param("rangeStart")       String rangeStart,
            @Param("rangeEnd")         String rangeEnd,
            @Param("showSavedOnly")    boolean showSavedOnly
    );

    void selectForExport(
            @Param("type")             String type,
            @Param("dminsttNm")        String dminsttNm,
            @Param("dminsttNmDetail")  String dminsttNmDetail,
            @Param("prdctClsfcNo")     String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate")  String firstCntrctDate,
            @Param("year")             Integer year,
            @Param("month")            String month,
            @Param("rangeStart")       String rangeStart,
            @Param("rangeEnd")         String rangeEnd,
            @Param("showSavedOnly")    boolean showSavedOnly,
            ResultHandler<Map<String, Object>> handler
    );

    /** 쇼핑몰 saved 갱신 */
    int updateShoppingMallSaved(
            @Param("deliveryContractNo")        String deliveryContractNo,
            @Param("deliveryContractChangeSeq") Long deliveryContractChangeSeq,
            @Param("deliveryItemSeq")           Long deliveryItemSeq,
            @Param("saved")                     String saved
    );
}
