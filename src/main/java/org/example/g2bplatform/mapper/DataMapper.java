package org.example.g2bplatform.mapper;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataMapper {
    /**
     *
     * @param start
     * @param length
     * @param dminsttNm
     * @param dminsttNmDetail
     * @param prdctClsfcNo
     * @param cntctCnclsMthdNm
     * @param firstCntrctDate
     * @return
     */
    List<Map<String, String>> getThingsData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );

    /**
     * 전체 데이터 개수를 반환
     *
     * @param category 데이터 카테고리
     * @return 총 데이터 개수
     */
    int getThingsTotalCount(@Param("category") String category);

    /**
     * 검색 조건에 맞는 데이터 개수를 반환
     * @param dminsttNm
     * @param dminsttNmDetail
     * @param prdctClsfcNo
     * @param cntctCnclsMthdNm
     * @param firstCntrctDate
     * @return
     */
    int getThingsFilteredCount(
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );

    List<Map<String, String>> getServicesData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );

    int getServicesTotalCount(@Param("category") String category);

    int getServicesFilteredCount(
            @Param("start") int start,
            @Param("length") int length,
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );

    List<Map<String, String>> getConstructionsData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );

    int getConstructionsTotalCount(@Param("category") String category);

    int getConstructionsFilteredCount(
            @Param("start") int start,
            @Param("length") int length,
            @Param("dminsttNm") String dminsttNm,
            @Param("dminsttNmDetail") String dminsttNmDetail,
            @Param("prdctClsfcNo") String prdctClsfcNo,
            @Param("cntctCnclsMthdNm") String cntctCnclsMthdNm,
            @Param("firstCntrctDate") String firstCntrctDate,
            @Param("year") Integer year,
            @Param("month") String month,
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd,
            @Param("showSavedOnly") int showSavedOnly
    );


    List<Map<String, String>> getTopsData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("search") String search,
            @Param("category") String category
    );

    int getTopsTotalCount(@Param("category") String category);

    int getTopsFilteredCount(
            @Param("search") String search,
            @Param("category") String category
    );

    int updateCheckedThings(@Param("id") int id, @Param("checked") int checked);
    int updateCheckedServices(@Param("id") int id, @Param("checked") int checked);
    int updateCheckedConstructions(@Param("id") int id, @Param("checked") int checked);
}
