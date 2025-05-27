package org.example.g2bplatform.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.g2bplatform.mapper.DataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    public ByteArrayOutputStream createExcelFile(List<Map<String, String>> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("조회결과");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {"cmpNm", "cntrctNm", "dminsttNm", "dminsttNmDetail", "prdctClsfcNo", "cntctCnclsMthdNm", "ntceNo", "firstCntrctDate", "firstCntrctAmt", "cntrctDate", "thtmCntrctAmt", "cntrctCnt"};
            String[] headerNames = {"업체명", "계약건명", "수요기관명", "수요기관지역명", "품명내용", "입찰계약방법", "입찰공고번호", "최초계약일자", "계약금액", "계약일자", "계약금액", "계약변경차수"};

            for (int i = 0; i < headerNames.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerNames[i]);
            }

            // 데이터 추가
            int rowNum = 1;
            for (Map<String, String> row : data) {
                Row excelRow = sheet.createRow(rowNum++);
                for (int colNum = 0; colNum < headers.length; colNum++) {
                    String key = headers[colNum];
                    Object value = row.getOrDefault(key, ""); // 데이터 값 가져오기
                    Cell cell = excelRow.createCell(colNum);

                    // 데이터 타입 확인 및 처리
                    if (value instanceof BigDecimal) {
                        cell.setCellValue(value.toString()); // BigDecimal -> 문자열 변환
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue()); // 숫자 -> double 변환
                    } else {
                        cell.setCellValue(value.toString()); // 기타 데이터 -> 문자열 변환
                    }
                }
            }

            workbook.write(out);
            return out;
        }
    }
}
