package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 쇼핑몰(제3자단가) 납품 실적 — shopping_mall_flat 조회 응답.
 */
@Data
public class ShoppingMallFlatDto {

    private String deliveryContractNo;
    private Integer deliveryContractChangeSeq;
    private Integer deliveryItemSeq;
    private String vendorBizRegNo;
    private String vendorName;
    private String contractTitle;
    private String demandAgencyName;
    private String demandAgencyRegion;
    private String contractMethod;
    private String contractNo;
    private String deliveryPlaceName;
    private String itemCategoryNo;
    private String itemCategoryName;
    private String detailItemNo;
    private String detailItemName;
    private String itemIdentifierNo;
    private String itemIdentifierName;
    private String unit;
    private Long unitPrice;
    private Long quantity;
    private Long supplyAmount;
    private String isMas;
    private String isExcellentProduct;
    private String isDirectPurchaseTarget;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate refDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstRefDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDeadlineDate;
}
