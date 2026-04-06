package org.example.g2bplatform.service;

import org.example.g2bplatform.DTO.ShoppingMallFlatDto;
import org.example.g2bplatform.mapper.ShoppingMallMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class ShoppingMallService {

    private final ShoppingMallMapper shoppingMallMapper;

    public ShoppingMallService(ShoppingMallMapper shoppingMallMapper) {
        this.shoppingMallMapper = shoppingMallMapper;
    }

    /**
     * 쇼핑몰(제3자단가) 납품 실적 목록 (페이지네이션).
     *
     * @param page 1부터 시작
     */
    public Map<String, Object> getFlatList(
            String dateFrom, String dateTo,
            String vendorBizRegNo,
            String demandAgencyName, String demandAgencyRegion,
            String itemCategoryNo, String detailItemNo,
            String isMas, String isExcellentProduct,
            int page, int size
    ) {
        int p = Math.max(1, page);
        int s = Math.max(1, Math.min(size, 500));
        int offset = (p - 1) * s;

        List<ShoppingMallFlatDto> content = shoppingMallMapper.selectFlatList(
                dateFrom, dateTo, vendorBizRegNo,
                demandAgencyName, demandAgencyRegion,
                itemCategoryNo, detailItemNo,
                isMas, isExcellentProduct,
                offset, s);
        int totalCount = shoppingMallMapper.selectFlatCount(
                dateFrom, dateTo, vendorBizRegNo,
                demandAgencyName, demandAgencyRegion,
                itemCategoryNo, detailItemNo,
                isMas, isExcellentProduct);

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("totalCount", totalCount);
        body.put("page", p);
        body.put("size", s);
        return body;
    }

    /**
     * 동일 필터로 shopping_mall_flat 전체를 배치 단위로 순회 (엑셀 스트리밍 등).
     *
     * @param batchSize 권장 500~5000
     */
    public void forEachFlatBatch(
            String dateFrom, String dateTo,
            String vendorBizRegNo,
            String demandAgencyName, String demandAgencyRegion,
            String itemCategoryNo, String detailItemNo,
            String isMas, String isExcellentProduct,
            int batchSize,
            Consumer<List<ShoppingMallFlatDto>> batchConsumer
    ) {
        int batch = Math.max(100, Math.min(batchSize, 5000));
        int offset = 0;
        while (true) {
            List<ShoppingMallFlatDto> chunk = shoppingMallMapper.selectFlatList(
                    dateFrom, dateTo, vendorBizRegNo,
                    demandAgencyName, demandAgencyRegion,
                    itemCategoryNo, detailItemNo,
                    isMas, isExcellentProduct,
                    offset, batch);
            if (chunk == null || chunk.isEmpty()) {
                break;
            }
            batchConsumer.accept(chunk);
            if (chunk.size() < batch) {
                break;
            }
            offset += batch;
        }
    }
}
