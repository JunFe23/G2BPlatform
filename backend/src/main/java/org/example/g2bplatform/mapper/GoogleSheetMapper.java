package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoogleSheetMapper {
    List<Map<String, String>> getTopsDataForSheet();
}
