package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ContractShoppingmallDTO {

    private String shopngCntrctNo; // 쇼핑계약번호
    private String shopngCntrctSno; // 쇼핑계약순번
    private String prdctImgUrl; // 상품이미지URL
    private String cntrctCorpNm; // 계약업체명
    private String entrprsDivNm; // 기업구분명
    private String drctPrdctnCnfrmCertHoldCorpYn; // 직접생산확인증명서 보유여부
    private String entrprsDivInfrmtn; // 기업구분안내
    private String cntrctMthdNm; // 계약방법명
    private String prdctSpecNm; // 물품규격명
    private String exclncPrcrmntPrdctYn; // 우수조달물품여부
    private String bndeFlawGrntyPrdctYn; // 일괄하자보증물품여부
    private String smetprCmptProdctYn; // 중소기업자간경쟁제품여부

    private BigDecimal cntrctPrceAmt; // 계약가격금액
    private BigDecimal orderCalclPrceAmt; // 주문산정가격금액
    private BigDecimal dscntAmt; // 할인금액

    private String prdctUnit; // 물품단위
    private String prdctOrgplceNm; // 물품원산지명
    private String orgplceMainCmpnt1Cntnts; // 원산지 주요부품1내용
    private String orgplceMainCmpnt1Nm; // 원산지 주요부품1명
    private String orgplceMainCmpnt2Cntnts; // 원산지 주요부품2내용
    private String orgplceMainCmpnt2Nm; // 원산지 주요부품2명

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt; // 등록일시

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt; // 변경일시
}

