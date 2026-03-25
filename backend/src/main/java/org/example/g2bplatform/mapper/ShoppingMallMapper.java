package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.g2bplatform.DTO.ShoppingMallFlatDto;

import java.util.List;

@Mapper
public interface ShoppingMallMapper {

    List<ShoppingMallFlatDto> selectFlatList(
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("detailItemNo") String detailItemNo,
            @Param("isMas") String isMas,
            @Param("isExcellentProduct") String isExcellentProduct,
            @Param("offset") int offset,
            @Param("size") int size
    );

    int selectFlatCount(
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("vendorBizRegNo") String vendorBizRegNo,
            @Param("demandAgencyName") String demandAgencyName,
            @Param("demandAgencyRegion") String demandAgencyRegion,
            @Param("itemCategoryNo") String itemCategoryNo,
            @Param("detailItemNo") String detailItemNo,
            @Param("isMas") String isMas,
            @Param("isExcellentProduct") String isExcellentProduct
    );
}
