package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractShoppingmallDTO {

    // ===== 복합키 =====
    private String shopngCntrctNo;   // 쇼핑계약번호 (PK part)
    private String shopngCntrctSno;  // 쇼핑계약순번 (PK part)

    // ===== 새 스펙 필드 =====
    private String prdctImgUrl;          // 상품이미지URL
    private String cntrctCorpNm;         // 계약업체명
    private String entrprsDivNm;         // 기업구분명
    private String cntrctMthdNm;         // 계약방법명
    private String exclncPrcrmntPrdctYn; // 우수조달물품여부 (Y/N)
    private String masYn;                // 다수공급경쟁자여부 (Y/N)
    private String smetprCmptProdctYn;   // 중소기업자간경쟁제품여부 (Y/N)

    private BigDecimal cntrctPrceAmt;    // 계약가격금액(원화, 정수)

    private String prdctUnit;            // 물품단위
    private String prdctMakrNm;          // 물품제조사명
    private String prdctDlvrPlceNm;      // 물품납품장소명
    private String prdctDlvryCndtnNm;    // 물품인도조건명
    private String prdctSplyRgnNm;       // 물품공급지역명
    private String dlvrTmlmtDaynum;      // 납품기한일수

    private String prdctLrgclsfcCd;      // 물품대분류코드
    private String prdctLrgclsfcNm;      // 물품대분류명
    private String prdctMidclsfcCd;      // 물품중분류코드
    private String prdctMidclsfcNm;      // 물품중분류명

    private String prdctClsfcNo;         // 물품분류번호(8)
    private String prdctClsfcNoNm;       // 품명(물품분류번호명)
    private String dtilPrdctClsfcNo;     // 세부품명번호(10)
    private String dtilPrdctClsfcNoNm;   // 세부품명
    private String prdctIdntNo;          // 물품식별번호(8)

    private String prdctSpecNm;          // 물품규격명

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate cntrctDate;        // 계약일자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate cntrctBgnDate;     // 계약시작일자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate cntrctEndDate;     // 계약종료일자

    private String cntrctDeptNm;         // 계약부서명
    private String prodctCertList;       // 제품인증목록

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate rgstDt;        // 등록일시

    private String cntrctCorpBizno;      // 계약업체 사업자등록번호
}
