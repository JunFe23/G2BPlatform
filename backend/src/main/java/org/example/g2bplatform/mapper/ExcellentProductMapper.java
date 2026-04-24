package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExcellentProductMapper {

    /** 요약 집계 (탑그룹 제품 수 / 경쟁사 수 / 만료임박 수) */
    Map<String, Object> selectExcellentSummary();

    /** 탑그룹(탑인더스트리/탑정보통신) 우수제품 목록 */
    List<Map<String, Object>> selectOwnProducts();

    /** 탑그룹 보유 제품과 동일 제품코드를 가진 경쟁사 목록 */
    List<Map<String, Object>> selectCompetitorsByProduct();

    /** 경쟁사 지역별 현황 집계 */
    List<Map<String, Object>> selectCompetitorsByRegion();

    /** 전체 우수제품 상세 목록 (탑그룹 + 경쟁사 포함) */
    List<Map<String, Object>> selectAllExcellentDetail();
}
