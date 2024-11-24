package org.example.g2bplatform.controller;

import org.example.g2bplatform.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/data")
    public Map<String, Object> getData( @RequestParam int draw,
                                              @RequestParam int start,
                                              @RequestParam int length,
                                              @RequestParam Map<String, String> search, // Map으로 검색 값을 받아옵니다
                                              @RequestParam String category) {
        String searchValue = search.get("search[value]"); // DataTables가 전송하는 검색어

        List<Map<String, String>> data = new ArrayList<>();
        int totalRecords = 0;
        int filteredRecords = 0;

        // 예시 데이터 - 실제로는 데이터베이스에서 가져와야 함
        if ("goods".equals(category)) {
            data = dataService.getThingsData(start, length, searchValue, category);
            totalRecords = dataService.getThingsTotalCount(category);
            filteredRecords = dataService.getThingsFilteredCount(searchValue, category);
        } else if ("services".equals(category)) {
            data = dataService.getServicesData(start, length, searchValue, category);
            totalRecords = dataService.getServicesTotalCount(category);
            filteredRecords = dataService.getServicesFilteredCount(searchValue, category);
        } else if ("constructions".equals(category)) {
            data = dataService.getConstructionsData(start, length, searchValue, category);
            totalRecords = dataService.getConstructionsTotalCount(category);
            filteredRecords = dataService.getConstructionsFilteredCount(searchValue, category);
        } else if ("onlyTop".equals(category)) {
            data = dataService.getTopsData(start, length, searchValue, category);
            totalRecords = dataService.getTopsTotalCount(category);
            filteredRecords = dataService.getTopsFilteredCount(searchValue, category);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", totalRecords);
        response.put("recordsFiltered", filteredRecords);
        response.put("data", data);

        return response;
    }
}
