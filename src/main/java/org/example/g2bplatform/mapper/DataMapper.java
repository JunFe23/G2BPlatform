package org.example.g2bplatform.mapper;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataMapper {
    /**
     * 페이징 및 검색 조건에 맞는 데이터를 조회
     *
     * @param start    시작 인덱스 (OFFSET)
     * @param length   가져올 데이터의 개수 (LIMIT)
     * @param search   검색어 (null 허용)
     * @param category 데이터 카테고리
     * @return 조회된 데이터 목록
     */
    List<Map<String, String>> getThingsData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("search") String search,
            @Param("category") String category
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
     *
     * @param search   검색어 (null 허용)
     * @param category 데이터 카테고리
     * @return 검색된 데이터 개수
     */
    int getThingsFilteredCount(
            @Param("search") String search,
            @Param("category") String category
    );

    List<Map<String, String>> getServicesData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("search") String search,
            @Param("category") String category
    );

    int getServicesTotalCount(@Param("category") String category);

    int getServicesFilteredCount(
            @Param("search") String search,
            @Param("category") String category
    );

    List<Map<String, String>> getConstructionsData(
            @Param("start") int start,
            @Param("length") int length,
            @Param("search") String search,
            @Param("category") String category
    );

    int getConstructionsTotalCount(@Param("category") String category);

    int getConstructionsFilteredCount(
            @Param("search") String search,
            @Param("category") String category
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
}
