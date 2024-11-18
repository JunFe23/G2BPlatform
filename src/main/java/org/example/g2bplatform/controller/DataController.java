package org.example.g2bplatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    @GetMapping("/data")
    public List<Map<String, String>> getData(@RequestParam String category) {
        List<Map<String, String>> data = new ArrayList<>();

        // 예시 데이터 - 실제로는 데이터베이스에서 가져와야 함
        if ("goods".equals(category)) {
            data.add(Map.of("id", "1", "name", "Item A", "category", "물품", "details", "Details about Item A"));
            data.add(Map.of("id", "2", "name", "Item B", "category", "물품", "details", "Details about Item B"));
        } else if ("services".equals(category)) {
            data.add(Map.of("id", "1", "name", "Service A", "category", "용역", "details", "Details about Service A"));
            data.add(Map.of("id", "2", "name", "Service B", "category", "용역", "details", "Details about Service B"));
        } else if ("construction".equals(category)) {
            data.add(Map.of("id", "1", "name", "Construction A", "category", "공사", "details", "Details about Construction A"));
            data.add(Map.of("id", "2", "name", "Construction B", "category", "공사", "details", "Details about Construction B"));
        }

        return data;
    }
}
