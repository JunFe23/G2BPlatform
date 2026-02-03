package org.example.g2bplatform.controller;

import org.example.g2bplatform.service.GoogleSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sheet")
public class SheetController {

    @Autowired(required = false)
    private GoogleSheetService sheetService;

    @GetMapping("/test")
    public String test() {
        if (sheetService == null) {
            return "Google Sheets 미설정(google.sheets.enabled=true 및 키 파일 필요)";
        }
        try {
            sheetService.testWriteToSheet();
            return "스프레드시트 업데이트 성공!";
        } catch (Exception e) {
            e.printStackTrace();
            return "실패: " + e.getMessage();
        }
    }
}
