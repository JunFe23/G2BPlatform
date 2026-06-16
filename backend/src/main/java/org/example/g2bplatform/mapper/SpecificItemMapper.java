package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpecificItemMapper {

    List<Map<String, Object>> selectFlatList(@Param("p") Map<String, Object> params);

    int selectFlatCount(@Param("p") Map<String, Object> params);

    List<Map<String, Object>> selectGroupedList(@Param("p") Map<String, Object> params);

    int selectGroupedCount(@Param("p") Map<String, Object> params);

    void updateSaved(Map<String, Object> params);

    void updateGroupedSaved(Map<String, Object> params);
}
