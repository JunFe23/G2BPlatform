package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;
import org.example.g2bplatform.entity.TopManualContract;

import java.util.List;
import java.util.Map;

/**
 * 탑 수주 현황(2社) 통합 조회 — specific_item_flat/grouped ∪ market_contract_flat/grouped.
 * 파라미터는 단일 Map(키: type, dataOrigin, dminsttNm, dminsttNmDetail, contractName,
 * categoryNames(CSV), cntctCnclsMthdNm, firstCntrctDate, year, month, rangeStart, rangeEnd,
 * showSavedOnly, start, length)로 전달 — MyBatis가 #{키}로 바인딩.
 */
@Mapper
public interface TopCompaniesReportMapper {

    List<Map<String, Object>> selectFlatList(Map<String, Object> p);
    int selectFlatCount(Map<String, Object> p);
    void selectFlatExport(Map<String, Object> p, ResultHandler<Map<String, Object>> handler);
    Map<String, Object> selectFlatTotals(Map<String, Object> p);

    List<Map<String, Object>> selectGroupedList(Map<String, Object> p);
    int selectGroupedCount(Map<String, Object> p);
    void selectGroupedExport(Map<String, Object> p, ResultHandler<Map<String, Object>> handler);
    Map<String, Object> selectGroupedTotals(Map<String, Object> p);

    // 통합 분류 계층(중→품명) / 입찰계약방법 distinct (2社 데이터 기준)
    List<Map<String, Object>> selectCategoryHierarchy();
    List<String> selectDistinctContractMethods();

    // 민수(직접입력) 데이터 CRUD (관리자)
    List<TopManualContract> selectManualList();
    TopManualContract selectManualById(@Param("id") Long id);
    int insertManual(TopManualContract entity);
    int updateManual(TopManualContract entity);
    int updateManualSaved(@Param("id") Long id, @Param("saved") String saved);
    int deleteManual(@Param("id") Long id);
}
