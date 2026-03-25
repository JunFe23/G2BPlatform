package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.g2bplatform.service.ShoppingMallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Shopping Mall", description = "종합쇼핑몰(제3자단가계약) 납품 실적")
@RestController
@RequestMapping("/api/shopping-mall")
public class ShoppingMallController {

    private final ShoppingMallService shoppingMallService;

    public ShoppingMallController(ShoppingMallService shoppingMallService) {
        this.shoppingMallService = shoppingMallService;
    }

    @Operation(summary = "쇼핑몰 납품 실적 목록", description = "shopping_mall_flat 기준 조회")
    @GetMapping("/flat")
    public ResponseEntity<Map<String, Object>> getFlat(
            @Parameter(description = "기간 시작 (ref_date >=), yyyy-MM-dd") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "기간 종료 (ref_date <=), yyyy-MM-dd") @RequestParam(required = false) String dateTo,
            @Parameter(description = "업체사업자등록번호") @RequestParam(required = false) String vendorBizRegNo,
            @Parameter(description = "수요기관명 (부분일치)") @RequestParam(required = false) String demandAgencyName,
            @Parameter(description = "수요기관지역 (부분일치)") @RequestParam(required = false) String demandAgencyRegion,
            @Parameter(description = "물품분류번호") @RequestParam(required = false) String itemCategoryNo,
            @Parameter(description = "세부품명번호") @RequestParam(required = false) String detailItemNo,
            @Parameter(description = "MAS여부 Y/N") @RequestParam(required = false) String isMas,
            @Parameter(description = "우수제품여부 Y/N") @RequestParam(required = false) String isExcellentProduct,
            @Parameter(description = "페이지 (1부터)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int size
    ) {
        Map<String, Object> data = shoppingMallService.getFlatList(
                dateFrom, dateTo, vendorBizRegNo,
                demandAgencyName, demandAgencyRegion,
                itemCategoryNo, detailItemNo,
                isMas, isExcellentProduct,
                page, size);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.putAll(data);
        return ResponseEntity.ok(res);
    }
}
