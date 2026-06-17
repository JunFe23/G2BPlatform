package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpecificItemMapper {

    List<Map<String, Object>> selectFlatList(@Param("p") Map<String, Object> params);

    int selectFlatCount(@Param("p") Map<String, Object> params);

    /** 엑셀 export 전용 — 전체 행을 스트리밍으로 반환 (열린 SqlSession 내에서 순회) */
    Cursor<Map<String, Object>> selectFlatListExport(@Param("p") Map<String, Object> params);

    List<Map<String, Object>> selectGroupedList(@Param("p") Map<String, Object> params);

    int selectGroupedCount(@Param("p") Map<String, Object> params);

    /** 묶어서 보기 상단 합계 (최초/최종 계약금액 합계) */
    Map<String, Object> selectGroupedTotals(@Param("p") Map<String, Object> params);

    /** 엑셀 export 전용 — 전체 행을 스트리밍으로 반환 */
    Cursor<Map<String, Object>> selectGroupedListExport(@Param("p") Map<String, Object> params);

    /** 자사 우수제품 기준 물품분류번호 목록 (설정 테이블) */
    List<String> selectTopCategories();

    void updateSaved(Map<String, Object> params);

    void updateGroupedSaved(Map<String, Object> params);
}
