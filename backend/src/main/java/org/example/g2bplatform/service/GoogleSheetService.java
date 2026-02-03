package org.example.g2bplatform.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.example.g2bplatform.config.GoogleSheetConfig;
import org.example.g2bplatform.mapper.GoogleSheetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@ConditionalOnBean(GoogleSheetConfig.class)
public class GoogleSheetService {
    private final Sheets sheets;
//    private final String spreadsheetId = "1eIjkSF2RGMfRmpmA0JtOko-5DDlud7Luo3h1a6Mq7kA";
    private final String spreadsheetId = "1SrVCuagp9Yyuv3B854IC90do9iWz4_Z1ptxj66BHwX8";
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
        // 날짜 문자열 생성
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String updateNote = "업데이트 날짜: " + today;

        // 1. 기존 시트 내용 전체 삭제
        sheets.spreadsheets().values()
                .clear(spreadsheetId, sheetName, new ClearValuesRequest())
                .execute();

        // 2. 시트에 새 데이터 작성 (A1: 날짜, A2: 헤더, A3~: 본문 데이터)
        List<List<Object>> sheetData = new ArrayList<>();

        // A1: 날짜
        sheetData.add(Collections.singletonList(updateNote));

        // A2: 헤더
        sheetData.add(Arrays.asList(
                "분류", "업체명", "계약건명", "수요기관명", "수요기관지역명",
                "품명내용", "입찰계약방법", "입찰공고번호",
                "최초계약일자", "최초계약금액", "최종계약일자", "최종계약금액", "계약변경차수"
        ));

        // A3~: 본문 데이터
        List<Map<String, String>> data = googleSheetMapper.getTopsDataForSheet();
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

        // 시트에 데이터 입력
        ValueRange body = new ValueRange().setValues(sheetData);
        sheets.spreadsheets().values()
                .update(spreadsheetId, sheetName + "!A1", body)
                .setValueInputOption("RAW")
                .execute();

        // 시트 ID 가져오기
        Integer sheetId = getSheetIdByName(sheetName);

        // A1 빨간 텍스트 (업데이트 날짜)
        CellFormat redTextFormat = new CellFormat()
                .setTextFormat(new TextFormat().setForegroundColor(new Color().setRed(1f).setGreen(0f).setBlue(0f)).setBold(true));

        Request redTextFormatRequest = new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setRange(new GridRange()
                                .setSheetId(sheetId)
                                .setStartRowIndex(0).setEndRowIndex(1)
                                .setStartColumnIndex(0).setEndColumnIndex(1))
                        .setCell(new CellData().setUserEnteredFormat(redTextFormat))
                        .setFields("userEnteredFormat.textFormat"));

        // A2 노란 배경 헤더
        CellFormat yellowBgFormat = new CellFormat()
                .setBackgroundColor(new Color().setRed(1f).setGreen(1f).setBlue(0f))
                .setTextFormat(new TextFormat().setBold(true));

        Request yellowHeaderBackground = new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setRange(new GridRange()
                                .setSheetId(sheetId)
                                .setStartRowIndex(1).setEndRowIndex(2)
                                .setStartColumnIndex(0).setEndColumnIndex(13))
                        .setCell(new CellData().setUserEnteredFormat(yellowBgFormat))
                        .setFields("userEnteredFormat(backgroundColor,textFormat.bold)"));

        // A2에 필터 적용
        Request setFilterRequest = new Request()
                .setSetBasicFilter(new SetBasicFilterRequest()
                        .setFilter(new BasicFilter()
                                .setRange(new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(1)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(13))));

        // 요청 배치 실행
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Arrays.asList(
                        redTextFormatRequest,
                        yellowHeaderBackground,
                        setFilterRequest
                ));

        sheets.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

    private Integer getSheetIdByName(String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();
            }
        }
        throw new IllegalArgumentException("Sheet with name '" + sheetName + "' not found.");
    }
}
