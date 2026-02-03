package org.example.g2bplatform.controller;

import org.example.g2bplatform.service.GoogleSheetService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sheet")
@ConditionalOnBean(GoogleSheetService.class)
public class SheetController {
    private final GoogleSheetService sheetService;

    public SheetController(GoogleSheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping("/test")
    public String test() {
        try {
            sheetService.testWriteToSheet();
            return "스프레드시트 업데이트 성공!";
        } catch (Exception e) {
            e.printStackTrace();
            return "실패: " + e.getMessage();
        }
    }
}
