package org.example.g2bplatform.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.scheduling.annotation.Scheduled;
import org.example.g2bplatform.config.GoogleSheetConfig;
import org.example.g2bplatform.mapper.GoogleSheetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSheetService {
    private final Sheets sheets;
    private final String spreadsheetId = "1eIjkSF2RGMfRmpmA0JtOko-5DDlud7Luo3h1a6Mq7kA";
    private final String sheetName = "시트1"; // 구글 시트 탭 이름 확인!

    @Autowired
    private GoogleSheetMapper googleSheetMapper;

    public GoogleSheetService(GoogleSheetConfig config) throws Exception {
        this.sheets = config.getSheetsService();
    }

    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시
    public void updateSheetEveryMorning() throws IOException {
        testWriteToSheet();
    }

    public void testWriteToSheet() throws IOException {
        List<List<Object>> sheetData = new ArrayList<>();
        sheetData.add(Arrays.asList(
                "분류", "업체명", "계약건명", "수요기관명", "수요기관지역명",
                "품명내용", "입찰계약방법", "입찰공고번호",
                "최초계약일자", "최초계약금액", "최종계약일자", "최종계약금액", "계약변경차수"
        ));

        List<Map<String, String>> data = new ArrayList<>();
        data = googleSheetMapper.getTopsDataForSheet();

        // 데이터 행 추가
        for (Map<String, String> row : data) {
            sheetData.add(Arrays.asList(
                    row.getOrDefault("type", ""),
                    row.getOrDefault("cmpNm", ""),
                    row.getOrDefault("cntrctNm", ""),
                    row.getOrDefault("dminsttNm", ""),
                    row.getOrDefault("dminsttNmDetail", ""),
                    row.getOrDefault("prdctClsfcNo", ""),
                    row.getOrDefault("cntctCnclsMthdNm", ""),
                    row.getOrDefault("ntceNo", ""),
                    row.getOrDefault("firstCntrctDate", ""),
                    row.getOrDefault("firstCntrctAmt", ""),
                    row.getOrDefault("cntrctDate", ""),
                    row.getOrDefault("thtmCntrctAmt", ""),
                    row.getOrDefault("cntrctCnt", "")
            ));
        }

        // 기존 시트 내용 전체 삭제
        sheets.spreadsheets().values()
                .clear(spreadsheetId, sheetName, new ClearValuesRequest())
                .execute();

        // 시트 다시 입력
        ValueRange body = new ValueRange().setValues(sheetData);
        sheets.spreadsheets().values()
                .update(spreadsheetId, sheetName + "!A1", body)
                .setValueInputOption("RAW")
                .execute();
    }
}
