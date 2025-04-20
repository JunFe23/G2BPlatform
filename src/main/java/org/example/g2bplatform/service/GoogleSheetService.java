package org.example.g2bplatform.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.g2bplatform.config.GoogleSheetConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleSheetService {
    private final Sheets sheets;
    private final String spreadsheetId = "1eIjkSF2RGMfRmpmA0JtOko-5DDlud7Luo3h1a6Mq7kA";
    private final String sheetName = "시트1"; // 구글 시트 탭 이름 확인!

    public GoogleSheetService(GoogleSheetConfig config) throws Exception {
        this.sheets = config.getSheetsService();
    }

    public void testWriteToSheet() throws IOException {
//    public void updateUserSheet(List<UserDto> users) throws IOException {
//        List<List<Object>> sheetData = new ArrayList<>();
//        sheetData.add(List.of("ID", "이름", "이메일", "가입일"));
//
//        for (UserDto user : users) {
//            sheetData.add(List.of(
//                    user.getId(),
//                    user.getName(),
//                    user.getEmail(),
//                    user.getCreatedAt().toString()
//            ));
//        }

        // 테스트용 데이터 생성
        List<List<Object>> values = new ArrayList<>();
        values.add(Arrays.asList("이름", "나이", "국가"));
        values.add(Arrays.asList("홍길동", "30", "대한민국"));
        values.add(Arrays.asList("John Doe", "28", "USA"));

        ValueRange body = new ValueRange().setValues(values);
        sheets.spreadsheets().values()
                .update(spreadsheetId, sheetName + "!A1", body) // 여기가 포인트!
                .setValueInputOption("RAW")
                .execute();
    }
}
