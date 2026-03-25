package org.example.g2bplatform.DTO;

import lombok.Data;

/**
 * 쇼핑몰 flat 목록 검색 조건 (문서화·서비스 레이어 전달용).
 */
@Data
public class ShoppingMallSearchDto {
    private String dateFrom;
    private String dateTo;
    private String vendorBizRegNo;
    private String demandAgencyName;
    private String demandAgencyRegion;
    private String itemCategoryNo;
    private String detailItemNo;
    private String isMas;
    private String isExcellentProduct;
    private int page = 1;
    private int size = 50;
}
